package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.*;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.utils.ConfigReader;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentService;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.inventory.model.stockmovement.StockMovementDetail;
import com.krawler.inventory.model.stockmovement.StockMovementService;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockmovement.TransactionType;
import com.krawler.inventory.model.store.StorageException;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.term.accTermDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class OlympusImportDataServiceDAOImpl implements OlympusImportDataServiceDAO {

    private accCustomerDAO accCustomerDAOobj;
    private accTermDAO accTermObj;
    private accAccountDAO accAccountDAOobj;
    private accProductDAO accProductDAO;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private StoreService storeService;
    private LocationService locationService;
    private StockMovementService stockMovementService;
    private StockService stockService;
    private HibernateTransactionManager txnManager;
    private fieldDataManager fieldDataManagercntrl;
    private auditTrailDAO auditTrailObj;
    private ImportDAO importDao;
    private ConsignmentService consignmentService;
    private authHandlerDAO authHandlerDAOObj;
    private AccProductModuleService accProductModuleService;
    private static final DateFormat commonDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat importDF = new SimpleDateFormat("yyyyMMdd");

    public enum ImportScriptsFilePathAndDefaultValues {

        PRODUCTMASTER,
        CUSTOMERBILLINGMASTER,
        CUSTOMERSHIPPINGMASTER,
        LICENSEMASTER,
        SUPPLLICENSE1,
        SUPPLLICENSE2,
        STOCKMOVEMENT1,
        STOCKMOVEMENT2,
        STOCKMOVEMENT3,
        STOCKMOVEMENT4
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setaccTermDAO(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccProductDAO(accProductDAO accProductDAO) {
        this.accProductDAO = accProductDAO;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    
    public void setConsignmentService(ConsignmentService consignmentService) {
        this.consignmentService = consignmentService;
    }
    
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    
     public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }
     
    public Company getCompanyObject(String olympusSubdomain) throws ServiceException {
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        /*
         * Finding Company from subdomain
         */
        Company company = null;
        HashMap<String, Object> companyRequestParams = new HashMap<String, Object>();
        filter_names.add("c.subDomain");
        filter_params.add(olympusSubdomain);
        companyRequestParams.put("filter_names", filter_names);
        companyRequestParams.put("filter_values", filter_params);
        KwlReturnObject companyListObj = accCommonTablesDAO.getCompany(companyRequestParams);
        if (companyListObj.getEntityList().size() > 0) {
            List<Company> companyList = companyListObj.getEntityList();
            company = companyList.get(0);
        }

        return company;
    }

    @Override
    public JSONObject importMasterLicense(String baseUrl, String olympusSubdomain) throws JSONException {
        return importMasterLicense(null, baseUrl, olympusSubdomain);
    }
    @Override
    public JSONObject importMasterLicense(String filePath, String baseUrl, String olympusSubdomain) throws JSONException {
        StringBuilder msg = new StringBuilder();
        boolean issuccess = true;
        StringBuilder destinationFileName = new StringBuilder();
        JSONObject resultJObj = new JSONObject();
        int cnt = 0, total = 0, TotalBatchCnt = Constants.Transaction_Commit_Limit_50, initBatchCnt = 0 ;
        int failedCnt = 0;
        String processfilepath="", failuefilepath = "", orgfile="";
        String toEmailID="", fromEmailID="";
        Company company =null;
        String importLogID = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        Map<String,String> map = new HashMap<>();                                
        try {
            System.out.println("Started at :"+ new Date());
            company = getCompanyObject(olympusSubdomain);
            StringBuilder failedRecords = new StringBuilder();
            StringBuilder successRecords = new StringBuilder();
            List<Object> fileData = new ArrayList();
            List<Integer> validCellIndex = new ArrayList();
            String localcode = "", overseascode = "", localcategory = "", overseascategory = "";
//            String olympusSubdomain = !StringUtil.isNullOrEmpty(request.getParameter("cdomain"))?request.getParameter("cdomain"):"";
            int noofcol=0;
            String scriptinfo = "select filepath,importinfo,noofcol from olympus_importscriptinfo where id = ? and subdomain = ?";
//            filePath = "/Users/sagar/Desktop/Master License.xls";
            List scriptInfoList = null;
            scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.LICENSEMASTER.toString(), olympusSubdomain});
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String customfield = "";
            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            List headArrayList = new ArrayList();
            DateFormat df = authHandler.getGlobalDateFormat();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
            Date licenseValidityStartDate = null;
            Date licenseValidityEndDate = null;
            JSONObject scriptInfoData=null;
            boolean flag=false;
            String xlsfilepath="";
            if (scriptInfoList.size() > 0) {
                    Object[] row = (Object[]) scriptInfoList.get(0);
                    if(StringUtil.isNullOrEmpty(filePath)){
                    filePath = row[0].toString();
                    }
                    processfilepath = filePath;
                    orgfile=processfilepath.substring(processfilepath.lastIndexOf("/")+1);
                    noofcol = Integer.parseInt(row[2].toString());
                    if(!StringUtil.isNullOrEmpty(filePath)){
                        String ext = FilenameUtils.getExtension(filePath);
                        xlsfilepath = filePath.replace(ext, "xls");
                        long start = new Date().getTime();
                        flag=convertToXLS(filePath, xlsfilepath, noofcol);
                        long end = new Date().getTime();
                        System.out.println("\nConvert file : "+(end-start)+" millisec .\n");
                        Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nConvert file : "+(end-start)+" millisec .\n", "");
                    }
                    scriptInfoData = new JSONObject(row[1].toString());
            }
            if (company != null && flag) {
                filePath=xlsfilepath;
                fromEmailID = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                scriptinfo = "select emailids from olympus_importinfo where subdomain = ?";
                List scriptEmailInfo = null;
                scriptEmailInfo = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{olympusSubdomain});
                if (scriptEmailInfo.size() > 0) {
                    toEmailID = scriptEmailInfo.get(0)!=null ? scriptEmailInfo.get(0).toString() :"";
                }
                Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "ImportMasterLicense started", "");
                if(scriptInfoData!=null){
                    if (scriptInfoData.has("localcode")) {
                        localcode = scriptInfoData.getString("localcode");
                    }
                    if (scriptInfoData.has("overseascode")) {
                        overseascode = scriptInfoData.getString("overseascode");
                    }
                    if (scriptInfoData.has("localcategory")) {
                        localcategory = scriptInfoData.getString("localcategory");
                    }
                    if (scriptInfoData.has("overseascategory")) {
                        overseascategory = scriptInfoData.getString("overseascategory");
                    }
                }
                HashMap<String, String> requestMailParams = new HashMap<>();
                requestMailParams.put("company", company.getCompanyID());
                requestMailParams.put("toEmailID", toEmailID);
                /**
                 * method used to get email IDs from companypreferences setting
                 *
                 */
                map = getAllMailsIDS(requestMailParams);
                POIFSFileSystem fs;
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                int sheetNo = 0;
                int maxCol = 0;
                int productIDIndex = -1;
                int licenseTypeIndex = -1;
                int licenseCodeIndex = -1;
                int licenseValiditySDIndex = -1;
                int licenseValidityEDIndex = -1;
                int licenseStatusIndex = -1;
                Date processStartDate = new Date();
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);
                total = sheet.getLastRowNum();
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                        OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, -1, msg, processStartDate, total);
                        failuefilepath = destinationFileName.toString();
                        ImportLog logObj = OlympusImportDataHandler.addInitialLogEntry(importDao, "", destinationFileName, msg, cnt, failedCnt, company, issuccess, "30", processStartDate);
                        importLogID = logObj.getId();
                    }
                    
                    if(initBatchCnt>=TotalBatchCnt) {
                        initBatchCnt = 0;
                        System.out.println("LOG ENTRY - "+cnt);
                        OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, cnt, msg, processStartDate, total);
                        OlympusImportDataHandler.addInitialLogEntry(importDao, importLogID, destinationFileName, msg, cnt, failedCnt, null, issuccess, "30", null);
                        txnManager.commit(status);
                        status = txnManager.getTransaction(def);
                    }
                    initBatchCnt++; 
                    
                    String licenseType = "";
                    String productID = "";
                    String licenseCode = "";
                    String licenseValiditySD = "";
                    String licenseValidityED = "";
                    String licenseStatus = "";
                    if (cnt != 0) {
                        fileData.clear();
                        try {
                            for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                                HSSFCell cell = row.getCell(cellcount);
                                boolean allowData = false;
                                if (cell != null) {
                                    allowData = true;
                                    if (cellcount == productIDIndex) {
//                                        productID = getCellValue(cell);
                                        productID = cell != null ? org.apache.commons.lang.StringUtils.stripStart(getCellValue(cell),"0") : "";
                                    } else if (cellcount == licenseTypeIndex) {
                                        licenseType = getCellValue(cell);
                                    } else if (cellcount == licenseCodeIndex) {
                                        licenseCode = getCellValue(cell);
                                    } else if (cellcount == licenseValiditySDIndex) {
                                        licenseValiditySD = getCellValue(cell);
                                        try {
                                            licenseValidityStartDate = sdf.parse(licenseValiditySD);
                                        } catch (ParseException pex) {
                                            licenseValidityStartDate = sdf1.parse(licenseValiditySD);
                                        }
                                    } else if (cellcount == licenseValidityEDIndex) {
                                        licenseValidityED = getCellValue(cell);
                                        try {
                                            licenseValidityEndDate = sdf.parse(licenseValidityED);
                                        } catch (ParseException pex) {
                                            licenseValidityEndDate = sdf1.parse(licenseValidityED);
                                        }
                                    } else if (cellcount == licenseStatusIndex) {
                                        licenseStatus = getCellValue(cell);
                                    } else {
                                        allowData = false;
                                    }
                                }
                                if (validCellIndex.contains(Integer.valueOf(cellcount))) {
                                    if (cell != null) {
                                        fileData.add(getCellValue(cell));
                                    } else {
                                        fileData.add("");
                                    }
                                }
                            }
                            try {
                                filter_names.clear();
                                filter_params.clear();
                                KwlReturnObject productListObj = accProductDAO.getProduct(productID, company.getCompanyID());
                                List<Product> productList = productListObj.getEntityList();
                                if (productList.size() > 0) {
                                    Product product = productList.get(0);
                                    product.setLicenseCode(licenseCode);
                                    if (licenseType.equals(localcode)) { // "ZH01"
                                        product.setLicenseType(LicenseType.LOCAL);
                                        product.setCustomerCategory(localcategory);
                                    } else if (licenseType.equals(overseascode)) { // "ZH02"
                                        product.setLicenseType(LicenseType.OVERSEAS);
                                        product.setCustomerCategory(overseascategory);
                                    } else {
                                        product.setLicenseType(LicenseType.NONE);
                                    }
                                    // Save Product
                                    List productUpdateList = new ArrayList();
                                    productUpdateList.add(product);
                                    accAccountDAOobj.saveOrUpdateAll(productUpdateList);

                                    String findMasterLicenseEntry = "select * from olympus_licensemaster where productid=?";
                                    List masterLicenseList = accCommonTablesDAO.executeSQLQuery(findMasterLicenseEntry, new Object[]{product.getProductid()});
                                    if (masterLicenseList.size() == 0) {
                                        String insertmasterLicenseData = "INSERT into olympus_licensemaster(id, productid, licensetype, "
                                                + "licensecode, vstartdate, venddate, licstatus)"
                                                + " values(?,?,?,?,?,?,?)";
                                        accCommonTablesDAO.executeSQLUpdate(insertmasterLicenseData, new Object[]{StringUtil.generateUUID(), product.getProductid(),
                                                    product.getLicenseType().name(), product.getLicenseCode(), licenseValidityStartDate, licenseValidityEndDate, licenseStatus});
                                    } else {
                                        String updatemasterLicenseData = "UPDATE olympus_licensemaster set licensetype =?, "
                                                + "licensecode =? , vstartdate =? , venddate=? , licstatus=? where productid = ?";
                                        accCommonTablesDAO.executeSQLUpdate(updatemasterLicenseData, new Object[]{product.getLicenseType().name(), product.getLicenseCode(),
                                                    licenseValidityStartDate, licenseValidityEndDate, licenseStatus, product.getProductid()});
                                    }

//                                msg += "\n---------------Product License updated - " + productID + "-------------------------------------";
//                                System.out.println("\n---------------Product License updated - " + productID + "-------------------------------------");
                                    Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nSr. No. " + i + "---Product License updated - " + productID + "---", "");

                                    // For creating custom field array
                                    customfield = "";
                                    JSONArray customJArr = new JSONArray();
                                    KwlReturnObject productObj = null;
                                    HashMap<String, Object> productRequestParams = new HashMap<String, Object>();
                                    for (int K = 0; K < headArrayList.size(); K++) {
                                        HashMap<String, Object> requestParamsCF = new HashMap<String, Object>();
                                        requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                        requestParamsCF.put(Constants.filter_values, Arrays.asList(company.getCompanyID(), Constants.Acc_Product_Master_ModuleId, headArrayList.get(K)));
                                        KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                        FieldParams params = null;
                                        if (fieldParamsResult.getRecordTotalCount() > 0) {
                                            params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                        }

                                        HSSFCell cell = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                        if (params != null && cell != null) {
                                            JSONObject customJObj = new JSONObject();
                                            customJObj.put("fieldid", params.getId());
                                            customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                            customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                            customJObj.put("xtype", params.getFieldtype());

                                            String fieldComboDataStr = "";
                                            if (params.getFieldtype() == 3) { // if field of date type
                                                HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                                String dateStr = cell1 != null ? getCellValue(cell1) : "";
                                                try {
                                                    customJObj.put("Col" + params.getColnum(), sdf.parse(dateStr).getTime());
                                                    customJObj.put("fieldDataVal", sdf.parse(dateStr).getTime());
                                                } catch (ParseException pex) {
                                                    customJObj.put("Col" + params.getColnum(), sdf1.parse(dateStr).getTime());
                                                    customJObj.put("fieldDataVal", sdf1.parse(dateStr).getTime());
                                                }
                                            } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                                HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                                String comboRecords = cell1 != null ? getCellValue(cell1) : "";
                                                String[] fieldComboDataArr = comboRecords.split(";");

                                                for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                                    requestParamsCF = new HashMap<String, Object>();
                                                    requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                                    requestParamsCF.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));

                                                    fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParamsCF);
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
                                                HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                                String checkbox = cell1 != null ? getCellValue(cell1) : "";
                                                customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(checkbox));
                                                customJObj.put("fieldDataVal", Boolean.parseBoolean(checkbox));
                                            } else if (params.getFieldtype() == 12) { // if field of check list type
                                                requestParamsCF = new HashMap<String, Object>();
                                                requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                                requestParamsCF.put(Constants.filter_values, Arrays.asList(params.getId(), 0));

                                                fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParamsCF);
                                                List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();
                                                HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                                String comboRecords = cell1 != null ? getCellValue(cell1) : "";

                                                String[] fieldComboDataArr = comboRecords.split(";");

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
                                                HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                                String record = cell1 != null ? getCellValue(cell1) : "";
                                                customJObj.put("Col" + params.getColnum(), record);
                                                customJObj.put("fieldDataVal", record);
                                            }

                                            customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                            customJArr.put(customJObj);
                                        }
                                    }

                                    customfield = customJArr.toString();
                                    if (product != null && !StringUtil.isNullOrEmpty(customfield)) {
                                        JSONArray jcustomarray = new JSONArray(customfield);
                                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                                        customrequestParams.put("customarray", jcustomarray);
                                        customrequestParams.put("modulename", Constants.Acc_Product_modulename);
                                        customrequestParams.put("moduleprimarykey", Constants.Acc_Productid);
                                        customrequestParams.put("modulerecid", product.getID());
                                        customrequestParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                                        customrequestParams.put("companyid", company.getCompanyID());
                                        productRequestParams.put("id", product.getID());
                                        customrequestParams.put("customdataclasspath", Constants.Acc_Product_custom_data_classpath);
                                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                            productRequestParams.put("accproductcustomdataref", product.getID());
                                            productObj = accProductDAO.updateProduct(productRequestParams);
                                        }
                                    }
                                    successRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\" Master License data imported successfully\"");
                                } else {
                                    failedCnt++;
                                    failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Product Not Exist - " + productID + "\"");
//                                msg += "\n---------------Product Not Exist - " + productID + "-------------------------------------";
//                                System.out.println("\n---------------Product Not Exist - " + productID + "-------------------------------------");
                                }
                            } catch (Exception ex) {
                                failedCnt++;
                                failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + ex.getMessage() + "\"");
                            }
                        } catch (Exception ex) {
                            failedCnt++;
                            failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + ex.getMessage() + "\"");
                        }
                    } else {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            if (cell != null) {
                                boolean allowData = true;
                                String cellHeaderName = cell.getStringCellValue();
                                cellHeaderName = cellHeaderName.replaceAll("[.]", " ");//As we cannot add a custom column with special characters
                                if (cellHeaderName.toLowerCase().contains("Material Number".toLowerCase()) && productIDIndex == -1) {
                                    productIDIndex = cellcount;
                                } else if (cellHeaderName.toLowerCase().contains("Master License type".toLowerCase()) && licenseTypeIndex == -1) {
                                    licenseTypeIndex = cellcount;
                                } else if (cellHeaderName.toLowerCase().contains("Master License".toLowerCase()) && licenseCodeIndex == -1) {
                                    licenseCodeIndex = cellcount;
                                } else if (cellHeaderName.toLowerCase().contains("Validity Start date".toLowerCase()) && licenseValiditySDIndex == -1) {
                                    licenseValiditySDIndex = cellcount;
                                } else if (cellHeaderName.toLowerCase().contains("Validity End date".toLowerCase()) && licenseValidityEDIndex == -1) {
                                    licenseValidityEDIndex = cellcount;
                                } else if (cellHeaderName.toLowerCase().contains("Lic status".toLowerCase()) && licenseStatusIndex == -1) {
                                    licenseStatusIndex = cellcount;
                                } else {
                                    allowData = false;
                                }
                                validCellIndex.add(Integer.valueOf(cellcount));
                                fileData.add(cellHeaderName);
                                headArrayList.add(cellHeaderName);
                                columnConfig.put(cellHeaderName, cellcount);
                            }
                        }
                        failedRecords.append(OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Error Message\"");
                        successRecords.append(OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Import Status\"");
                    }
                    cnt++;
                }
                // Move File to another location after file process
                OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, cnt, msg, processStartDate, total);
                Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "ImportMasterLicense completed", "");
            } else {
                System.out.println("---------------Company not exist with subdomain " + olympusSubdomain + " - ");
                System.out.println("or");
                System.out.println("---------------Problem in creating xls file.");
            }
            txnManager.commit(status);
            System.out.println("Completed at :"+ new Date());
        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            ex.printStackTrace();
            msg.append(ex.getMessage());
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            createMailContent(baseUrl, "License Master", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
        } finally {
            status = txnManager.getTransaction(def);
                try {
                     OlympusImportDataHandler.updateLogEntry(importDao, importLogID, destinationFileName, msg, total, failedCnt, company, issuccess, "30");
                    txnManager.commit(status);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            resultJObj.put("data", msg);
            resultJObj.put("failedCnt", failedCnt);
            resultJObj.put("total", total);
            resultJObj.put("processfilepath", processfilepath);
            resultJObj.put("xlsfilepath", filePath);
            resultJObj.put("failuefilepath", failuefilepath);
            createMailContent(baseUrl, "License Master", total, failedCnt, "", orgfile, fromEmailID, map, company);
            return resultJObj;
        }
    }

    @Override
    public JSONObject importSupplimenteryLicense(String baseUrl, String olympusSubdomain, boolean isSuppOneFile) throws JSONException {
        return importSupplimenteryLicense(null, baseUrl, olympusSubdomain, isSuppOneFile);
    }
    @Override
    public JSONObject importSupplimenteryLicense(String filePath, String baseUrl, String olympusSubdomain, boolean isSuppOneFile) throws JSONException {
        StringBuilder msg = new StringBuilder();
        boolean issuccess = true;
        StringBuilder destinationFileName = new StringBuilder();
        JSONObject resultJObj = new JSONObject();
        int cnt = 0, total = 0, TotalBatchCnt = Constants.Transaction_Commit_Limit_50, initBatchCnt = 0;
        String importLogID = "";
        int failedCnt = 0;
        String processfilepath="", failuefilepath = "", orgfile="";
        String toEmailID="", fromEmailID="";
        Company company =null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        Map<String,String> map = new HashMap<>();                                
        try {
            // As we need to store the Validity Start date, Validity End date, Lic.status from the master license files only
            List ignoreList = new ArrayList();
            ignoreList.add("Validity Start date");
            ignoreList.add("Validity End date");
            ignoreList.add("Lic status");
            
            company = getCompanyObject(olympusSubdomain);
            StringBuilder failedRecords = new StringBuilder();
            StringBuilder successRecords = new StringBuilder();
            List<Object> fileData = new ArrayList();
            List<Integer> validCellIndex = new ArrayList();
            String headerPrefix = "Supplementary";
            int noofcol=0;
            String scriptinfo = "select filepath,importinfo,noofcol from olympus_importscriptinfo where id = ? and subdomain = ?";
            String customLabelMasterIntLicenseNo = "";
//            String olympusSubdomain = !StringUtil.isNullOrEmpty(request.getParameter("cdomain"))?request.getParameter("cdomain"):"";
            int flag = isSuppOneFile ? 1 : 2;
            List scriptInfoList = null;
            if (isSuppOneFile) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.SUPPLLICENSE1.toString(), olympusSubdomain});
                //                filePath = "/Users/sagar/Desktop/License Sup 1_explaination.xls";
                headerPrefix += "1";
            } else {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.SUPPLLICENSE2.toString(), olympusSubdomain});
                //                filePath = "/Users/sagar/Desktop/License Sup 2_explaination.xls";
                headerPrefix += "2";
            }
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            /*
             * Finding Company from subdomain
             */
            JSONObject scriptInfoData=null;
            boolean flag1=false;
            String xlsfilepath="";
            if (scriptInfoList.size() > 0) {
                    Object[] row = (Object[]) scriptInfoList.get(0);
                    if(StringUtil.isNullOrEmpty(filePath)){
                    filePath = row[0].toString();
                    }
                    processfilepath = filePath;
                    orgfile = processfilepath.substring(processfilepath.lastIndexOf("/")+1);
                    noofcol = Integer.parseInt(row[2].toString());
                    if(!StringUtil.isNullOrEmpty(filePath)){
                        String ext = FilenameUtils.getExtension(filePath);
                        xlsfilepath = filePath.replace(ext, "xls");
                        long start = new Date().getTime();
                        flag1=convertToXLS(filePath, xlsfilepath, noofcol);
                        long end = new Date().getTime();
                        System.out.println("\nConvert file : "+(end-start)+" millisec .\n");
                        Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nConvert file : "+(end-start)+" millisec .\n", "");
                    }
                    if(!StringUtil.isNullOrEmpty(row[1].toString())){
                        scriptInfoData = new JSONObject();
                    }
            }
            
            if (company != null && flag1) {
                filePath=xlsfilepath;
                fromEmailID = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                scriptinfo = "select emailids from olympus_importinfo where subdomain = ?";
                List scriptEmailInfo = null;
                scriptEmailInfo = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{olympusSubdomain});
                if (scriptEmailInfo.size() > 0) {
                    toEmailID = scriptEmailInfo.get(0)!=null ? scriptEmailInfo.get(0).toString() :"";
                }
                if (scriptInfoData!=null) {
                    if (scriptInfoData.has("customLabelMasterIntLicenseNo")) {
                        customLabelMasterIntLicenseNo = scriptInfoData.getString("customLabelMasterIntLicenseNo");
                    }
                }
                HashMap<String, String> requestMailParams = new HashMap<>();
                requestMailParams.put("company", company.getCompanyID());
                requestMailParams.put("toEmailID", toEmailID);
                /**
                 * method used to get email IDs from companypreferences setting
                 *
                 */
                map = getAllMailsIDS(requestMailParams);
                Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "ImportSupplimenteryLicense "+flag+" started", "");
                POIFSFileSystem fs;
                
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                int sheetNo = 0;
                int maxCol = 0;
                int productIDIndex = -1;
                int licenseTypeIndex = -1;
                int licenseCodeIndex = -1;
                int licenseNoIndex = -1;
                int licenseValiditySDIndex = -1;
                int licenseValidityEDIndex = -1;
                int licenseStatusIndex = -1;
                Date processStartDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
                String customfield = "";
                HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
                List headArrayList = new ArrayList();
                DateFormat df = authHandler.getGlobalDateFormat();
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);
                total = sheet.getLastRowNum();
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                        OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, -1, msg, processStartDate, total);
                        failuefilepath = destinationFileName.toString();
                        ImportLog logObj = OlympusImportDataHandler.addInitialLogEntry(importDao, "", destinationFileName, msg, cnt, failedCnt, company, issuccess, "30", processStartDate);
                        importLogID = logObj.getId();
                    }
                    if(initBatchCnt>=TotalBatchCnt) {
                        initBatchCnt = 0;
                        System.out.println("LOG ENTRY - "+cnt);
                        OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, cnt, msg, processStartDate, total);
                        OlympusImportDataHandler.addInitialLogEntry(importDao, importLogID, destinationFileName, msg, cnt, failedCnt, null, issuccess, "30", null);
                        txnManager.commit(status);
                        status = txnManager.getTransaction(def);
                    }
                    initBatchCnt++; 
                    
                    String licenseType = "";
                    String productID = "";
                    String licenseCode = "";
                    String licenseNo = "";
                    String licenseValiditySD = "";
                    Date licenseValidityStartDate = null;
                    String licenseValidityED = "";
                    Date licenseValidityEndDate = null;
                    String licenseStatus = "";
                    if (cnt != 0) {
                        fileData.clear();
                        try {
                            for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                                HSSFCell cell = row.getCell(cellcount);
                                boolean allowData = true;
                                if (cell != null) {
                                    if (cellcount == productIDIndex) {
//                                        productID = getCellValue(cell);
                                        productID = cell != null ? org.apache.commons.lang.StringUtils.stripStart(getCellValue(cell),"0") : "";
                                    } else if (cellcount == licenseTypeIndex) {
                                        licenseType = getCellValue(cell);
                                    } else if (cellcount == licenseCodeIndex) {
                                        licenseCode = getCellValue(cell);
                                    } else if (cellcount == licenseNoIndex) {
                                        licenseNo = getCellValue(cell);
                                    } else if (cellcount == licenseValiditySDIndex) {
                                        licenseValiditySD = getCellValue(cell);
                                        try {
                                            licenseValidityStartDate = sdf.parse(licenseValiditySD);
                                        } catch (ParseException pex) {
                                            licenseValidityStartDate = sdf1.parse(licenseValiditySD);
                                        }
                                    } else if (cellcount == licenseValidityEDIndex) {
                                        licenseValidityED = getCellValue(cell);
                                        try {
                                            licenseValidityEndDate = sdf.parse(licenseValidityED);
                                        } catch (ParseException pex) {
                                            licenseValidityEndDate = sdf1.parse(licenseValidityED);
                                        }
                                    } else if (cellcount == licenseStatusIndex) {
                                        licenseStatus = getCellValue(cell);
                                    } else {
                                        allowData = false;
                                    }
                                }
                                if (validCellIndex.contains(Integer.valueOf(cellcount))) {
                                    if (cell != null) {
                                        fileData.add(getCellValue(cell));
                                    } else {
                                        fileData.add("");
                                    }
                                }
                            }
                            try {
                                filter_names.clear();
                                filter_params.clear();
                                KwlReturnObject productListObj = accProductDAO.getProduct(productID, company.getCompanyID());
                                List<Product> productList = productListObj.getEntityList();
                                if (productList.size() > 0) {
                                    Product product = productList.get(0);
                                    String findMasterLicenseEntry = "select * from olympus_licensesupplementary where productid=? and supptype= ?";
                                    List masterLicenseList = accCommonTablesDAO.executeSQLQuery(findMasterLicenseEntry, new Object[]{product.getProductid(), flag});
                                    if (masterLicenseList.size() == 0) {
                                        String insertmasterLicenseData = "INSERT into olympus_licensesupplementary(id, productid, licensetype, "
                                                + "licensecode, intlicenseno, vstartdate, venddate, licstatus,supptype)"
                                                + " values(?,?,?,?,?,?,?,?,?)";
                                        accCommonTablesDAO.executeSQLUpdate(insertmasterLicenseData, new Object[]{StringUtil.generateUUID(), product.getProductid(),
                                                    licenseType, licenseCode, licenseNo, licenseValidityStartDate, licenseValidityEndDate, licenseStatus, flag});
                                    } else {
                                        String updatemasterLicenseData = "UPDATE olympus_licensesupplementary set licensetype =?, "
                                                + "licensecode =? , vstartdate =? , venddate=? , licstatus=? where productid = ? and supptype = ? ";
                                        accCommonTablesDAO.executeSQLUpdate(updatemasterLicenseData, new Object[]{licenseType, licenseCode,
                                                    licenseValidityStartDate, licenseValidityEndDate, licenseStatus, product.getProductid(), flag});
                                    }

//                            msg += "\n---------------Product License updated - " + productID + "-------------------------------------";
//                            System.out.println("\n---------------Product License updated - " + productID + "-------------------------------------");
                                    Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nSr. No. " + i + "---Product License updated - " + productID + "---", "");
                                    // For creating custom field array
                                    customfield = "";
                                    JSONArray customJArr = new JSONArray();
                                    KwlReturnObject productObj = null;
                                    HashMap<String, Object> productRequestParams = new HashMap<String, Object>();
                                    for (int K = 0; K < headArrayList.size(); K++) {
                                        HashMap<String, Object> requestParamsCF = new HashMap<String, Object>();
                                        requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                        requestParamsCF.put(Constants.filter_values, Arrays.asList(company.getCompanyID(), Constants.Acc_Product_Master_ModuleId, headArrayList.get(K)));
                                        KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                        FieldParams params = null;
                                        if (fieldParamsResult.getRecordTotalCount() > 0) {
                                            params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                        }

                                        HSSFCell cell = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                        if (params != null && cell != null) {
                                            JSONObject customJObj = new JSONObject();
                                            customJObj.put("fieldid", params.getId());
                                            customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                            customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                            customJObj.put("xtype", params.getFieldtype());

                                            String fieldComboDataStr = "";
                                            if (params.getFieldtype() == 3) { // if field of date type
                                                HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                                String dateStr = cell1 != null ? getCellValue(cell1) : "";
                                                try {
                                                    customJObj.put("Col" + params.getColnum(), sdf.parse(dateStr).getTime());
                                                    customJObj.put("fieldDataVal", sdf.parse(dateStr).getTime());
                                                } catch (ParseException pex) {
                                                    customJObj.put("Col" + params.getColnum(), sdf1.parse(dateStr).getTime());
                                                    customJObj.put("fieldDataVal", sdf1.parse(dateStr).getTime());
                                                }
                                            } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                                HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                                String comboRecords = cell1 != null ? getCellValue(cell1) : "";
                                                String[] fieldComboDataArr = comboRecords.split(";");

                                                for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                                    requestParamsCF = new HashMap<String, Object>();
                                                    requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                                    requestParamsCF.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));

                                                    fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParamsCF);
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
                                                HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                                String checkbox = cell1 != null ? getCellValue(cell1) : "";
                                                customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(checkbox));
                                                customJObj.put("fieldDataVal", Boolean.parseBoolean(checkbox));
                                            } else if (params.getFieldtype() == 12) { // if field of check list type
                                                requestParamsCF = new HashMap<String, Object>();
                                                requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                                requestParamsCF.put(Constants.filter_values, Arrays.asList(params.getId(), 0));

                                                fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParamsCF);
                                                List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();
                                                HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                                String comboRecords = cell1 != null ? getCellValue(cell1) : "";

                                                String[] fieldComboDataArr = comboRecords.split(";");

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
                                                HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                                String record = cell1 != null ? getCellValue(cell1) : "";
                                                if(flag==2 && customLabelMasterIntLicenseNo.equals(params.getFieldlabel()) && product!=null){
                                                    KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), product.getID());
                                                    AccProductCustomData accProductCustomData = (AccProductCustomData) custumObjresult.getEntityList().get(0);
                                                    if (accProductCustomData != null) {
                                                        String coldata = accProductCustomData.getCol(params.getColnum());
                                                        if(StringUtil.isNullOrEmpty(coldata)) {
                                                            customJObj.put("Col" + params.getColnum(), record);
                                                            customJObj.put("fieldDataVal", record);
                                                        }else{
                                                            customJObj.put("Col" + params.getColnum(), coldata);
                                                            customJObj.put("fieldDataVal", coldata); 
                                                        }
                                                    }
                                                }else{
                                                    customJObj.put("Col" + params.getColnum(), record);
                                                    customJObj.put("fieldDataVal", record);
                                                }
                                            }

                                            customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                            customJArr.put(customJObj);
                                        }
                                    }

                                    customfield = customJArr.toString();
                                    if (product != null && !StringUtil.isNullOrEmpty(customfield)) {
                                        JSONArray jcustomarray = new JSONArray(customfield);
                                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                                        customrequestParams.put("customarray", jcustomarray);
                                        customrequestParams.put("modulename", Constants.Acc_Product_modulename);
                                        customrequestParams.put("moduleprimarykey", Constants.Acc_Productid);
                                        customrequestParams.put("modulerecid", product.getID());
                                        customrequestParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                                        customrequestParams.put("companyid", company.getCompanyID());
                                        productRequestParams.put("id", product.getID());
                                        customrequestParams.put("customdataclasspath", Constants.Acc_Product_custom_data_classpath);
                                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                            productRequestParams.put("accproductcustomdataref", product.getID());
                                            productObj = accProductDAO.updateProduct(productRequestParams);
                                        }
                                    }
                                    successRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + headerPrefix + " data imported successfully\"");
                                } else {
                                    failedCnt++;
                                    failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Product Not Exist - " + productID + "\"");
//                            msg += "\n---------------Product Not Exist - " + productID + "-------------------------------------";
//                            System.out.println("\n---------------Product Not Exist - " + productID + "-------------------------------------");
                                }
                            } catch (Exception ex) {
                                failedCnt++;
                                failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + ex.getMessage() + "\"");
                                }
                        } catch (Exception ex) {
                            failedCnt++;
                            failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + ex.getMessage() + "\"");
                            }
                    } else {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            if (cell != null) {
                                boolean allowData = true;
                                String cellHeaderName = cell.getStringCellValue();
                                cellHeaderName = cellHeaderName.replaceAll("[.]", " ");//As we cannot add a custom column with special characters
                                if (cellHeaderName.contains("Material Number") && productIDIndex == -1) {
                                    productIDIndex = cellcount;
                                } else if (cellHeaderName.contains(headerPrefix + " License type") && licenseTypeIndex == -1) {
                                    licenseTypeIndex = cellcount;
                                } else if (cellHeaderName.contains("License Code " + headerPrefix) && licenseTypeIndex == -1) {
                                    licenseCodeIndex = cellcount;
                                } else if (cellHeaderName.contains(headerPrefix + " Int License No ") && licenseCodeIndex == -1) {
                                    licenseNoIndex = cellcount;
                                } else if (cellHeaderName.contains("Validity Start date") && licenseValiditySDIndex == -1) {
                                    licenseValiditySDIndex = cellcount;
                                } else if (cellHeaderName.contains("Validity End date") && licenseValidityEDIndex == -1) {
                                    licenseValidityEDIndex = cellcount;
                                } else if (cellHeaderName.contains("Lic status") && licenseStatusIndex == -1) {
                                    licenseStatusIndex = cellcount;
                                } else {
                                    allowData = false;
                                }
                                
                                if (allowData) {
                                    validCellIndex.add(Integer.valueOf(cellcount));
                                    fileData.add(cellHeaderName);
                                }
                                if (!ignoreList.contains(cellHeaderName)) {
                                    headArrayList.add(cellHeaderName);
                                    columnConfig.put(cellHeaderName, cellcount);
                                    if (!allowData) {
                                        validCellIndex.add(Integer.valueOf(cellcount));
                                        fileData.add(cellHeaderName);
                                    }
                                }
                            }
                        }
                        failedRecords.append(OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Error Message\"");
                        successRecords.append(OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Import Status\"");
                    }
                    cnt++;
                }
                // Move File to another location after file process
                OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, cnt, msg, processStartDate, total);
                Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "ImportSupplimenteryLicense "+flag+" completed", "");
            } else {
                System.out.println("---------------Company not exist with subdomain " + olympusSubdomain + " - ");
            }
            txnManager.commit(status);
        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            ex.printStackTrace();
            msg.append(ex.getMessage());
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            
            if(isSuppOneFile){
                createMailContent(baseUrl, "License Master Supp. 1", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else{
                createMailContent(baseUrl, "License Master Supp. 2", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }
        } finally {
            status = txnManager.getTransaction(def);
                try {
                    OlympusImportDataHandler.updateLogEntry(importDao, importLogID, destinationFileName, msg, total, failedCnt, company, issuccess, "30");
                    txnManager.commit(status);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            resultJObj.put("data", msg);
            resultJObj.put("failedCnt", failedCnt);
            resultJObj.put("total", total);
            resultJObj.put("processfilepath", processfilepath);
            resultJObj.put("xlsfilepath", filePath);
            resultJObj.put("failuefilepath", failuefilepath);
            if(isSuppOneFile){
                createMailContent(baseUrl, "License Master Supp. 1", total, failedCnt, "", orgfile, fromEmailID, map, company);
            }else{
                createMailContent(baseUrl, "License Master Supp. 2", total, failedCnt, "", orgfile, fromEmailID, map, company);
            }
            return resultJObj;
        }
    }

    @Override
    public JSONObject importCustomer(String baseUrl, boolean isBillingAddress, String olympusSubdomain) throws JSONException {
        return importCustomer(null, baseUrl, isBillingAddress, olympusSubdomain);
    }
    @Override
    public JSONObject importCustomer(String filePath, String baseUrl, boolean isBillingAddress, String olympusSubdomain) throws JSONException {
        StringBuilder msg = new StringBuilder();
        boolean issuccess = true;
        StringBuilder destinationFileName = new StringBuilder();
        JSONObject resultJObj = new JSONObject();
        int cnt = 0, total = 0, TotalBatchCnt = Constants.Transaction_Commit_Limit_50, initBatchCnt = 0 ;
        int failedCnt = 0;
        String processfilepath="", failuefilepath = "", orgfile="";
        String toEmailID="", fromEmailID="";
        Company company =null;
        String importLogID = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IMPORTCUSTOMER_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        Map<String,String> map = new HashMap<>();                                
        try {
            company = getCompanyObject(olympusSubdomain);
//            boolean isBillingAddress = request.getParameter("SP") != null ? true : false;
            int noofcol=0;
            String scriptinfo = "select filepath,importinfo,noofcol from olympus_importscriptinfo where id = ? and subdomain = ?";
            String Trade_Debtors_AccountID = "";
            StringBuilder failedRecords = new StringBuilder();
            StringBuilder successRecords = new StringBuilder();
//            String olympusSubdomain = !StringUtil.isNullOrEmpty(request.getParameter("cdomain"))?request.getParameter("cdomain"):"";
            List scriptInfoList = null;
            if (isBillingAddress) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.CUSTOMERBILLINGMASTER.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/CustomerMaster-SP_Explaination.xls";
            } else {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.CUSTOMERSHIPPINGMASTER.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/CustomerMaster-SH_Explaination.xls";
            }
            HashMap<String, Object> requestParams = new HashMap();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();

            List<Object> fileData = new ArrayList();
            
            JSONObject scriptInfoData=null;
            boolean flag=false;
            String xlsfilepath="";
            if (scriptInfoList.size() > 0) {
                    Object[] row = (Object[]) scriptInfoList.get(0);
                    if(StringUtil.isNullOrEmpty(filePath)){
                    filePath = row[0].toString();
                    }
                    processfilepath = filePath;
                    orgfile = processfilepath.substring(processfilepath.lastIndexOf("/")+1);
                    noofcol = Integer.parseInt(row[2].toString());
                    if(!StringUtil.isNullOrEmpty(filePath)){
                        String ext = FilenameUtils.getExtension(filePath);
                        xlsfilepath = filePath.replace(ext, "xls");
                        long start = new Date().getTime();
                        flag=convertToXLS(filePath, xlsfilepath, noofcol);
                        long end = new Date().getTime();
                        System.out.println("\nConvert file : "+(end-start)+" millisec .\n");
                        Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nConvert file : "+(end-start)+" millisec .\n", "");
                    }
                    scriptInfoData = new JSONObject(row[1].toString());
            }
                
            if (company != null && flag) {
                filePath=xlsfilepath;
                fromEmailID = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                scriptinfo = "select emailids from olympus_importinfo where subdomain = ?";
                List scriptEmailInfo = null;
                scriptEmailInfo = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{olympusSubdomain});
                if (scriptEmailInfo.size() > 0) {
                    toEmailID = scriptEmailInfo.get(0)!=null ? scriptEmailInfo.get(0).toString() :"";
                }
                if (scriptInfoData!=null) {
                    if (scriptInfoData.has("tradedebtoraccount")) {
                        Trade_Debtors_AccountID = scriptInfoData.getString("tradedebtoraccount");
                    }
                }
                
                // Find out Term - 30 Days 
                int termDays = 30;
                HashMap<String, Object> termRequestParams = new HashMap<String, Object>();
                termRequestParams.put("companyid", company.getCompanyID());
                termRequestParams.put("termdays", termDays);
                KwlReturnObject termresult = accTermObj.getTerm(termRequestParams);
                String termId = "";
                if (termresult.getEntityList().size() > 0) {
                    List<Term> termList = termresult.getEntityList();
                    termId = termList.get(0).getID();
                }

                POIFSFileSystem fs;
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                int sheetNo = 0;
                int maxCol = 0;
                List<Integer> validCellIndex = new ArrayList();
                int customerIDIndex = -1;
                int customerNameIndex = -1;
                int customerAliasNameIndex = -1;
                int postalCodeIndex = -1;
                int cityIndex = -1;
                Date processStartDate = new Date();
                int countryIndex = -1;
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);
                List<Integer> billAddressIndex = new ArrayList<Integer>();
                List<Integer> shipAddressIndex = new ArrayList<Integer>();
                total = sheet.getLastRowNum();
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                        OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, -1, msg, processStartDate, total);
                        failuefilepath = destinationFileName.toString();
                        ImportLog logObj = OlympusImportDataHandler.addInitialLogEntry(importDao, "", destinationFileName, msg, cnt, failedCnt, company, issuccess, "09508488-c1d2-102d-b048-001e58a64cb6", processStartDate);
                        importLogID = logObj.getId();
                    }
                    if(initBatchCnt>=TotalBatchCnt) {
                        initBatchCnt = 0;
                        System.out.println("LOG ENTRY - "+cnt);
                        OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, cnt, msg, processStartDate, total);
                        OlympusImportDataHandler.addInitialLogEntry(importDao, importLogID, destinationFileName, msg, cnt, failedCnt, null, issuccess, "09508488-c1d2-102d-b048-001e58a64cb6", null);
                        txnManager.commit(status);
                        status = txnManager.getTransaction(def);
                    }
                    initBatchCnt++; 
                    String customerName = "";
                    String customerAliasName = "";
                    String customerID = "";
                    String billingAddress = "";
                    String shippingAddress = "";
                    String postalCode = "";
                    String city = "";
                    String country = "";
                    if (cnt != 0) {
                        fileData.clear();
                        try {
                            for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                                HSSFCell cell = row.getCell(cellcount);
                                boolean allowData = false;
                                if (cell != null) {
                                    allowData = true;
                                    if (cellcount == customerIDIndex) {
                                        customerID = getCellValue(cell);
                                    } else if (billAddressIndex.contains(cellcount)) {
                                        if (billingAddress.length() != 0) {
                                            billingAddress = billingAddress.concat("\n");
                                        }
                                        billingAddress = billingAddress.concat(getCellValue(cell));
                                    } else if (shipAddressIndex.contains(cellcount)) {
                                        if (shippingAddress.length() != 0) {
                                            shippingAddress = shippingAddress.concat("\n");
                                        }
                                        shippingAddress = shippingAddress.concat(getCellValue(cell));
                                    } else if (cellcount == customerNameIndex) {
                                        customerName = getCellValue(cell);
                                    } else if (cellcount == customerAliasNameIndex) {
                                        customerAliasName = getCellValue(cell);
                                    } else if (cellcount == postalCodeIndex) {
                                        postalCode = getCellValue(cell);
                                    } else if (cellcount == cityIndex) {
                                        city = getCellValue(cell);
                                    } else if (cellcount == countryIndex) {
                                        country = getCellValue(cell);
                                    } else {
                                        allowData = false;
                                    }
                                }
                                if (validCellIndex.contains(Integer.valueOf(cellcount))) {
                                    if (cell != null) {
                                        fileData.add(getCellValue(cell));
                                    } else {
                                        fileData.add("");
                                    }
                                }
                            }
                            filter_names.clear();
                            filter_params.clear();
                            filter_names.add("acccode");
                            filter_names.add("company.companyID");
                            filter_params.add(customerID);
                            filter_params.add(company.getCompanyID());
                            requestParams.put("filter_names", filter_names);
                            requestParams.put("filter_params", filter_params);
                            KwlReturnObject customerListObj = accCustomerDAOobj.getCustomerList(requestParams);
                            List<Customer> customerList = customerListObj.getEntityList();
                            HashMap<String, Object> custAddrMap = new HashMap<String, Object>();
                            Customer customer=null;
                            try {
                                if (customerList.size() > 0) {
                                    customer = customerList.get(0);                                   
                                    customer.setName(customerName);
                                    customer.setAliasname(customerAliasName);
                                    List customerUpdateList = new ArrayList();
                                    customerUpdateList.add(customer);
                                    accAccountDAOobj.saveOrUpdateAll(customerUpdateList);
                                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                                    addressParams.put("companyid", company.getCompanyID());
                                    addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress                                
                                    addressParams.put("customerid", customer.getID());
                                    CustomerAddressDetails customerAddressDetails = null;
                                    if (isBillingAddress) {
                                        addressParams.put("isBillingAddress", true);    //true to get billing address
                                        customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                                    } else {
                                        addressParams.put("isBillingAddress", false);    //false to get shipping address
                                        customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                                    }
                                    if (customerAddressDetails != null) {
                                        custAddrMap.put("addressid", customerAddressDetails.getID());
//                                        custAddrMap.put("aliasName", customerAddressDetails.getAliasName());
                                    }

                                    successRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + "Customer updated successfully" + "\"");

//                            msg += "\n---------------Customer updated - " + customerID + "-------------------------------------";
//                            System.out.println("\n---------------Customer updated - " + customerID + "-------------------------------------");
                                } else {
                                    HashMap<String, Object> customerRequestParams = new HashMap<String, Object>();
                                    String uuid = StringUtil.generateUUID();
                                    customerRequestParams.put("accid", uuid);
                                    customerRequestParams.put("acccode", customerID);
                                    customerRequestParams.put("accname", customerName);
                                    customerRequestParams.put("aliasname", customerAliasName);
                                    customerRequestParams.put("termid", termId);
                                    customerRequestParams.put("companyid", company.getCompanyID());
                                    customerRequestParams.put("creationDate", company.getCreatedOn());
                                    customerRequestParams.put("currencyid", company.getCurrency().getCurrencyID());
                                    customerRequestParams.put("accountid", Trade_Debtors_AccountID);
                                    KwlReturnObject result = accCustomerDAOobj.addCustomer(customerRequestParams);
                                    if (result.getEntityList().size() > 0) {
                                        customer = (Customer) result.getEntityList().get(0);
                                    }
                                    
                                    //check for "Main" warehouse

                                    KwlReturnObject prefRes = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), company.getCompanyID());
                                    ExtraCompanyPreferences companyPreferences = (ExtraCompanyPreferences) prefRes.getEntityList().get(0);
                                    String warehouseId = companyPreferences.getDefaultWarehouse();
                                    if (!StringUtil.isNullOrEmpty(warehouseId) && customer !=null) {
                                        accCustomerDAOobj.addCustomerWarehouseMapping(warehouseId, customer.getID(), false, null, true);
                                    } else {
                                        msg.append(" ,Customer Default WareHouse is not found");
                                    }
                                    HashMap<String, String> requestMailParams = new HashMap<>();
                                    requestMailParams.put("company", company.getCompanyID());
                                    requestMailParams.put("toEmailID", toEmailID);
                                    /**
                                     * method used to get email IDs from
                                     * companypreferences setting
                                     *
                                     */
                                    map = getAllMailsIDS(requestMailParams);
                                    successRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + "Customer added successfully" + "\"");
                                }
                                //save address detail after saving customer  
                                if (customer != null) {
                                    custAddrMap.put("customerid", customer.getID());
                                    custAddrMap.put("city", city);
                                    custAddrMap.put("country", country);
                                    custAddrMap.put("postalCode", postalCode);
                                    custAddrMap.put("isDefaultAddress", true);
                                    if (isBillingAddress) {
                                        custAddrMap.put("aliasName", !StringUtil.isNullOrEmpty(customerAliasName)?customerAliasName:"Billing Address1");
                                    } else {
                                        custAddrMap.put("aliasName", !StringUtil.isNullOrEmpty(customerAliasName)?customerAliasName:"Shipping Address1");
                                    }
                                    if (isBillingAddress) {
                                        custAddrMap.put("address", billingAddress);
                                        custAddrMap.put("isBillingAddress", true);
                                        KwlReturnObject custAddrobject = accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, company.getCompanyID());
                                    } else {
                                        custAddrMap.put("address", shippingAddress);
                                        custAddrMap.put("isBillingAddress", false);
                                        KwlReturnObject custAddrobject = accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, company.getCompanyID());
                                    }
                                }
                            } catch (Exception ex) {
                                failedCnt++;
                                failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + ex.getMessage() + "\"");
                            }
                        } catch (Exception ex) {
                            failedCnt++;
                            failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + ex.getMessage() + "\"");
                        }
                    } else {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);

                            if (cell != null) {
                                String cellHeaderName = cell.getStringCellValue();
//                                String deskeraFieldName = cellHeaderName;
                                boolean allowData = true;
                                if (cellHeaderName.contains("Name 2") || cellHeaderName.contains("Name3") || cellHeaderName.contains("House number and street") 
                                        || cellHeaderName.contains("Street 4") || cellHeaderName.contains("Street 5")) {
                                    if (isBillingAddress) {
                                        billAddressIndex.add(cellcount);
                                    } else {
                                        shipAddressIndex.add(cellcount);
                                    }
                                    
                                } else if (cellHeaderName.contains("Customer Number") && customerIDIndex == -1) {
                                    customerIDIndex = cellcount;
                                } else if (cellHeaderName.contains("Name 1") && customerNameIndex == -1) {
                                    customerNameIndex = cellcount;
                                } else if (cellHeaderName.contains("Search term") && customerAliasNameIndex == -1) {
                                    customerAliasNameIndex = cellcount;
                                } else if (cellHeaderName.contains("Postal Code") && postalCodeIndex == -1) {
                                    postalCodeIndex = cellcount;
                                } else if (cellHeaderName.contains("City") && cityIndex == -1) {
                                    cityIndex = cellcount;
                                } else if (cellHeaderName.contains("Country Key") && countryIndex == -1) {
                                    countryIndex = cellcount;
                                } else {
                                    allowData = false;
                                }
                                if (allowData) {
                                    validCellIndex.add(Integer.valueOf(cellcount));
                                    fileData.add(cellHeaderName);
                                }
                            }
                        }
                        failedRecords.append(OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Error Message\"");
                        successRecords.append(OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Import Status\"");
                    }
                    cnt++;
                }
                OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, cnt, msg, processStartDate, total);
            } else {
                System.out.println("---------------Company not exist with subdomain " + olympusSubdomain + " - ");
            }
            txnManager.commit(status);
        } catch (ServiceException ex) {
            issuccess = false;
            txnManager.rollback(status);
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg.append(ex.getMessage());
            
            if(isBillingAddress){
                createMailContent(baseUrl, "Customer Billing", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else{
                createMailContent(baseUrl, "Customer Shipping", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }
        } catch (IOException ex) {
            issuccess = false;
            txnManager.rollback(status);
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg.append(ex.getMessage());
            
            if(isBillingAddress){
                createMailContent(baseUrl, "Customer Billing", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else{
                createMailContent(baseUrl, "Customer Shipping", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }
        } catch (JSONException ex) {
            issuccess = false;
            txnManager.rollback(status);
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg.append(ex.getMessage());
            
            if(isBillingAddress){
                createMailContent(baseUrl, "Customer Billing", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else{
                createMailContent(baseUrl, "Customer Shipping", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }
        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg.append(ex.getMessage());
            
            if(isBillingAddress){
                createMailContent(baseUrl, "Customer Billing", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else{
                createMailContent(baseUrl, "Customer Shipping", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }
        } finally {
            status = txnManager.getTransaction(def);
                try {
                    OlympusImportDataHandler.updateLogEntry(importDao, importLogID, destinationFileName, msg, total, failedCnt, company, issuccess, "09508488-c1d2-102d-b048-001e58a64cb6");
                    txnManager.commit(status);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            resultJObj.put("data", msg);
            resultJObj.put("failedCnt", failedCnt);
            resultJObj.put("total", total);
            resultJObj.put("processfilepath", processfilepath);
            resultJObj.put("xlsfilepath", filePath);
            resultJObj.put("failuefilepath", failuefilepath);
            if(isBillingAddress){
                createMailContent(baseUrl, "Customer Billing", total, failedCnt, "", orgfile, fromEmailID, map, company);
            }else{
                createMailContent(baseUrl, "Customer Shipping", total, failedCnt, "", orgfile, fromEmailID, map, company);
            }
            return resultJObj;
        }
    }

    private String getCellValue(HSSFCell cell) {
        String cellval = "";
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                cellval = cell.getRichStringCellValue().getString();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    cellval = commonDF.format(cell.getDateCellValue());
                } else {
                    double value = cell.getNumericCellValue();
                    cellval = String.valueOf(value);
                    if(cellval.contains(".")){
                        if(value == Math.round(value)){
                            cellval = String.valueOf(Math.round(value));
                        }
                    }
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                cellval = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                break;
            default:
        }
        return cellval;
    }

    @Override
    public JSONObject importProducts(String baseUrl, String olympusSubdomain) throws JSONException {
        return importProducts(null, baseUrl, olympusSubdomain);
    }
    @Override
    public JSONObject importProducts(String filePath, String baseUrl, String olympusSubdomain) throws JSONException {
        StringBuilder msg = new StringBuilder();
        JSONObject resultJObj = new JSONObject();
        int count = 0;
        boolean issuccess = true;
        StringBuilder destinationFileName = new StringBuilder();
        int cnt = 0, total = 0, TotalBatchCnt = Constants.Transaction_Commit_Limit_50, initBatchCnt = 0;
        String importLogID = "";
        int failedCnt = 0;
        int batchSerialCnt = 0;
        String processfilepath="", failuefilepath = "", orgfile="";
        String toEmailID="", fromEmailID="",importEmailIDs="";//importEmailIDs:mail ids are set from companypreferences.
        Company company =null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        Map<String,String> map = new HashMap<>();                                
        try {
            company = getCompanyObject(olympusSubdomain);
            // Default Values
            String defaultProductType = "";
            boolean defaultIsBatchForProduct = false;
            boolean defaultIsSerialForProduct = false;
            boolean defaultIslocationforproduct = false;
            boolean defaultIswarehouseforproduct = false;
            String defaultAccountName = "";
            String defaultAccountID = "";
            String defaultInitialPurchasePrice = "";
            String defaultInitialSalesPrice = "";
            String defaultfrqIds = "";
            String blankUOM = "";
            String defaultCasingUOMValue = "";
            String defaultSerialNumber = "";//ZYBP = Check the box, Empty = Uncheck box
            String defaultBatch = "";//X = Check the box, Empty = Uncheck box
//            String olympusSubdomain = !StringUtil.isNullOrEmpty(request.getParameter("cdomain"))?request.getParameter("cdomain"):"";
            int noofcol=0;
            String scriptinfo = "select filepath,importinfo,noofcol from olympus_importscriptinfo where id = ? and subdomain = ?";
            List scriptInfoList = null;
            scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.PRODUCTMASTER.toString(), olympusSubdomain});
            List<Object> fileData = new ArrayList();
            List<Object> batchSerialfileData = new ArrayList();
            StringBuilder failedRecords = new StringBuilder();
            StringBuilder batchSerialRecords = new StringBuilder();
            StringBuilder successRecords = new StringBuilder();
            String companyid = "";
            JSONObject scriptInfoData=null;
            boolean flag=false;
            String xlsfilepath="";
            if (scriptInfoList.size() > 0) {
                Object[] row = (Object[]) scriptInfoList.get(0);
                if (StringUtil.isNullOrEmpty(filePath)) {
                filePath = row[0].toString();
                }
                processfilepath = filePath;
                orgfile = processfilepath.substring(processfilepath.lastIndexOf("/")+1);
                noofcol = Integer.parseInt(row[2].toString());
                if(!StringUtil.isNullOrEmpty(filePath)){
                    String ext = FilenameUtils.getExtension(filePath);
                    xlsfilepath = filePath.replace(ext, "xls");
                    long start = new Date().getTime();
                    flag=convertToXLS(filePath, xlsfilepath, noofcol);
                    long end = new Date().getTime();
                    System.out.println("\nConvert file : "+(end-start)+" millisec .\n");
                    Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nConvert file : "+(end-start)+" millisec .\n", "");
                }
                scriptInfoData = new JSONObject(row[1].toString());
            }
            
            if (company != null && flag) {
                KwlReturnObject prefRes = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), company.getCompanyID());
                ExtraCompanyPreferences companyPreferences = (ExtraCompanyPreferences) prefRes.getEntityList().get(0);
                String impEmailIds=companyPreferences.getUserEmails();//emails ids from user combo in companypreference
                importEmailIDs=companyPreferences.getSendImportMailTo();//emails ids from send a copy to text field in companypreferences
                
                filePath=xlsfilepath;
                companyid = company.getCompanyID();
                fromEmailID = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                scriptinfo = "select emailids from olympus_importinfo where subdomain = ?";
                List scriptEmailInfo = null;
                scriptEmailInfo = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{olympusSubdomain});
                if (scriptEmailInfo.size() > 0) {
                    toEmailID = scriptEmailInfo.get(0)!=null ? scriptEmailInfo.get(0).toString() :"";
                }
                HashMap<String, String> requestMailParams = new HashMap<>();
                requestMailParams.put("company", company.getCompanyID());
                requestMailParams.put("toEmailID", toEmailID);
                /**
                 * method used to get email IDs from companypreferences setting
                 *
                 */
                map = getAllMailsIDS(requestMailParams);
                if (scriptInfoData!=null) {
                    if (scriptInfoData.has("producttype")) {
                        defaultProductType = scriptInfoData.getString("producttype");
                    }
//                    if (scriptInfoData.has("isbatchforproduct")) {
//                        defaultIsBatchForProduct = scriptInfoData.getBoolean("isbatchforproduct");
//                    }
//                    if (scriptInfoData.has("isserialforproduct")) {
//                        defaultIsSerialForProduct = scriptInfoData.getBoolean("isserialforproduct");
//                    }
                    if (scriptInfoData.has("islocationforproduct")) {
                        defaultIslocationforproduct = scriptInfoData.getBoolean("islocationforproduct");
                    }
                    if (scriptInfoData.has("iswarehouseforproduct")) {
                        defaultIswarehouseforproduct = scriptInfoData.getBoolean("iswarehouseforproduct");
                    }
                    if (scriptInfoData.has("account")) {
                        defaultAccountID = scriptInfoData.getString("account");//account is on company level
                    }
                    if (scriptInfoData.has("initialpurchaseprice")) {
                        defaultInitialPurchasePrice = scriptInfoData.getString("initialpurchaseprice");
                    }
                    if (scriptInfoData.has("initialsalesirice")) {
                        defaultInitialSalesPrice = scriptInfoData.getString("initialsalesirice");
                    }
                    if (scriptInfoData.has("frequency")) {
                        defaultfrqIds = scriptInfoData.getString("frequency");
                    }
                    if (scriptInfoData.has("blankUOM")) {
                        blankUOM = scriptInfoData.getString("blankUOM");
                    }
                    if (scriptInfoData.has("casingUOMvalue")) {
                        defaultCasingUOMValue = scriptInfoData.getString("casingUOMvalue");
                    }
                    if (scriptInfoData.has("defaultSerialNumber")) {
                        defaultSerialNumber = scriptInfoData.getString("defaultSerialNumber");
                    }
                    if (scriptInfoData.has("defaultBatch")) {
                        defaultBatch = scriptInfoData.getString("defaultBatch");
                    }
                }
                Set<Frequency> frequencys = new HashSet();
                if (!StringUtil.isNullOrEmpty(defaultfrqIds)) {
                    String[] fArray = defaultfrqIds.split(",");
                    for (String fId : fArray) {
                        Integer freqid = Integer.parseInt(fId);
                        KwlReturnObject jeresult = accProductDAO.getObject(Frequency.class.getName(), freqid);
                        Frequency frequency = (Frequency) jeresult.getEntityList().get(0);
                        frequencys.add(frequency);
                    }
                }
                
                // creating Price list Hashmap
                HashMap<String, Object> initialPurchasePriceMap = new HashMap<String, Object>();
                if (defaultInitialPurchasePrice.length() > 0) {
//                    initialPurchasePriceMap.put("productid", p.getID());
                    initialPurchasePriceMap.put("companyid", companyid);
                    initialPurchasePriceMap.put("carryin", true);
                    initialPurchasePriceMap.put("price", Double.parseDouble(defaultInitialPurchasePrice));
                    initialPurchasePriceMap.put("applydate", new Date());
                    initialPurchasePriceMap.put("affecteduser", "-1");
                    initialPurchasePriceMap.put("currencyid", company != null ? company.getCurrency().getCurrencyID() : "");
//                    accProductDAO.addPriceList(initialPurchasePriceMap);
                }
                HashMap<String, Object> salesPriceMap = new HashMap<String, Object>();
                if (defaultInitialSalesPrice.length() > 0) {
    //                salesPriceMap.put("productid", p.getID());
                    salesPriceMap.put("companyid", companyid);
                    salesPriceMap.put("carryin", false);
                    salesPriceMap.put("price", Double.parseDouble(defaultInitialSalesPrice));
                    salesPriceMap.put("applydate", new Date());
                    salesPriceMap.put("affecteduser", "-1");
                    salesPriceMap.put("currencyid", company != null ? company.getCurrency().getCurrencyID() : "");
                }
                
                POIFSFileSystem fs;
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                int sheetNo = 0;
                int maxCol = 0;
                int materialNumberIndex = -1;
                int charecteristicValueIndex = -1;
                int materialDescriptionIndex = -1;
                int stockUOMIndex = -1;
                int numeratorstockUOMIndex = -1;
                int innerUOMIndex = -1;
                int numeratorinnerUOMIndex = -1;
                int packagingUOMIndex = -1;
                int HSCodeIndex = -1;
                int serialNumberIndex = -1;
                int batchIndex = -1;
                DateFormat df = authHandler.getGlobalDateFormat();
                Date processStartDate = new Date();

                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);
                HashMap<String,FieldParams> customFieldParamMap = new HashMap<String, FieldParams>();
                HashMap<String,String> UOMIDMap = new HashMap<String, String>();
                HashMap<String,HashMap<String, String>> customFieldComboDataList = new HashMap<String,HashMap<String, String>>();
                HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
                List headArrayList = new ArrayList();
                String customfield = "";
                List<Integer> validCellIndex = new ArrayList();
                total = sheet.getLastRowNum();
                long st=0, et=0, totMillisecRow=0, totMillisecSave=0;
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                        OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, -1, msg, processStartDate, total);
                        failuefilepath = destinationFileName.toString();
                        ImportLog logObj = OlympusImportDataHandler.addInitialLogEntry(importDao, "", destinationFileName, msg, cnt, failedCnt, company, issuccess, "30", processStartDate);
                        importLogID = logObj.getId();
                    }
                    if(initBatchCnt>=TotalBatchCnt) {
                        initBatchCnt = 0;
                        System.out.println("LOG ENTRY - "+cnt);
                        OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, cnt, msg, processStartDate, total);
                        OlympusImportDataHandler.addInitialLogEntry(importDao, importLogID, destinationFileName, msg, cnt, failedCnt, null, issuccess, "30", null);
                        txnManager.commit(status);
                        status = txnManager.getTransaction(def);
                    }
                    initBatchCnt++; 
                    
                    String productID = "";
                    String productName = "";
                    String description = "";
                    String casingUOMValue = defaultCasingUOMValue;
                    String innerUOMValue = "";
                    String stockUOMValue = "";
                    String UOMID = "";
                    String innerUOMID = "";
                    String casingUOMID = "";
                    UnitOfMeasure stockuom = null;
                    UnitOfMeasure inneruom = null;
                    UnitOfMeasure casinguom = null;
                    String stockUOM = "";
                    String innerUOM = "";
                    String casingUOM = "";
                    String HSCode = "";
                    defaultIsSerialForProduct = false;
                    defaultIsBatchForProduct = false;
                    String serialNumber = "";
                    String batch = "";

                    if (cnt != 0) {
                        fileData.clear();
                        batchSerialfileData.clear();
                        try {
                            if (customFieldParamMap.size() == 0 && cnt == 1) {
                                for (int K = 0; K < headArrayList.size(); K++) {
                                    HashMap<String, Object> requestParamsCF = new HashMap<String, Object>();
                                    requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                    requestParamsCF.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, headArrayList.get(K)));
                                    KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                    FieldParams params = null;
                                    if (fieldParamsResult.getRecordTotalCount() > 0) {
                                        params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                        customFieldParamMap.put(headArrayList.get(K).toString(), params);
                                    }
                                }
                            }
                            st = new Date().getTime();
                            for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                                HSSFCell cell = row.getCell(cellcount);
                                boolean allowData = true;
                                if (cellcount == materialNumberIndex) {
                                    productID = cell != null ? org.apache.commons.lang.StringUtils.stripStart(getCellValue(cell),"0") : "";
                                } else if (cellcount == charecteristicValueIndex) {
                                    HSSFCell cellProdId = row.getCell(materialNumberIndex);
                                    HSSFCell cellProdDesc = row.getCell(materialDescriptionIndex);
                                    String prodId = cellProdId != null ? getCellValue(cellProdId) : "";
                                    String prodDesc = cellProdDesc != null ? getCellValue(cellProdDesc) : "";
                                    if (!StringUtil.isNullOrEmpty(prodDesc) && prodDesc.startsWith("99 ")) {
                                        prodDesc = prodDesc.replaceFirst("99 ", "");
                                    }
                                    productName = prodId + "_" + prodDesc;
                                } else if (cellcount == materialDescriptionIndex) {
                                    description = cell != null ? getCellValue(cell) : "";
                                    if (!StringUtil.isNullOrEmpty(description) && description.startsWith("99 ")) {
                                        description = description.replaceFirst("99 ", "");
                                    }
                                } else if (cellcount == stockUOMIndex) {
                                    stockUOM = cell != null ? getCellValue(cell) : "";
                                    if (StringUtil.isNullOrEmpty(stockUOM)) {
                                        stockUOM = blankUOM;
                                    }
                                    if (UOMIDMap.containsKey(stockUOM)) {
                                        UOMID = UOMIDMap.get(stockUOM);
                                        KwlReturnObject uomObj = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), UOMID);
                                        stockuom = (UnitOfMeasure) uomObj.getEntityList().get(0);
                                    } else {
                                            stockuom = getUOMByName(stockUOM, companyid);
                                            if (stockuom != null) {
                                                UOMID = stockuom.getID();
                                            }
                                            UOMIDMap.put(stockUOM, UOMID);
                                        }
                                } else if (cellcount == numeratorstockUOMIndex) {
                                    stockUOMValue = cell != null ? getCellValue(cell) : "";
                                } else if (cellcount == innerUOMIndex) {
                                    innerUOM = cell != null ? getCellValue(cell) : "";
                                    if (StringUtil.isNullOrEmpty(innerUOM)) {
                                        innerUOM = blankUOM;
                                    }
                                    if (UOMIDMap.containsKey(innerUOM)) {
                                        innerUOMID = UOMIDMap.get(innerUOM);
                                        KwlReturnObject uomObj = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), innerUOMID);
                                        inneruom = (UnitOfMeasure) uomObj.getEntityList().get(0);
                                    } else {
                                            inneruom = getUOMByName(innerUOM, companyid);
                                            if (inneruom != null) {
                                                innerUOMID = inneruom.getID();
                                            }
                                            UOMIDMap.put(innerUOM, innerUOMID);
                                        }
                                } else if (cellcount == numeratorinnerUOMIndex) {
                                    innerUOMValue = cell != null ? getCellValue(cell) : "";
                                } else if (cellcount == packagingUOMIndex) {
                                    casingUOM = cell != null ? getCellValue(cell) : "";
                                    if (StringUtil.isNullOrEmpty(casingUOM) || stockUOM.equals(casingUOM)) {
                                        casingUOM = blankUOM;
                                        casingUOMValue = "0";
                                    }
                                    if (UOMIDMap.containsKey(casingUOM)) {
                                        casingUOMID = UOMIDMap.get(casingUOM);
                                        KwlReturnObject uomObj = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), casingUOMID);
                                        casinguom = (UnitOfMeasure) uomObj.getEntityList().get(0);
                                    } else {
                                            casinguom = getUOMByName(casingUOM, companyid);
                                            if (casinguom != null) {
                                                casingUOMID = casinguom.getID();
                                            }
                                            UOMIDMap.put(casingUOM, casingUOMID);
                                        }
                                } else if (cellcount == HSCodeIndex) {
                                    HSCode = cell != null ? getCellValue(cell) : "";
                                } else if (cellcount == serialNumberIndex) {
                                    serialNumber = cell != null ? getCellValue(cell) : "";
                                    if(!StringUtil.isNullOrEmpty(serialNumber) && serialNumber.equals(defaultSerialNumber)){
                                        defaultIsSerialForProduct = true;
                                    }else{
                                        defaultIsSerialForProduct = false;
                                    }
                                } else if (cellcount == batchIndex) {
                                    batch = cell != null ? getCellValue(cell) : "";
                                    if(!StringUtil.isNullOrEmpty(batch) && batch.equals(defaultBatch)){
                                        defaultIsBatchForProduct = true;
                                    }else{
                                        defaultIsBatchForProduct = false;
                                    }
                                } else {
                                    allowData = false;
                                }

                                if (validCellIndex.contains(Integer.valueOf(cellcount))) {
                                    if (cell != null) {
                                        fileData.add(getCellValue(cell));
                                    } else {
                                        fileData.add("");
                                    }
                                }
                            }
                            et=new Date().getTime();
                            totMillisecRow += (et-st);
                            System.out.println("\nRead row no. "+i+" from file : "+(et-st)+" millisec .\n");
                            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nRead row no. "+i+" from file : "+(et-st)+" millisec .\n", "");
                            try {
                                st=new Date().getTime();
                                HashMap<String, Object> productRequestParams = new HashMap<String, Object>();
                                productRequestParams.put("producttype", defaultProductType);
                                productRequestParams.put("productid", productID);
                                productRequestParams.put("isActiveItem", true);
                             
                                ItemReusability ir = ItemReusability.REUSABLE;
                                productRequestParams.put("isreusable",ir);
                             
                                if(!StringUtil.isNullOrEmpty(productName)){
                                    productRequestParams.put("name", productName);
                                }
                                productRequestParams.put("companyid", companyid);
                                productRequestParams.put("currencyid", company != null ? company.getCurrency().getCurrencyID() : "");
                                if(!StringUtil.isNullOrEmpty(description)){
                                    productRequestParams.put("desc", description);
                                }
                                productRequestParams.put("syncable", false);
                                productRequestParams.put("multiuom", false);
                                if(!StringUtil.isNullOrEmpty(batch)){
                                    productRequestParams.put("isBatchForProduct", defaultIsBatchForProduct);
                                }
                                if(!StringUtil.isNullOrEmpty(serialNumber)){
                                    productRequestParams.put("isSerialForProduct", defaultIsSerialForProduct);
                                }
                                productRequestParams.put("isLocationForProduct", defaultIslocationforproduct);
                                productRequestParams.put("isWarehouseForProduct", defaultIswarehouseforproduct);
                                Packaging packaging = null;
                                packaging = new Packaging();
                                packaging.setCasingUoM(casinguom);
                                packaging.setCasingUomValue(Double.parseDouble(casingUOMValue));

                                if(!StringUtil.isNullOrEmpty(defaultAccountID)){
                                    productRequestParams.put("purchaseaccountid", defaultAccountID);
                                    productRequestParams.put("purchaseretaccountid", defaultAccountID);
                                    productRequestParams.put("salesaccountid", defaultAccountID);
                                    productRequestParams.put("salesretaccountid", defaultAccountID);
                                }
                                if (!StringUtil.isNullOrEmpty(defaultfrqIds)) {
                                    productRequestParams.put("CCFrequency", frequencys);
                                }
                                if(!StringUtil.isNullOrEmpty(HSCode)){
                                    productRequestParams.put("hsCode", HSCode);
                                }
                                boolean isProductAdded = false;
                                KwlReturnObject productListObj = accProductDAO.getProductByProdID(productID, companyid,false);
                                List<Product> productList = productListObj.getEntityList();
                                KwlReturnObject productObj = null;
                                if (productList.size() > 0) {
                                    Product product = productList.get(0);
                                    productRequestParams.put("id", product.getID());
                                    HashMap<String,Object> reqParams=new HashMap<String,Object>();
                                    reqParams.put("unBuild",false);
                                    List listObj = accProductModuleService.isProductUsedintransction(product.getID(), companyid, reqParams); //if product used in transaction do not update isserial and isbatch
                                    boolean isUsedInTransaction = (Boolean) listObj.get(0);    //always boolean value
                                    if (isUsedInTransaction || product.getAvailableQuantity()>0) {
                                       productRequestParams.remove("isBatchForProduct");
                                       productRequestParams.remove("isSerialForProduct");
                                    }
                                    if((product.isIsBatchForProduct()&&!defaultIsBatchForProduct)||(!product.isIsBatchForProduct()&&defaultIsBatchForProduct)){
                                        batchSerialCnt++;
                                        batchSerialfileData.add(product.getProductid());
                                        batchSerialRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(batchSerialfileData.toArray(new Object[batchSerialfileData.size()])) + "\" Batch not updated \"");
                                    }
                                    if((product.isIsSerialForProduct()&&!defaultIsSerialForProduct)||(!product.isIsSerialForProduct()&&defaultIsSerialForProduct)){
                                       batchSerialCnt++;
                                        batchSerialfileData.add(product.getProductid());
                                        batchSerialRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(batchSerialfileData.toArray(new Object[batchSerialfileData.size()])) + "\" serial not updated \"");
                                    }
                                    if(!stockUOM.equals(blankUOM)){
                                        productRequestParams.put("uomid", UOMID);
                                    }
                                    if (stockuom != null) {
                                        if(!stockUOM.equals(blankUOM)){
                                            productRequestParams.put("purchaseuomid", stockuom);
                                            productRequestParams.put("salesuomid", stockuom);
                                            productRequestParams.put("transferUoM", stockuom);
                                            productRequestParams.put("orderUoM", stockuom);
                                        }
                                        try {
                                            if(!innerUOM.equals(blankUOM)){
                                                packaging.setInnerUoM(inneruom);
                                                packaging.setInnerUomValue(Double.parseDouble(innerUOMValue));
                                            }else if(product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null){
                                                packaging.setInnerUoM(product.getPackaging().getInnerUoM());
                                                packaging.setInnerUomValue(product.getPackaging().getInnerUomValue());
                                            }
                                            if(!stockUOM.equals(blankUOM)){
                                                packaging.setStockUoM(stockuom);
                                                packaging.setStockUomValue(Double.parseDouble(stockUOMValue));
                                            }else if(product.getPackaging()!=null && product.getPackaging().getStockUoM()!=null){
                                                packaging.setStockUoM(product.getPackaging().getStockUoM());
                                                packaging.setStockUomValue(product.getPackaging().getStockUomValue());
                                            }
                                        } catch (NumberFormatException ex) {
                                            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "NumberFormatException for product id : " + productID + " | product name : " + productName, ex);
                                            failedCnt++;
                                            failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + ex.getMessage() + "\"");
                                            continue;
                                        }
                                        packaging.setCompany(company);
                                    }
                                    if (packaging != null) {
                                        accProductDAO.saveProductPackging(packaging);
                                        productRequestParams.put("packaging", packaging);
                                    }
                                                                        
                                    productObj = accProductDAO.updateProduct(productRequestParams);
                                    System.out.println("\n---------------Product ID updated - " + productID + "-------------------------------------");
                                } else {
                                    isProductAdded = true;
                                    
                                    productRequestParams.put("uomid", UOMID);
                                    if (stockuom != null) {
                                        productRequestParams.put("purchaseuomid", stockuom);
                                        productRequestParams.put("salesuomid", stockuom);
                                        productRequestParams.put("transferUoM", stockuom);
                                        productRequestParams.put("orderUoM", stockuom);
                                        try {
                                            packaging.setInnerUoM(inneruom);
                                            packaging.setInnerUomValue(Double.parseDouble(innerUOMValue));
                                            packaging.setStockUoM(stockuom);
                                            packaging.setStockUomValue(Double.parseDouble(stockUOMValue));
                                        } catch (NumberFormatException ex) {
                                            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "NumberFormatException for product id : " + productID + " | product name : " + productName, ex);
                                            failedCnt++;
                                            failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + ex.getMessage() + "\"");
                                            continue;
                                        }
                                        packaging.setCompany(company);
                                    }else{
                                            failedCnt++;
                                            String message="Stock UOM is not set for product id : "+ productID + " | product name : " + productName;
                                            failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + message + "\"");
                                            continue;
                                      
                                    }
                                    if (packaging != null) {
                                        accProductDAO.saveProductPackging(packaging);
                                        productRequestParams.put("packaging", packaging);
                                    }
                                    
                                    productObj = accProductDAO.addProduct(productRequestParams);
                                    System.out.println("\n---------------Product ID added - " + productID + "-------------------------------------");
                                }
                                Product p = (Product) productObj.getEntityList().get(0);
                                Date ondate = new Date();
                                if (p != null && defaultInitialPurchasePrice.length() > 0) {
                                    // creating Price list Hashmap
//                                HashMap<String, Object> initialPurchasePriceMap = new HashMap<String, Object>();
                                    initialPurchasePriceMap.put("productid", p.getID());
//                                initialPurchasePriceMap.put("companyid", companyid);
//                                initialPurchasePriceMap.put("carryin", true);
//                                initialPurchasePriceMap.put("price", Double.parseDouble(defaultInitialPurchasePrice));
//                                initialPurchasePriceMap.put("applydate", ondate);
//                                initialPurchasePriceMap.put("affecteduser", "-1");
//                                initialPurchasePriceMap.put("currencyid", company != null ? company.getCurrency().getCurrencyID() : "");
                                    accProductDAO.addPriceList(initialPurchasePriceMap);
                                }
                                if (p != null && defaultInitialSalesPrice.length() > 0) {
                                    // creating Price list Hashmap
//                                HashMap<String, Object> salesPriceMap = new HashMap<String, Object>();
                                    salesPriceMap.put("productid", p.getID());
//                                salesPriceMap.put("companyid", companyid);
//                                salesPriceMap.put("carryin", false);
//                                salesPriceMap.put("price", Double.parseDouble(defaultInitialSalesPrice));
//                                salesPriceMap.put("applydate", ondate);
//                                salesPriceMap.put("affecteduser", "-1");
//                                salesPriceMap.put("currencyid", company != null ? company.getCurrency().getCurrencyID() : "");
                                    accProductDAO.addPriceList(salesPriceMap);
                                }

                                // For creating custom field array
                                customfield = "";
                                boolean addFlag = true;
                                JSONArray customJArr = new JSONArray();
                                for (int K = 0; K < headArrayList.size(); K++) {
                                    addFlag = true;
                                    HashMap<String, Object> requestParamsCF = new HashMap<String, Object>();
//                                requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
//                                requestParamsCF.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, headArrayList.get(K)));
//                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                    FieldParams params = null;
                                    if (customFieldParamMap.containsKey(headArrayList.get(K).toString())) {
                                        params = (FieldParams) customFieldParamMap.get(headArrayList.get(K).toString());
                                    }

                                    HSSFCell cell = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                    if (params != null && cell != null) {
                                        JSONObject customJObj = new JSONObject();
                                        customJObj.put("fieldid", params.getId());
                                        customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                        customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                        customJObj.put("xtype", params.getFieldtype());

                                        String fieldComboDataStr = "";
                                        if (params.getFieldtype() == 3) { // if field of date type
                                            HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                            String dateStr = cell1 != null ? getCellValue(cell1) : "";
                                            if(StringUtil.isNullOrEmpty(dateStr)){
                                                addFlag = false;
                                            }
                                            customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                                            customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
                                        } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                            HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                            String comboRecords = cell1 != null ? getCellValue(cell1) : "";
                                            if(StringUtil.isNullOrEmpty(comboRecords)){
                                                addFlag = false;
                                            }
                                            String[] fieldComboDataArr = comboRecords.split(";");

                                            for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                                if (customFieldComboDataList.containsKey(params.getId()) && customFieldComboDataList.get(params.getId()).containsKey(fieldComboDataArr[dataArrIndex])) {
                                                    String comboRecID = customFieldComboDataList.get(params.getId()).get(fieldComboDataArr[dataArrIndex]).toString();
                                                    if (!StringUtil.isNullOrEmpty(comboRecID)) {
                                                        fieldComboDataStr += comboRecID + ",";
                                                    }
                                                } else {
                                                    requestParamsCF = new HashMap<String, Object>();
                                                    requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                                    requestParamsCF.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));
                                                    String comboRecID = "";
                                                    KwlReturnObject fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParamsCF);
                                                    if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                        FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                        comboRecID = fieldComboData.getId();
                                                    }
                                                    HashMap<String, String> tempComboDataMap = new HashMap<String, String>();
                                                    if (customFieldComboDataList.containsKey(params.getId())) {
                                                        tempComboDataMap = customFieldComboDataList.get(params.getId());
                                                    }
                                                    tempComboDataMap.put(fieldComboDataArr[dataArrIndex], comboRecID);
                                                    customFieldComboDataList.put(params.getId(), tempComboDataMap);
                                                }
                                            }

                                            if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                                customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                                customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            } else {
                                                continue;
                                            }
                                    } 
                                    /*
                                        For Olympus no field with check box type. So commenting code.
                                    */
//                                    else if (params.getFieldtype() == 11) { // if field of check box type 
//                                        HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
//                                        String checkbox = cell1 != null ? getCellValue(cell1) : "";
//                                        customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(checkbox));
//                                        customJObj.put("fieldDataVal", Boolean.parseBoolean(checkbox));
//                                    } 
/*
                                        For Olympus no field with checklist type. So commenting code.
                                    */
//                                    else if (params.getFieldtype() == 12) { // if field of check list type
//                                        requestParamsCF = new HashMap<String, Object>();
//                                        requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
//                                        requestParamsCF.put(Constants.filter_values, Arrays.asList(params.getId(), 0));
//
//                                        KwlReturnObject fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParamsCF);
//                                        List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();
//                                        HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
//                                        String comboRecords = cell1 != null ? getCellValue(cell1) : "";
//
//                                        String[] fieldComboDataArr = comboRecords.split(";");
//
//                                        int dataArrIndex = 0;
//
//                                        for (FieldComboData fieldComboData : fieldComboDataList) {
//                                            if (fieldComboDataArr.length != dataArrIndex && fieldComboDataArr[dataArrIndex] != null && fieldComboDataArr[dataArrIndex].replaceAll("\"", "").trim().equalsIgnoreCase("true")) {
//                                                fieldComboDataStr += fieldComboData.getId() + ",";
//                                            }
//                                            dataArrIndex++;
//                                        }
//
//                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
//                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
//                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
//                                        } else {
//                                            continue;
//                                        }
//                                    } 
                                        else {
                                            HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                            String record = cell1 != null ? getCellValue(cell1) : "";
                                            if(StringUtil.isNullOrEmpty(record)){
                                                addFlag = false;
                                            }
                                            customJObj.put("Col" + params.getColnum(), record);
                                            customJObj.put("fieldDataVal", record);
                                        }

                                        customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                        if(addFlag){
                                            customJArr.put(customJObj);
                                        }
                                    }
                                }

                                customfield = customJArr.toString();
                                if (p != null && !StringUtil.isNullOrEmpty(customfield)) {
                                    JSONArray jcustomarray = new JSONArray(customfield);
                                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                                    customrequestParams.put("customarray", jcustomarray);
                                    customrequestParams.put("modulename", Constants.Acc_Product_modulename);
                                    customrequestParams.put("moduleprimarykey", Constants.Acc_Productid);
                                    customrequestParams.put("modulerecid", p.getID());
                                    customrequestParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                                    customrequestParams.put("companyid", companyid);
                                    productRequestParams.put("id", p.getID());
                                    customrequestParams.put("customdataclasspath", Constants.Acc_Product_custom_data_classpath);
                                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                        productRequestParams.put("accproductcustomdataref", p.getID());
                                        productObj = accProductDAO.updateProduct(productRequestParams);
                                    }
                                }
                                if (isProductAdded) {
                                    successRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + "Product added successfully" + "\"");
                                } else {
                                    successRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + "Product updated successfully" + "\"");
                                }
                                
                                et=new Date().getTime();
                                totMillisecSave += (et-st);
                                System.out.println("\nSave : "+(et-st)+" millisec .\n");
                                Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nSave : "+(et-st)+" millisec .\n", "");
                                System.out.println("\nTotal time to save data : "+totMillisecSave+" millisec .");
                                Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nTotal time to save data : "+totMillisecSave+" millisec .", "");
                            } catch (Exception ex) {
                                failedCnt++;
                                failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + ex.getMessage() + "\"");
                            }
                        } catch (Exception ex) {
                            failedCnt++;
                            failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + ex.getMessage() + "\"");
                            
                        }
                    } else {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);

                            if (cell != null) {
                                boolean allowData = true;
                                String cellHeaderName = getCellValue(cell);
                                if (cellHeaderName.contains("Material Number") && materialNumberIndex == -1) {
                                    materialNumberIndex = cellcount;
                                } else if (cellHeaderName.contains("Charecteristic value") && charecteristicValueIndex == -1) {
                                    charecteristicValueIndex = cellcount;
                                } else if (cellHeaderName.contains("Material Description") && materialDescriptionIndex == -1) {
                                    materialDescriptionIndex = cellcount;
                                } else if (cellHeaderName.contains("UOM") && stockUOMIndex == -1) {
                                    stockUOMIndex = cellcount;
                                } else if (cellHeaderName.contains("Numerator") && numeratorstockUOMIndex == -1) {
                                    numeratorstockUOMIndex = cellcount;
                                } else if (cellHeaderName.contains("AUOM1") && innerUOMIndex == -1) {
                                    innerUOMIndex = cellcount;
                                } else if (cellHeaderName.contains("Numerator") && numeratorinnerUOMIndex == -1) {
                                    numeratorinnerUOMIndex = cellcount;
                                } else if (cellHeaderName.contains("AUOM2") && packagingUOMIndex == -1) {
                                    packagingUOMIndex = cellcount;
                                } else if (cellHeaderName.contains("HS Code") && HSCodeIndex == -1) {
                                    HSCodeIndex = cellcount;
                                } else if (cellHeaderName.contains("Serial Number") && serialNumberIndex == -1) {
                                    serialNumberIndex = cellcount;
                                } else if (cellHeaderName.contains("Batch") && batchIndex == -1) {
                                    batchIndex = cellcount;
                                } else {
                                    allowData = false;
                                }
                                cellHeaderName = cellHeaderName.replaceAll("[.]", " ");//As we cannot add a custom column with special characters like dot
                                if (allowData) {
                                    validCellIndex.add(Integer.valueOf(cellcount));
                                    fileData.add(cellHeaderName);
                                }
//                                if (!getIsIgnoreColumn(cellHeaderName)) {
                                    headArrayList.add(cellHeaderName);
                                    columnConfig.put(cellHeaderName, cellcount);
                                    if (!allowData) {
                                        validCellIndex.add(Integer.valueOf(cellcount));
                                        fileData.add(cellHeaderName);
                                    }
//                                }
                            }
                        }
                        failedRecords.append(OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Error Message\"");
                        successRecords.append(OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Import Status\"");
                    }
                    cnt++;
                    count++;
                }
                System.out.println("\n-----------------------------------------------------------------------------------------------------------------------------------------");
                System.out.println("\nTotal time to read row data from file : "+totMillisecRow+" millisec .");
                Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nTotal time to read row data from file : "+totMillisecRow+" millisec .", "");
                System.out.println("\nTotal time to save data : "+totMillisecSave+" millisec ");
                Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nTotal time to save data : "+totMillisecSave+" millisec .", "");
                OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, cnt, msg, processStartDate, total);
                
                String bSFilePath = OlympusImportDataHandler.maintainBatchSerialFile(filePath, destinationFileName, batchSerialRecords, batchSerialCnt, msg, processStartDate);
                if (!StringUtil.isNullOrEmpty(bSFilePath)) {
                    createMailContent(baseUrl, "Product Master", total, batchSerialCnt, "", orgfile, fromEmailID, map,company, bSFilePath);
                }
            } else {
                System.out.println("---------------Company not exist with subdomain " + olympusSubdomain + " - ");
                System.out.println("or");
                System.out.println("---------------Problem in creating xls file.");
            }
            txnManager.commit(status);
        } catch (ServiceException ex) {
            issuccess = false;
            txnManager.rollback(status);
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Count : " + count);
            System.out.println("Ex : " + ex);
            msg.append(ex.getMessage());
            
            createMailContent(baseUrl, "Product Master", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
        } catch (IOException ex) {
            issuccess = false;
            txnManager.rollback(status);
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Count : " + count);
            System.out.println("Ex : " + ex);
            msg.append(ex.getMessage());
            
            createMailContent(baseUrl, "Product Master", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Count : " + count);
            System.out.println("Ex : " + ex);
            msg.append(ex.getMessage());
            
            createMailContent(baseUrl, "Product Master", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
        } finally {
            status = txnManager.getTransaction(def);
                try {
                    OlympusImportDataHandler.updateLogEntry(importDao, importLogID, destinationFileName, msg, total, failedCnt, company, issuccess, "30");
                    txnManager.commit(status);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            resultJObj.put("data", msg);
            resultJObj.put("failedCnt", failedCnt);
            resultJObj.put("total", total);
            resultJObj.put("processfilepath", processfilepath);
            resultJObj.put("xlsfilepath", filePath);
            resultJObj.put("failuefilepath", failuefilepath);
            createMailContent(baseUrl, "Product Master", total, failedCnt, "", orgfile, fromEmailID, map, company);
            return resultJObj;
        }
    }
    
    @Override
    public void addImportFilePath(String filePath, String subdomain, String moduleName, boolean deleteAllPreviousPath) throws ServiceException {
        int recId = getRecIdForModule(subdomain, moduleName);
        if (recId >= 0) {
            if (deleteAllPreviousPath) {
                removePreviousImportPaths(recId);
            }
            insertImportFilePath(recId, filePath);
        }
    }

    @Override
    public String[] getImportFilePaths(String subdomain, String moduleName) throws ServiceException {
        String getQuery = "SELECT recpaths.filepath FROM olympus_import_filepath recpaths "
                + " INNER JOIN olympus_importscriptinfo script ON script.recid = recpaths.recid"
                + " WHERE script.subdomain = ? AND script.id = ? ";
        List recidList = accCommonTablesDAO.executeSQLQuery(getQuery, new Object[]{subdomain, moduleName});
        String[] paths = new String[recidList.size()];
        paths = (String[]) recidList.toArray(paths);
        return paths;
    }

    private int getRecIdForModule(String subdomain, String moduleName) throws ServiceException {
        String getQuery = "SELECT recid FROM olympus_importscriptinfo WHERE subdomain = ? AND id = ? ";
        List recidList = accCommonTablesDAO.executeSQLQuery(getQuery, new Object[]{subdomain, moduleName});
        int recId = -1;
        if (recidList.size() > 0) {
            recId = recidList.get(0) != null ? ((BigInteger) recidList.get(0)).intValue() : -1;
        }
        return recId;
    }

    private void insertImportFilePath(int recId, String filePath) throws ServiceException {
        String query = "INSERT INTO olympus_import_filepath (recid, filepath) VALUES (?,?) ";
        accCommonTablesDAO.executeSQLUpdate(query, new Object[]{recId, filePath});
    }

    private void removePreviousImportPaths(int recId) throws ServiceException {
        String getQuery = "DELETE FROM olympus_import_filepath WHERE  recid = ? ";
        accCommonTablesDAO.executeSQLUpdate(getQuery, new Object[]{recId});
    }

    private UnitOfMeasure getUOMByName(String productUOMName, String companyID) throws AccountingException {
        UnitOfMeasure uom = null;
        try {
            if (!StringUtil.isNullOrEmpty(productUOMName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductDAO.getUOMByName(productUOMName, companyID);
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

    private Account getAccountByName(String accountName, String companyID) throws AccountingException {
        Account account = null;
        try {
            if (!StringUtil.isNullOrEmpty(accountName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductDAO.getAccountByName(accountName, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    Iterator itr = retObj.getEntityList().iterator();
                    if (itr != null && itr.hasNext()) {
                        String accountID = (String) itr.next();
                        KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountID);
                        account = (Account) custumObjresult.getEntityList().get(0);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Account");
        }
        return account;
    }

    private boolean getIsIgnoreColumn(String searchString) {
        List list = new ArrayList();
        list.add("Plant");
        list.add("Storage Location");
        list.add("Denominator");
        list.add("AUOM4");
        list.add("AUOM5");
        list.add("Serial Number");
        list.add("Batch");
        if (list.contains(searchString)) {
            return true;
        }

        return false;
    }

    @Override
    public JSONObject importStockMovementInData(HttpServletRequest request,String olympusSubdomain, String type, String baseUrl) throws JSONException {
        return importStockMovementInData(request, null, olympusSubdomain, type, baseUrl);
    }
    @Override
    public JSONObject importStockMovementInData(HttpServletRequest request, String filePath, String olympusSubdomain, String type, String baseUrl) throws JSONException {
        StringBuilder msg = new StringBuilder();
        JSONObject resultJObj = new JSONObject();
        boolean issuccess = true;
        StringBuilder destinationFileName = new StringBuilder();
        int cnt = 0, total = 0, TotalBatchCnt = Constants.Transaction_Commit_Limit_50, initBatchCnt = 0;
        int failedCnt = 0;
        String processfilepath="", failuefilepath = "", orgfile="";
        String toEmailID="", fromEmailID="";
        Company company =null;
        User user = null;
        String importLogID = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        Map<String,String> map = new HashMap<>();
        try {
            String userid = sessionHandlerImpl.getUserid(request);
            if (!StringUtil.isNullOrEmpty(userid)) {
                KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
                user = (User) jeresult.getEntityList().get(0);
            }
            company = getCompanyObject(olympusSubdomain);
            int noofcol=0;
            String scriptinfo = "select filepath,importinfo,noofcol from olympus_importscriptinfo where id = ? and subdomain = ?";
            List scriptInfoList = null;
            String storeCode = "";
            if ("1".equals(type)) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT1.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/Incoming Demo Asset_explaination.xls";
//                storeCode = "WH-DE";
            } else if ("2".equals(type)) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT2.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/Incoming Free Stock-A900_explaination.xls";
//                storeCode = "WH-SS";
            } else if ("3".equals(type)) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT3.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/Incoming-K stocks_explaination.xls";
//                storeCode = "WH-KS";
            } else if ("4".equals(type)) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT4.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/Incoming SP Stock-A900_explaination.xls";
//                storeCode = "WH-ES";
            }
            List<Object> fileData = new ArrayList();
            StringBuilder failedRecords = new StringBuilder();
            StringBuilder successRecords = new StringBuilder();
            JSONObject scriptInfoData=null;
            boolean flag=false;
            String xlsfilepath="";
            if (scriptInfoList.size() > 0) {
                    Object[] row = (Object[]) scriptInfoList.get(0);
                    if(StringUtil.isNullOrEmpty(filePath)){
                    filePath = row[0].toString();
                    }
                    processfilepath = filePath;
                    orgfile = processfilepath.substring(processfilepath.lastIndexOf("/")+1);
                    noofcol = Integer.parseInt(row[2].toString());
                    if(!StringUtil.isNullOrEmpty(filePath)){
                        String ext = FilenameUtils.getExtension(filePath);
                        xlsfilepath = filePath.replace(ext, "xls");
                        long start = new Date().getTime();
                        flag=convertToXLS(filePath, xlsfilepath, noofcol);
                        long end = new Date().getTime();
                        System.out.println("\nConvert file : "+(end-start)+" millisec .\n");
                        Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, "\nConvert file : "+(end-start)+" millisec .\n", "");
                    }
                    scriptInfoData = new JSONObject(row[1].toString());
            }
            if (company != null && flag) {
                filePath=xlsfilepath;
                fromEmailID = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                scriptinfo = "select emailids from olympus_importinfo where subdomain = ?";
                List scriptEmailInfo = null;
                scriptEmailInfo = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{olympusSubdomain});
                if (scriptEmailInfo.size() > 0) {
                    toEmailID = scriptEmailInfo.get(0)!=null ? scriptEmailInfo.get(0).toString() :"";
                }
                if (scriptInfoData!=null) {
                    if (scriptInfoData.has("storecode")) {
                        storeCode = scriptInfoData.getString("storecode");
                    }
                }
                HashMap<String,String> requestMailParams = new HashMap<>();
                requestMailParams.put("company", company.getCompanyID());
                requestMailParams.put("toEmailID", toEmailID);
                /**
                 * method used to get email IDs from companypreferences setting
                 *
                 */
                map = getAllMailsIDS(requestMailParams);
                POIFSFileSystem fs;
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                int sheetNo = 0;
                int maxCol = 0;
                int transactionDateIndex = -1;
                int productIdIndex = -1;
                int locationIndex = -1;
                int batchIndex = -1;
                int serialIndex = -1;
                int costIndex = -1;
                int qtyIndex = -1;
                int POreferenceIndex = -1;
                int assetIDIndex = -1;
                Date processStartDate = new Date();
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);
                List<Integer> validCellIndex = new ArrayList();
                total = sheet.getLastRowNum();
                Store store = storeService.getStoreByAbbreviation(company, storeCode);
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                        OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, -1, msg, processStartDate, total);
                        failuefilepath = destinationFileName.toString();
                        ImportLog logObj = OlympusImportDataHandler.addInitialLogEntry(importDao, "", destinationFileName, msg, cnt, failedCnt, company, issuccess, "83", processStartDate);
                        importLogID = logObj.getId();
                    }
                    if(initBatchCnt>=TotalBatchCnt) {
                        initBatchCnt = 0;
                        System.out.println("LOG ENTRY - "+cnt);
                        OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, cnt, msg, processStartDate, total);
                        OlympusImportDataHandler.addInitialLogEntry(importDao, importLogID, destinationFileName, msg, cnt, failedCnt, null, issuccess, "83", null);
                        txnManager.commit(status);
                        status = txnManager.getTransaction(def);
                    }
                    initBatchCnt++;
                    String poReferenceNo = null;
                    Product product = null;
                    Date transactionDate = null;
                    Location location = store.getDefaultLocation();
                    String serialNames = "";
                    String assetIds = "";
                    String batchName = "";
                    double qty = 0;
                    double cost = 0;
                    boolean invalidRec = false;
                    String invalidmsg = "";
                    if (cnt != 0) {
                        fileData.clear();
                        try {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            boolean allowData = true;
                            if (cell != null) {
                                String cellValue = getCellValue(cell);
                                if (cellcount == productIdIndex) {
//                                    String productID = cellValue;
                                    String productID = org.apache.commons.lang.StringUtils.stripStart(cellValue,"0");
                                    KwlReturnObject returnObject = accProductDAO.getProductByProductID(productID, company.getCompanyID());
                                    if (returnObject.getEntityList().size() > 0) {
                                        List<Product> productList = returnObject.getEntityList();
                                        product = productList.get(0);
                                    }
                                    if (product == null) {
                                        invalidRec = true;
                                        invalidmsg = " invalid Product";
                                        System.out.println("\n" + row.getRowNum() + "----invalid Product ");
                                        break;
                                    }
                                } else if (cellcount == transactionDateIndex) {
                                    try {
                                        transactionDate = importDF.parse(cellValue);
                                    } catch (ParseException pe) {
                                        try {
                                            transactionDate = commonDF.parse(cellValue);
                                        } catch (ParseException e) {
                                            invalidRec = true;
                                            invalidmsg = " invalid Transaction date";
                                            System.out.println("\n" + row.getRowNum() + "----invalid Transaction date ");
                                            break;
                                        }
                                    }
                                } else if (cellcount == POreferenceIndex) {
                                    poReferenceNo = cellValue;
                                } else if (cellcount == locationIndex) {
                                    try {
                                        if (!StringUtil.isNullOrEmpty(cellValue)) {
                                            location = locationService.getLocationByName(company, cellValue);
                                            if (location == null) {
                                                invalidRec = true;
                                                invalidmsg = " invalid Location";
                                                System.out.println("\n" + row.getRowNum() + "----invalid Location ");
                                                break;
                                            }
                                        }
                                    } catch (StorageException ex) {
                                        invalidRec = true;
                                        invalidmsg = " invalid Location";
                                        System.out.println("\n" + row.getRowNum() + "----invalid Location ");
                                        break;
                                    }

                                } else if (cellcount == serialIndex) {
                                    if (cellValue == null) {
                                        serialNames = "";
                                    } else {
                                        serialNames = cellValue;
                                    }

                                } else if (cellcount == assetIDIndex) {
                                    assetIds = cellValue;
                                } else if (cellcount == batchIndex) {
                                    if (cellValue == null) {
                                        batchName = "";
                                    } else {
                                        batchName = cellValue;
                                    }
                                } else if (cellcount == qtyIndex) {
                                    try {
                                        qty = Double.parseDouble(cellValue);
                                    } catch (NumberFormatException ex) {
                                        qty = 0;
                                    }
                                } else if (cellcount == costIndex) {
                                    try {
                                        cost = Double.parseDouble(cellValue);
                                    } catch (NumberFormatException ex) {
                                        cost = 0;
                                    }
                                } else {
                                    allowData = false;
                                }
                            }
                            if (validCellIndex.contains(Integer.valueOf(cellcount))) {
                                if(cell!=null) {
                                    fileData.add(getCellValue(cell));
                                } else {
                                    fileData.add("");
                                }
                            }
                        }
                        if (invalidRec) {
                            failedCnt++;
                            failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + invalidmsg + "\"");
                            continue;
                        }
                        if (product.isIsBatchForProduct()) {
                            if (StringUtil.isNullOrEmpty(batchName)) {
                                batchName = "B1"; //  auto created batch b1 if not comming
                            }
                        } else {
                            batchName = "";
                        }
                        if (product.isIsSerialForProduct()) {
                            int serialQty = 0;
                            if (!StringUtil.isNullOrEmpty(serialNames)) {
                                serialQty = serialNames.split(",").length;
                            }
                            if (serialQty > qty) {
                                qty = serialQty;
                            }
                            int remaingSerialQty = (int) qty - serialQty;
                            if (serialQty > 0) {
                                String[] serialArr = serialNames.split(",");
                                boolean serialExists = false;
                                for (String sn : serialArr) {
                                    serialExists = stockService.isSerialExists(product, store, location, batchName, sn);// has duequantity 1
                                    if (serialExists) {
                                        invalidmsg = "Serial " + sn + " already exists ";
                                        System.out.println("\n" + row.getRowNum() + "----Serial " + sn + " already exists ");
                                        break;
                                    }
                                }
                                if (serialExists) {
                                    failedCnt++;
                                    failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + invalidmsg + "\"");
                                    continue;
                                }
                            }
                            if (remaingSerialQty > 0) { // auto creating serials for remaining quantity
                                int serialNumber = 1;
                                while (remaingSerialQty > 0) {
                                    boolean exists = stockService.isSerialExists(product, store, location, batchName, String.valueOf(serialNumber));
                                    if (!exists) {
                                        remaingSerialQty--;
                                        if(StringUtil.isNullOrEmpty(serialNames)){
                                            serialNames = String.valueOf(serialNumber);
                                        }else{
                                            serialNames += "," + serialNumber;
                                        }
                                    }
                                    serialNumber++;
                                }
                            }
                            
                            if (product.isIsSKUForProduct()) {
                                if (StringUtil.isNullOrEmpty(assetIds)) {//if Asset Ids not givent then Serial Names kept as Asset Ids
                                    assetIds = serialNames;
                                } else {
                                    int assetIdQty = 0;
                                    if (!StringUtil.isNullOrEmpty(assetIds)) {
                                        assetIdQty = assetIds.split(",").length;
                                    }
                                    
                                    if (assetIdQty > 0) {
                                        String[] assedIDArr = assetIds.split(",");
                                        boolean assetIdExists = false;
                                        for (String asset : assedIDArr) {
                                            assetIdExists = stockService.isSKUExists(product, batchName, asset);// has duequantity 1
                                            if (assetIdExists) {
                                                invalidmsg = "Asset ID " + asset + " already exists ";
                                                System.out.println("\n" + row.getRowNum() + "----Asset ID  " + asset + " already exists ");
                                                break;
                                            }
                                        }
                                        if (assetIdExists) {
                                            failedCnt++;
                                            failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + invalidmsg + "\"");
                                            continue;
                                        }
                                    }
                                    int remaingAssetIdQty = (int) qty - assetIdQty;
                                    while (remaingAssetIdQty > 0) { // auto creating asset Id for remaining quantity
                                        int indexOfSerialToCopy = (int) qty - remaingAssetIdQty;
                                        assetIds += "," + (serialNames.split(","))[indexOfSerialToCopy];
                                        remaingAssetIdQty--;
                                    }
                                }
                            } else {
                                assetIds = "";
                            }
                        } else {
                            serialNames = "";
                            assetIds = "";
                        }
                        
                        /*
                         * ERP-12758 
                         * 1. If Batch and Serial is enabled - then auto generation of Batch and Serial.
                         * 2. If Batch and Serial is not enabled - then don't generate Batch and Serial. Only quantity will be updated.
                         * 3. If only Batch is enabled - then don't generate Serial. Only Batch and Quantity will be updated.
                         * 4. If only Serial is enabled - then auto generation of Batch and Serial.
                         */
                        if(!product.isIsBatchForProduct() && !product.isIsSerialForProduct()){
                            batchName = "";
                            serialNames = "";
                        } else if(product.isIsBatchForProduct() && !product.isIsSerialForProduct()){
                            serialNames = "";
                        }else if(!product.isIsBatchForProduct() && product.isIsSerialForProduct()){
                            batchName = "";
                        }
                        
                        StockMovement sm = new StockMovement(product, store, qty, cost, poReferenceNo, transactionDate, TransactionType.IN, TransactionModule.IMPORT, null);
                        sm.setRemark("Stock In data imported");
                        sm.setStockUoM(product.getUnitOfMeasure());
                        StockMovementDetail smd = new StockMovementDetail(sm, location, batchName, serialNames, qty);
                        sm.getStockMovementDetails().add(smd);
                        stockMovementService.addStockMovement(sm);

                        stockMovementService.stockMovementInERP(true, product, store, location, batchName, serialNames, qty);
                        stockMovementService.updateSkuFields(product, store, location, null, null, null, batchName, serialNames, assetIds);
                        stockService.increaseInventory(product, store, location, batchName, serialNames, qty);
                        stockService.updateERPInventory(true, transactionDate, product, product.getPackaging(), product.getUnitOfMeasure(), qty, batchName);
                        
                        successRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "Stock Movement done for po reference - " + poReferenceNo + ", Product - " + product.getProductid());
//                        msg += "\n---------------Stock Movement done for po reference - " + poReferenceNo + ", Product - " + product.getProductid() + " -------------------------------------";
//                        System.out.println("\n---------------Stock Movement done for po reference  - " + poReferenceNo + ", Product - " + product.getProductid() + " -------------------------------------");
                        } catch (Exception ex) {
                            failedCnt++;
                            failedRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"" + ex.getMessage() + "\"");
                        }
                    } else {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);

                            if (cell != null) {
                                String cellHeaderName = cell.getStringCellValue();
                                if ((cellHeaderName.contains("Cost center")) && locationIndex == -1) {
                                    locationIndex = cellcount;
                                } else if (cellHeaderName.contains("Transaction date") && transactionDateIndex == -1) {
                                    transactionDateIndex = cellcount;
                                } else if ((cellHeaderName.contains("Material Number")) && productIdIndex == -1) {
                                    productIdIndex = cellcount;
                                } else if ((cellHeaderName.contains("Serial/Lot no.") || cellHeaderName.contains("Serial / Lot #")) && serialIndex == -1) {
                                    serialIndex = cellcount;
                                } else if (((cellHeaderName.contains("Batch Number")) || cellHeaderName.contains("Batch")) && batchIndex == -1) {
                                    batchIndex = cellcount;
                                } else if ((cellHeaderName.contains("PO/PR information")) && POreferenceIndex == -1) {
                                    POreferenceIndex = cellcount;
                                } else if ((cellHeaderName.contains("Qty")) && qtyIndex == -1) {
                                    qtyIndex = cellcount;
                                } else if ((cellHeaderName.contains("Stock cost") || cellHeaderName.contains("Expense cost")) && costIndex == -1) {
                                    costIndex = cellcount;
                                } else if ((cellHeaderName.contains("Asset No.") || cellHeaderName.contains("Asset No.")) && assetIDIndex == -1) {
                                    assetIDIndex = cellcount;
                                }
                                validCellIndex.add(Integer.valueOf(cellcount));
                                fileData.add(cellHeaderName);
                            }
                        }
                        failedRecords.append(OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Error Message\"");
                        successRecords.append(OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "\"Import Status\"");
                    }
                    cnt++;
                }
                OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, cnt, msg, processStartDate, total);
            } else {
                System.out.println("---------------Company not exist with subdomain " + olympusSubdomain + " - ");
            }
            issuccess = true;
            txnManager.commit(status);
            if (company != null && scriptInfoList.size() > 0) {
                TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
                try {
                    KwlReturnObject retObj = consignmentService.assignStockToPendingConsignmentRequests(request,company,user);
                    txnManager.commit(statusforBlockSOQty);
                } catch (Exception ex) {
                    txnManager.rollback(statusforBlockSOQty);
                    Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (ServiceException ex) {
            issuccess = false;
            txnManager.rollback(status);
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg.append(ex.getMessage());
            
            if("1".equals(type)){
                createMailContent(baseUrl, "Stock Movement 1", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else if("2".equals(type)){
                createMailContent(baseUrl, "Stock Movement 2", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else if("3".equals(type)){
                createMailContent(baseUrl, "Stock Movement 3", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else if("4".equals(type)){
                createMailContent(baseUrl, "Stock Movement 4", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }
        } catch (IOException ex) {
            issuccess = false;
            txnManager.rollback(status);
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg.append(ex.getMessage());
            
            if("1".equals(type)){
                createMailContent(baseUrl, "Stock Movement 1", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else if("2".equals(type)){
                createMailContent(baseUrl, "Stock Movement 2", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else if("3".equals(type)){
                createMailContent(baseUrl, "Stock Movement 3", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else if("4".equals(type)){
                createMailContent(baseUrl, "Stock Movement 4", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }
        } catch (JSONException ex) {
            issuccess = false;
            txnManager.rollback(status);
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg.append(ex.getMessage());
            
            if("1".equals(type)){
                createMailContent(baseUrl, "Stock Movement 1", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else if("2".equals(type)){
                createMailContent(baseUrl, "Stock Movement 2", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else if("3".equals(type)){
                createMailContent(baseUrl, "Stock Movement 3", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }else if("4".equals(type)){
                createMailContent(baseUrl, "Stock Movement 4", total, failedCnt, ex.getMessage(), orgfile, fromEmailID, map, company);
            }
        } finally {
                status = txnManager.getTransaction(def);
                try {
                    OlympusImportDataHandler.updateLogEntry(importDao, importLogID, destinationFileName, msg, total, failedCnt, company, issuccess, "83");
                    txnManager.commit(status);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            resultJObj.put("data", msg);
            resultJObj.put("failedCnt", failedCnt);
            resultJObj.put("total", total);
            resultJObj.put("processfilepath", processfilepath);
            resultJObj.put("xlsfilepath", filePath);
            resultJObj.put("failuefilepath", failuefilepath);
            if("1".equals(type)){
                createMailContent(baseUrl, "Stock Movement 1", total, failedCnt, "", orgfile, fromEmailID, map, company);
            }else if("2".equals(type)){
                createMailContent(baseUrl, "Stock Movement 2", total, failedCnt, "", orgfile, fromEmailID, map, company);
            }else if("3".equals(type)){
                createMailContent(baseUrl, "Stock Movement 3", total, failedCnt, "", orgfile, fromEmailID, map, company);
            }else if("4".equals(type)){
                createMailContent(baseUrl, "Stock Movement 4", total, failedCnt, "", orgfile, fromEmailID, map, company);
            }
            return resultJObj;
        }
    }

//    public String createCSVrecord(Object[] listArray) {
//        String rec = "";
//        for (int i = 0; i < listArray.length; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
//            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString()) + "\",";
//        }
//        return rec;
//    }
    
    public void createFiles(String filename, StringBuilder failedRecords, String ext, boolean isFailure) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + (ext.equalsIgnoreCase(".csv") ? "importplans" : "xlsfiles");
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            java.io.FileOutputStream failurefileOut = new java.io.FileOutputStream(destinationDirectory + File.separator + filename + (isFailure ? ImportLog.failureTag : "") + ext);
            failurefileOut.write(failedRecords.toString().getBytes());
            failurefileOut.flush();
            failurefileOut.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }
    
//    public static String getActualFileName(String storageName, StringBuilder ext) {
//        ext.append(storageName.substring(storageName.lastIndexOf(".")));
//        String actualName = storageName.substring(storageName.lastIndexOf(File.separator)+1, storageName.lastIndexOf("."));
////        actualName = actualName;
//        return actualName;
//    }
    
    private void copyFileUsingFileStreams(File source, File dest)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            if(!dest.exists()) {
                dest.createNewFile();
            }
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
    }
    
    public boolean convertToXLS(String csvfilePath, String xlsFilePath, int noofcol) throws IOException {
        boolean flag=false;
        FileOutputStream fileOut=null;
        try {
            ArrayList arrayList = null;
            ArrayList al = null;
            String CLine;
            int i = 0;
            
            FileInputStream fis = new FileInputStream(csvfilePath);
            DataInputStream dis = new DataInputStream(fis);
            arrayList = new ArrayList();
            while ((CLine = dis.readLine()) != null) {
//                if(i>=startrow){//Start from startrow. Skip rows before startrow as they contain unwanted data.
                al = new ArrayList();
                String strar[] = CLine.split("\t");
                if(noofcol>0 && (strar.length+1)>=noofcol){//If no. of columns in file & value stored in 'noofcol' match then copy that record to file otherwise skip
                    strar[0] = strar[0].replace("\"", "");
                    strar[strar.length-1] = strar[strar.length-1].replace("\"", "");
                    for (int j = 0; j < strar.length; j++) {
                        strar[j] = strar[j].replaceAll("\"", "");
                        al.add(strar[j]);
                    }
                    arrayList.add(al);
                }else{
                    System.out.println("\nRow ["+(i+1)+"] rejected as number of column in file & database doesn't match.");
                }
                i++;
            }

            HSSFWorkbook hwb = new HSSFWorkbook();
            HSSFSheet sheet = hwb.createSheet("Sheet1");
            for (int k = 0; k < arrayList.size(); k++) {
                ArrayList ardata = (ArrayList) arrayList.get(k);
                HSSFRow row = sheet.createRow((short) 0 + k);
                for (int p = 0; p < ardata.size(); p++) {
                    HSSFCell cell = row.createCell((short) p);
                    cell.setCellValue(ardata.get(p).toString());
                }
                System.out.println();
            }
            fileOut = new FileOutputStream(xlsFilePath);
            hwb.write(fileOut);
            System.out.println("Your excel file has been generated");
            flag = true;
        } catch (Exception ex) {
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            fileOut.close();
        }
        return flag;
    }
    
    public void checkImportInfo(String olympusSubdomain, int type, String filepath) throws ServiceException {
        try{
            String importModule = "";
            switch(type){
                case 1 : importModule = OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.CUSTOMERBILLINGMASTER.toString();
                    break;
                case 11 : importModule = OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.CUSTOMERSHIPPINGMASTER.toString();
                    break;
                case 2 : importModule = OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.PRODUCTMASTER.toString();
                    break;
                case 3 : importModule = OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.LICENSEMASTER.toString();
                    break;
                case 4 : importModule = OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.SUPPLLICENSE1.toString();
                    break;
                case 41 : importModule = OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.SUPPLLICENSE2.toString();
                    break;
                case 5 : importModule = OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT1.toString();
                    break;
                case 51 : importModule = OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT2.toString();
                    break;
                case 52 : importModule = OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT3.toString();
                    break;
                case 53 : importModule = OlympusImportDataController.ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT4.toString();
                    break;
            }

            String scriptinfo = "select filepath from olympus_importscriptinfo where id = ? and subdomain = ?";
            List scriptInfoList = null;
            scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{importModule, olympusSubdomain});
            if (scriptInfoList.size() > 0) {
                scriptinfo = "update olympus_importscriptinfo set filepath = ? where id = ? and subdomain = ?";
                accCommonTablesDAO.executeSQLUpdate(scriptinfo, new Object[]{filepath, importModule, olympusSubdomain});
            }
//            else{
//                scriptinfo = "insert into olympus_importscriptinfo(id,filepath,importinfo,subdomain) "
//                        + "values(?,?,?,?) ";
//                accCommonTablesDAO.executeSQLUpdate(scriptinfo, new Object[]{importModule, filepath, "", olympusSubdomain});
//            }
        }catch(ServiceException ex){
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }catch(Exception ex){
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void createMailContent(String baseUrl, String importFor, int total, int failcnt, String msg, String fileName, String fromEmailID, Map map,Company company) {
        createMailContent(baseUrl, importFor, total, failcnt, msg, fileName, fromEmailID, map, company,null);
    }
    public void createMailContent(String baseUrl, String importFor, int total, int failcnt, String msg, String fileName, String fromEmailID, Map map,Company company,String attachment){
        HashMap mailParams= new HashMap();
        String importEmailIDs="",toEmailID="";
        if (map.containsKey("importEmailIDs")) {
            importEmailIDs = (String) map.get("importEmailIDs");
        }
        if (map.containsKey("toEmailID")) {
            toEmailID = (String) map.get("toEmailID");
        }
        try {
            mailParams.put("fromEmailID", fromEmailID);
            mailParams.put("importEmailIDs", importEmailIDs);
            mailParams.put("toEmailID", toEmailID);
            mailParams.put("company", company);
            
            String htmlMailContent ="<br><b>"+importFor+"</b>"+" File is processed from the backend. Please find its details.</br>\n";
            htmlMailContent += "<br/>Module: <b>"+importFor+"</b><br/>";
            htmlMailContent += "<br/>File Name: <b>"+ fileName +"</b><br/>";
            htmlMailContent += "<br/>Total Records: <b>"+ total +"</b><br/>";
            htmlMailContent += "<br/>Success Count: <b>"+ (total-failcnt) +"</b><br/>";
            htmlMailContent += "<br/>Failure Count: <b>"+ failcnt +"</b><br/>";
            htmlMailContent += "<br/><br/>For more details please check import log on - <b>"+ baseUrl +"</b><br/>";
            if(!StringUtil.isNullOrEmpty(msg)){
                htmlMailContent += "<br/>Reason: <b>"+ msg +"</b><br/>";
            }
            mailParams.put("htmlMailContent", htmlMailContent);

            String plainMailContent ="<br><b>"+importFor+"</b>"+" File is processed from the backend. Please find its details.</br>\n";
            plainMailContent += "\nModule: <b>"+importFor+"</b>\n";
            plainMailContent += "\nFile Name: <b>"+ fileName +"</b>\n";
            plainMailContent += "\nTotal Records: <b>"+ total +"</b>\n";
            plainMailContent += "\nSuccess Count: <b>"+ (total-failcnt) +"</b>\n";
            plainMailContent += "\nFailure Count: <b>"+ failcnt +"</b>\n";
            plainMailContent += "\n\nFor more details please check import log on - <b>"+ baseUrl +"</b>\n";
            if(!StringUtil.isNullOrEmpty(msg)){
                plainMailContent += "\nReason: <b>"+ msg +"</b>\n";
            }
            mailParams.put("plainMailContent", plainMailContent);
            if(!StringUtil.isNullOrEmpty(attachment)){
                mailParams.put("attachment", attachment);
            }
            
            SendMail(mailParams);    //Notification Mail 
        } catch (ServiceException ex) {
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void SendMail(HashMap requestParams) throws ServiceException {
        String importEmailIDs="",toEmailID="";
        String sendorInfo = (String) requestParams.get("fromEmailID");
        if (requestParams.containsKey("importEmailIDs")) {
            importEmailIDs = (String) requestParams.get("importEmailIDs");
        }
        if (requestParams.containsKey("toEmailID")) {
            toEmailID = (String) requestParams.get("toEmailID");
        }
        String htmlMailContent = (String) requestParams.get("htmlMailContent");
        String plainMailContent = (String) requestParams.get("plainMailContent");
        Company company = (Company) requestParams.get("company");
        SimpleDateFormat sdf = new SimpleDateFormat();
        String recipients[] = null ;
        String companyRecipients[] = null;
        /**
         *  email IDs from companypreferences setting
         */
        if(!StringUtil.isNullOrEmpty(importEmailIDs)){
          companyRecipients = importEmailIDs.split(",");
        }
        /**
         * maild ids saved in olympus_importinfo table(i.e
         * sagar.mahamuni@deskera.com,lipika.mahana@krawlernetworks.com,paritosh@deskera.com,mohammed.uzair@krawlernetworks.com)
         */
        if (!StringUtil.isNullOrEmpty(toEmailID)) {
            recipients = toEmailID.split(",");
        }
        if (recipients!=null) {
            try {
                String subject = "Olympus Import Status";
                String htmlTextC = "";
                htmlTextC += "<br/>Hello Dear,<br/>";
                htmlTextC = htmlTextC + htmlMailContent;

                htmlTextC += "<br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";
                htmlTextC += "<br/><br/>";
                htmlTextC += "<br/>This is an auto generated email. Do not reply<br/>";

                String plainMsgC = "";
                plainMsgC += "\nHello Dear,\n";
                plainMsgC = plainMsgC + plainMailContent;

                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nERP System\n";
                plainMsgC += "\n\n";
                plainMsgC += "\nThis is an auto generated email. Do not reply.\n";

                String[] attch = new String[0];
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                if (requestParams.containsKey("attachment") && requestParams.get("attachment") != null) {
                     attch = requestParams.get("attachment").toString().split(",");
                }
                /**
                 * maild ids from companypreferences setting
                 */
                if (companyRecipients != null) {
                    SendMailHandler.postMail(companyRecipients, subject, htmlTextC, plainMsgC, sendorInfo, attch, smtpConfigMap);
                }
                /**
                 * maild ids saved in olympus_importinfo table(i.e sagar.mahamuni@deskera.com,lipika.mahana@krawlernetworks.com,paritosh@deskera.com,mohammed.uzair@krawlernetworks.com  )
                 */
                if (recipients != null) {
                    SendMailHandler.postMail("", recipients, new String[0], subject, htmlTextC, plainMsgC, sendorInfo, attch, smtpConfigMap);
                }    

            } catch (Exception ex) {
                throw ServiceException.FAILURE("" + ex.getMessage(), ex);
            }
        }
    }
    /**
     * method used to get email IDs from companypreferences setting
     * 
     */
    public Map getAllMailsIDS(HashMap requestParams) throws ServiceException {
        Map<String, String> map = new HashMap<>();
        String userMailIds = "", toEmailID = "", importEmailIDs = "", company = "";
        if (requestParams.containsKey("company")) {
            company = (String) requestParams.get("company");
        }
        if (requestParams.containsKey("toEmailID")) {
            toEmailID = (String) requestParams.get("toEmailID");
        }
        try {

            if (!StringUtil.isNullOrEmpty(company)) {
                KwlReturnObject prefRes = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), company);
                ExtraCompanyPreferences companyPreferences = (ExtraCompanyPreferences) prefRes.getEntityList().get(0);
                userMailIds = companyPreferences.getUserEmails();
                importEmailIDs = companyPreferences.getSendImportMailTo();

                if (!StringUtil.isNullOrEmpty(userMailIds)) {
                    importEmailIDs += "," + userMailIds;
                }
                map.put("importEmailIDs", importEmailIDs);//maild ids from companypreferences setting
                map.put("toEmailID", toEmailID);//maild ids saved in olympus_importinfo table
            }
        } catch (ServiceException ex) {
            Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }
    
//    private void maintainLogFile(String filePath, StringBuilder destinationFileName, StringBuilder successRecords, StringBuilder failedRecords, int failedCnt, int total, StringBuilder msg) {
//        // Move File to another location after file process
//        StringBuilder ExT = new StringBuilder();
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
//        String fileName = getActualFileName(filePath, ExT) + "_" + df.format(new Date()) + ".csv";
//        String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
//        destinationFileName.append(destinationDirectory).append(File.separator).append(fileName);
//        File dirFrom = new File(filePath);
//        File dirTo = new File(destinationFileName.toString());
////                copyFileUsingFileStreams(dirFrom, dirTo);
//        createFiles(fileName, successRecords, ".csv", false);
//
//        // Create Failure File 
//        if (failedCnt > 0) {
//            createFiles(fileName, failedRecords, ".csv", true);
//        }
//        int success = total - failedCnt;
//        if (total == 0) {
//            msg.append("Empty file.");
//        } else if (success == 0) {
//            msg.append("Failed to import all the records.");
//        } else if (success == total) {
//            msg.append("All records are imported successfully.");
//        } else {
//            msg.append("Imported " + success + " record" + (success > 1 ? "s" : "")).append(" successfully");
//            msg.append(failedCnt == 0 ? "." : " and failed to import " + failedCnt + " record" + (failedCnt > 1 ? "s" : "") + ".");
//        }
//    }
//    
//    private void addLogEntry(StringBuilder destinationFileName, StringBuilder msg, int total, int failedCnt, Company company, boolean issuccess, String moduleid) throws ServiceException {
//        StringBuilder ExT = new StringBuilder();
//        String onlyFileName = getActualFileName(destinationFileName.toString(), ExT);
//        HashMap<String, Object> logDataMap = new HashMap<String, Object>();
//        logDataMap.put("FileName", onlyFileName + ExT);
//        logDataMap.put("StorageName", onlyFileName + ExT);
//        logDataMap.put("Log", msg.toString());
//        logDataMap.put("Type", destinationFileName.substring(destinationFileName.lastIndexOf(".") + 1));
//        logDataMap.put("TotalRecs", total);
//        logDataMap.put("Rejected", issuccess ? failedCnt : total);// if fail then rejected = total
//        logDataMap.put("Module", moduleid);
//        logDataMap.put("ImportDate", new Date());
//        logDataMap.put("User", company.getCreator().getUserID());
//        logDataMap.put("Company", company.getCompanyID());
//        importDao.saveImportLog(logDataMap);
//    }
}
