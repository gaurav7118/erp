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

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.LicenseType;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Term;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentService;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.store.StorageException;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.term.accTermDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class OlympusImportDataController extends MultiActionController {

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
    private OlympusImportDataServiceDAO olympusImportDataServiceDAO;
    private ConsignmentService consignmentService;
    private ImportDAO importDao;
    private auditTrailDAO auditTrailObj;
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

    public void setOlympusImportDataServiceDAO(OlympusImportDataServiceDAO olympusImportDataServiceDAO) {
        this.olympusImportDataServiceDAO = olympusImportDataServiceDAO;
    }
    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setConsignmentService(ConsignmentService consignmentService) {
        this.consignmentService = consignmentService;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    
    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
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
    
    public ModelAndView fileUpload(HttpServletRequest request, HttpServletResponse response) {
        String View = "jsonView_ex";
        JSONObject jobj = new JSONObject();
        try {
            int type = !StringUtil.isNullOrEmpty(request.getParameter("type"))?Integer.parseInt(request.getParameter("type")):-1;
            String olympusSubdomain = !StringUtil.isNullOrEmpty(request.getParameter("cdomain"))?request.getParameter("cdomain"):"";
            FileItemFactory factory = new DiskFileItemFactory(4096, new File(System.getProperty("java.io.tmpdir")));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(10000000);
            List fileItems = upload.parseRequest(request);
            Iterator i = fileItems.iterator();
            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importfiles";
            String fileName = null;
            String Ext = "";
            while (i.hasNext()) {
                java.io.File destDir = new java.io.File(destinationDirectory);
                if (!destDir.exists()) { //Create xls file's folder if not present
                    destDir.mkdirs();
                }

                FileItem fi = (FileItem) i.next();
                if (fi.isFormField()) {
                    continue;
                }
                fileName = fi.getName();
                if (fileName.contains(".")) {
                    Ext = fileName.substring(fileName.lastIndexOf("."));
                    int startIndex = fileName.contains("\\") ? (fileName.lastIndexOf("\\") + 1) : 0;
                    fileName = fileName.substring(startIndex, fileName.lastIndexOf("."));
                }

//                if (fileName.length() > 28) { // To fixed Mysql ERROR 1103 (42000): Incorrect table name
//                    throw new DataInvalidateException("Filename is too long, use upto 28 characters.");
//                }
                fi.write(new File(destinationDirectory, fileName + Ext));
            }
            
            String filepath = destinationDirectory + "/" + fileName + Ext;
            olympusImportDataServiceDAO.checkImportInfo(olympusSubdomain, type, filepath);
            
            jobj.put("filepath", destinationDirectory + "/" + fileName + Ext);
            jobj.put("filename", fileName + Ext);
            jobj.put("data", new com.krawler.utils.json.JSONArray());
            jobj.put("msg", "File has been successfully uploaded");
            jobj.put("success", true);
            jobj.put("valid", true);
        } catch (Exception e) {
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, e);
            try {
                jobj.put("msg", e.getMessage());
                jobj.put("success", false);
                jobj.put("valid", true);
            } catch (Exception ex) {
            }
        } finally {
            return new ModelAndView(View, "model", jobj.toString());
        }
    }
    
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        destFile.createNewFile();
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    public ModelAndView importOlympusFiles(HttpServletRequest request, HttpServletResponse response) {
        String message = "";
        try {
            String olympusSubdomain = !StringUtil.isNullOrEmpty(request.getParameter("cdomain"))?request.getParameter("cdomain"):"";
            int type = !StringUtil.isNullOrEmpty(request.getParameter("type"))?Integer.parseInt(request.getParameter("type")):-1;
            
            String baseUrl="";
            if(!StringUtil.isNullOrEmpty(request.getParameter("cdomain"))){
                baseUrl = URLUtil.getPageURL(request, loginpageFull);
            }else{
                baseUrl = getPageURLForCron(request, loginpageFull);
            }
            
            String userId = sessionHandlerImpl.getUserid(request);
            JSONObject jobj = new JSONObject();
            KwlReturnObject jeresult = accProductDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            String moduleName = "";    
            /*
             * Finding Company from subdomain
             */
            Company company = getCompanyObject(olympusSubdomain);
            if (company !=null && type!=-1) {
                message = "Olympus Data Import Process Started - "+ new Date();
                // Import Customers
                if(type==1) {
                    jobj = olympusImportDataServiceDAO.importCustomer(baseUrl, true, olympusSubdomain); // Import Billing Address
                    moduleName = Constants.CUSTOMERBILLINGMASTER_Module;
                } else if(type==11) {
                    jobj = olympusImportDataServiceDAO.importCustomer(baseUrl, false, olympusSubdomain); // Import Shipping Address
                    moduleName = Constants.CUSTOMERSHIPPINGMASTER_Module;
                }
                // Import Product Master
                else if(type==2){
                    jobj = olympusImportDataServiceDAO.importProducts(baseUrl, olympusSubdomain);
                    moduleName = Constants.PRODUCTMASTER_Module;
                }
                // Import Product License
                else if(type==3){
                    jobj = olympusImportDataServiceDAO.importMasterLicense(baseUrl, olympusSubdomain);
                    moduleName = Constants.LICENSEMASTER_Module;
                }
                // Import Product License Supplementary
                else if(type==4){
                    jobj = olympusImportDataServiceDAO.importSupplimenteryLicense(baseUrl, olympusSubdomain, true); // Import License Sup 1
                    moduleName = Constants.SUPPLLICENSE1_Module;
                }else if(type==41){
                    jobj = olympusImportDataServiceDAO.importSupplimenteryLicense(baseUrl, olympusSubdomain, false); // Import License Sup 2
                    moduleName = Constants.SUPPLLICENSE2_Module;
                }
                // Import Stock Movement in Data
                else if(type==5){
                    jobj = olympusImportDataServiceDAO.importStockMovementInData(request,olympusSubdomain,  "1", baseUrl);
                    moduleName = Constants.STOCKMOVEMENT1_Module;
                }else if(type==51){
                    jobj = olympusImportDataServiceDAO.importStockMovementInData(request,olympusSubdomain,  "2", baseUrl);
                    moduleName = Constants.STOCKMOVEMENT2_Module;
                }else if(type==52){
                    jobj = olympusImportDataServiceDAO.importStockMovementInData(request,olympusSubdomain,  "3", baseUrl);
                    moduleName = Constants.STOCKMOVEMENT3_Module;
                }else if(type==53){
                    jobj = olympusImportDataServiceDAO.importStockMovementInData(request,olympusSubdomain,  "4", baseUrl);
                    moduleName = Constants.STOCKMOVEMENT4_Module;
                }else {
//                    auditFlag = false;
                    message = "<br>Olympus Data Import - Please check the parameters.";
                    return new ModelAndView("jsonView_ex", "model", message);
                }
                message += "<br>Olympus Data Import Process Completed - "+ new Date();
                if(jobj.has("processfilepath")){
                    File processFile = new File(jobj.getString("processfilepath"));
                    String processFileName = processFile.getName();
                    String auditMessage = "manually imported " + processFileName + " for " + moduleName + ".";
                    if (!StringUtil.isNullOrEmpty(auditMessage)) {
                        auditMessage = "User " + user.getFullName() + " has " + auditMessage;
                        auditTrailObj.insertAuditLog(AuditAction.IMPORT_MASTER, auditMessage, request, "");
                    }
                }
            }else{
                message = "<br>Olympus Data Import - Please check the parameters.";
            }
        } catch(Exception ex) {
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", message);
    }
    
    public static String getPageURLForCron(HttpServletRequest request, String pagePathFormat) {
        String subdomain = request.getParameter("subdomain");
        String path = HttpUtils.getRequestURL(request).toString();
        String servPath = request.getServletPath();
        String uri = path.replace(servPath, "/" + String.format(pagePathFormat, subdomain));
        return uri;
    }
        
    public ModelAndView importMasterLicense(HttpServletRequest request, HttpServletResponse response) {
        String msg = "False";
        try {
            String filePath = "";
            String localcode = "", overseascode = "", localcategory = "", overseascategory = "";
            String olympusSubdomain = !StringUtil.isNullOrEmpty(request.getParameter("cdomain"))?request.getParameter("cdomain"):"";
            String scriptinfo = "select filepath,importinfo from olympus_importscriptinfo where id = ? and subdomain = ?";
//            filePath = "/Users/sagar/Desktop/Master License.xls";
            List scriptInfoList = null;
            scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{ImportScriptsFilePathAndDefaultValues.LICENSEMASTER.toString(),olympusSubdomain});
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String customfield = "";
            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            List headArrayList = new ArrayList();
            DateFormat df = authHandler.getGlobalDateFormat();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            Date licenseValidityStartDate = null;
            Date licenseValidityEndDate = null;
            /*
             * Finding Company from subdomain
             */
            Company company = getCompanyObject(olympusSubdomain);
            if (company != null && scriptInfoList.size()>0) {
                if (scriptInfoList.size() > 0) {
                    Object[] row = (Object[]) scriptInfoList.get(0);
                    filePath = row[0].toString();
                    JSONObject scriptInfoData = new JSONObject(row[1].toString());
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
                POIFSFileSystem fs;
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                int sheetNo = 0;
                int maxCol = 0;
                int cnt = 0;
                int productIDIndex = -1;
                int licenseTypeIndex = -1;
                int licenseCodeIndex = -1;
                int licenseValiditySDIndex = -1;
                int licenseValidityEDIndex = -1;
                int licenseStatusIndex = -1;
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                    }
                    String licenseType = "";
                    String productID = "";
                    String licenseCode = "";
                    String licenseValiditySD = "";
                    String licenseValidityED = "";
                    String licenseStatus = "";
                    if (cnt != 0) {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            if (cell != null) {
                                if (cellcount == productIDIndex) {
                                    productID = getCellValue(cell);
                                } else if (cellcount == licenseTypeIndex) {
                                    licenseType = getCellValue(cell);
                                } else if (cellcount == licenseCodeIndex) {
                                    licenseCode = getCellValue(cell);
                                } else if (cellcount == licenseValiditySDIndex) {
                                    licenseValiditySD = getCellValue(cell);
                                    licenseValidityStartDate = sdf.parse(licenseValiditySD);
                                } else if (cellcount == licenseValidityEDIndex) {
                                    licenseValidityED = getCellValue(cell);
                                    licenseValidityEndDate = sdf.parse(licenseValidityED);
                                } else if (cellcount == licenseStatusIndex) {
                                    licenseStatus = getCellValue(cell);
                                }
                            }
                        }
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
                                accCommonTablesDAO.executeSQLUpdate(insertmasterLicenseData, new Object[]{StringUtil.generateUUID(),product.getProductid(),
                                            product.getLicenseType().name(), product.getLicenseCode(), licenseValidityStartDate, licenseValidityEndDate, licenseStatus});
                            } else {
                                String updatemasterLicenseData = "UPDATE olympus_licensemaster set licensetype =?, "
                                        + "licensecode =? , vstartdate =? , venddate=? , licstatus=? where productid = ?";
                                accCommonTablesDAO.executeSQLUpdate(updatemasterLicenseData, new Object[]{product.getLicenseType().name(), product.getLicenseCode(),
                                            licenseValidityStartDate, licenseValidityEndDate, licenseStatus, product.getProductid()});
                            }

                            msg += "\n---------------Product License updated - " + productID + "-------------------------------------";
                            System.out.println("\n---------------Product License updated - " + productID + "-------------------------------------");

                            // For creating custom field array
                            customfield = "";
                            JSONArray customJArr = new JSONArray();
                            KwlReturnObject productObj = null;
                            HashMap<String, Object> productRequestParams = new HashMap<String, Object>();
                            for(int K = 0; K < headArrayList.size() ; K++) {
                                HashMap<String, Object> requestParamsCF = new HashMap<String, Object>();
                                requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParamsCF.put(Constants.filter_values, Arrays.asList(company.getCompanyID(), Constants.Acc_Product_Master_ModuleId, headArrayList.get(K)));
                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                FieldParams params = null;
                                if(fieldParamsResult.getRecordTotalCount()>0){
                                    params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                }

                                HSSFCell cell = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                if(params!=null && cell!=null){
                                    JSONObject customJObj = new JSONObject();
                                    customJObj.put("fieldid", params.getId());
                                    customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                    customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                    customJObj.put("xtype", params.getFieldtype());

                                    String fieldComboDataStr = "";
                                    if (params.getFieldtype() == 3) { // if field of date type
                                        HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                        String dateStr =cell1 !=null?getCellValue(cell1):""; 
                                        customJObj.put("Col" + params.getColnum(), sdf.parse(dateStr).getTime());
                                        customJObj.put("fieldDataVal", sdf.parse(dateStr).getTime());
                                    } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                        HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                        String comboRecords=cell1 !=null?getCellValue(cell1):""; 
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
                                        String checkbox=cell1 !=null?getCellValue(cell1):""; 
                                        customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(checkbox));
                                        customJObj.put("fieldDataVal", Boolean.parseBoolean(checkbox));
                                    } else if (params.getFieldtype() == 12) { // if field of check list type
                                        requestParamsCF = new HashMap<String, Object>();
                                        requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                        requestParamsCF.put(Constants.filter_values, Arrays.asList(params.getId(), 0));

                                        fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParamsCF);
                                        List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();
                                        HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                        String comboRecords=cell1 !=null?getCellValue(cell1):""; 

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
                                        String record=cell1 !=null?getCellValue(cell1):"";
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
                        } else {
                            msg += "\n---------------Product Not Exist - " + productID + "-------------------------------------";
                            System.out.println("\n---------------Product Not Exist - " + productID + "-------------------------------------");
                        }
                    } else {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            if (cell != null) {
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
                                }
                                headArrayList.add(cellHeaderName);
                                columnConfig.put(cellHeaderName, cellcount);
                            }
                        }
                    }
                    cnt++;
                }
            } else {
                System.out.println("---------------Company not exist with subdomain " + olympusSubdomain + " - ");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }

    public ModelAndView importSupplimenteryLicense(HttpServletRequest request, HttpServletResponse response) {
        String msg = "False";
        try {
            String filePath = "";
            String headerPrefix = "Supplementary";
            String scriptinfo = "select filepath,importinfo from olympus_importscriptinfo where id = ? and subdomain = ?";
            String olympusSubdomain = !StringUtil.isNullOrEmpty(request.getParameter("cdomain"))?request.getParameter("cdomain"):"";
            int flag = Integer.parseInt(request.getParameter("type"));
            List scriptInfoList = null;
            if (flag == 1) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{ImportScriptsFilePathAndDefaultValues.SUPPLLICENSE1.toString(), olympusSubdomain});
                //                filePath = "/Users/sagar/Desktop/License Sup 1_explaination.xls";
                headerPrefix += "1";
            } else {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{ImportScriptsFilePathAndDefaultValues.SUPPLLICENSE2.toString(), olympusSubdomain});
                //                filePath = "/Users/sagar/Desktop/License Sup 2_explaination.xls";
                headerPrefix += "2";
            }
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            /*
             * Finding Company from subdomain
             */
            Company company = getCompanyObject(olympusSubdomain);
            if (company != null && scriptInfoList.size()>0) {
                POIFSFileSystem fs;
                if (scriptInfoList.size() > 0) {
                    Object[] row = (Object[]) scriptInfoList.get(0);
                    filePath = row[0].toString();
                }
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                int sheetNo = 0;
                int maxCol = 0;
                int cnt = 0;
                int productIDIndex = -1;
                int licenseTypeIndex = -1;
                int licenseCodeIndex = -1;
                int licenseNoIndex = -1;
                int licenseValiditySDIndex = -1;
                int licenseValidityEDIndex = -1;
                int licenseStatusIndex = -1;
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                String customfield = "";
                HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
                List headArrayList = new ArrayList();
                DateFormat df = authHandler.getGlobalDateFormat();
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                    }
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
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            if (cell != null) {
                                if (cellcount == productIDIndex) {
                                    productID = getCellValue(cell);
                                } else if (cellcount == licenseTypeIndex) {
                                    licenseType = getCellValue(cell);
                                } else if (cellcount == licenseCodeIndex) {
                                    licenseCode = getCellValue(cell);
                                } else if (cellcount == licenseNoIndex) {
                                    licenseNo = getCellValue(cell);
                                } else if (cellcount == licenseValiditySDIndex) {
                                    licenseValiditySD = getCellValue(cell);
                                    licenseValidityStartDate = sdf.parse(licenseValiditySD);
                                } else if (cellcount == licenseValidityEDIndex) {
                                    licenseValidityED = getCellValue(cell);
                                    licenseValidityEndDate = sdf.parse(licenseValidityED);
                                } else if (cellcount == licenseStatusIndex) {
                                    licenseStatus = getCellValue(cell);
                                }
                            }
                        }
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
                                accCommonTablesDAO.executeSQLUpdate(insertmasterLicenseData, new Object[]{StringUtil.generateUUID(),product.getProductid(),
                                            licenseType, licenseCode, licenseNo, licenseValidityStartDate, licenseValidityEndDate, licenseStatus, flag});
                            } else {
                                String updatemasterLicenseData = "UPDATE olympus_licensesupplementary set licensetype =?, "
                                        + "licensecode =? , vstartdate =? , venddate=? , licstatus=? where productid = ? and supptype = ? ";
                                accCommonTablesDAO.executeSQLUpdate(updatemasterLicenseData, new Object[]{licenseType, licenseCode,
                                            licenseValidityStartDate, licenseValidityEndDate, licenseStatus, product.getProductid(), flag});
                            }

                            msg += "\n---------------Product License updated - " + productID + "-------------------------------------";
                            System.out.println("\n---------------Product License updated - " + productID + "-------------------------------------");

                            // For creating custom field array
                            customfield = "";
                            JSONArray customJArr = new JSONArray();
                            KwlReturnObject productObj = null;
                            HashMap<String, Object> productRequestParams = new HashMap<String, Object>();
                            for(int K = 0; K < headArrayList.size() ; K++) {
                                HashMap<String, Object> requestParamsCF = new HashMap<String, Object>();
                                requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParamsCF.put(Constants.filter_values, Arrays.asList(company.getCompanyID(), Constants.Acc_Product_Master_ModuleId, headArrayList.get(K)));
                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                FieldParams params = null;
                                if(fieldParamsResult.getRecordTotalCount()>0){
                                    params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                }

                                HSSFCell cell = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                if(params!=null && cell!=null){
                                    JSONObject customJObj = new JSONObject();
                                    customJObj.put("fieldid", params.getId());
                                    customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                    customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                    customJObj.put("xtype", params.getFieldtype());

                                    String fieldComboDataStr = "";
                                    if (params.getFieldtype() == 3) { // if field of date type
                                        HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                        String dateStr =cell1 !=null?getCellValue(cell1):""; 
                                        customJObj.put("Col" + params.getColnum(), sdf.parse(dateStr).getTime());
                                        customJObj.put("fieldDataVal", sdf.parse(dateStr).getTime());
                                    } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                        HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                        String comboRecords=cell1 !=null?getCellValue(cell1):""; 
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
                                        String checkbox=cell1 !=null?getCellValue(cell1):""; 
                                        customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(checkbox));
                                        customJObj.put("fieldDataVal", Boolean.parseBoolean(checkbox));
                                    } else if (params.getFieldtype() == 12) { // if field of check list type
                                        requestParamsCF = new HashMap<String, Object>();
                                        requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                        requestParamsCF.put(Constants.filter_values, Arrays.asList(params.getId(), 0));

                                        fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParamsCF);
                                        List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();
                                        HSSFCell cell1 = row.getCell((Integer) columnConfig.get(headArrayList.get(K).toString()));
                                        String comboRecords=cell1 !=null?getCellValue(cell1):""; 

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
                                        String record=cell1 !=null?getCellValue(cell1):"";
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
                        } else {
                            msg += "\n---------------Product Not Exist - " + productID + "-------------------------------------";
                            System.out.println("\n---------------Product Not Exist - " + productID + "-------------------------------------");
                        }
                    } else {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            if (cell != null) {
                                String cellHeaderName = cell.getStringCellValue();
                                cellHeaderName = cellHeaderName.replaceAll("[.]", " ");//As we cannot add a custom column with special characters
                                if (cellHeaderName.contains("Material Number") && productIDIndex == -1) {
                                    productIDIndex = cellcount;
                                } else if (cellHeaderName.contains(headerPrefix + " License type") && licenseTypeIndex == -1) {
                                    licenseTypeIndex = cellcount;
                                } else if (cellHeaderName.contains("License Code " + headerPrefix) && licenseTypeIndex == -1) {
                                    licenseCodeIndex = cellcount;
                                } else if (cellHeaderName.contains(headerPrefix+" Int License No ") && licenseCodeIndex == -1) {
                                    licenseNoIndex = cellcount;
                                } else if (cellHeaderName.contains("Validity Start date") && licenseValiditySDIndex == -1) {
                                    licenseValiditySDIndex = cellcount;
                                } else if (cellHeaderName.contains("Validity End date") && licenseValidityEDIndex == -1) {
                                    licenseValidityEDIndex = cellcount;
                                } else if (cellHeaderName.contains("Lic status") && licenseStatusIndex == -1) {
                                    licenseStatusIndex = cellcount;
                                }
                                headArrayList.add(cellHeaderName);
                                columnConfig.put(cellHeaderName, cellcount);
                            }
                        }
                    }
                    cnt++;
                }
            } else {
                System.out.println("---------------Company not exist with subdomain " + olympusSubdomain + " - ");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }

    public ModelAndView importCustomer(HttpServletRequest request, HttpServletResponse response) {
        String msg = "False";
        try {
            boolean isBillingAddress = request.getParameter("SP") != null ? true : false;
            String scriptinfo = "select filepath,importinfo from olympus_importscriptinfo where id = ? and subdomain = ?";
            String Trade_Debtors_AccountID = "";
            String filePath = "";
            String olympusSubdomain = !StringUtil.isNullOrEmpty(request.getParameter("cdomain"))?request.getParameter("cdomain"):"";
            List scriptInfoList = null;
            if (isBillingAddress) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{ImportScriptsFilePathAndDefaultValues.CUSTOMERBILLINGMASTER.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/CustomerMaster-SP_Explaination.xls";
            } else {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{ImportScriptsFilePathAndDefaultValues.CUSTOMERSHIPPINGMASTER.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/CustomerMaster-SH_Explaination.xls";
            }
            HashMap<String, Object> requestParams = new HashMap();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            /*
             * Finding Company from subdomain
             */
            Company company = getCompanyObject(olympusSubdomain);
            if (company != null && scriptInfoList.size() > 0) {
                if (scriptInfoList.size() > 0) {
                    Object[] row = (Object[]) scriptInfoList.get(0);
                    filePath = row[0].toString();
                    JSONObject scriptInfoData = new JSONObject(row[1].toString());
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
                int cnt = 0;
                int customerIDIndex = -1;
                int customerNameIndex = -1;
                int postalCodeIndex = -1;
                int cityIndex = -1;
                int countryIndex = -1;
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);
                List<Integer> billAddressIndex = new ArrayList<Integer>();
                List<Integer> shipAddressIndex = new ArrayList<Integer>();
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                    }
                    String customerName = "";
                    String customerID = "";
                    String billingAddress = "";
                    String shippingAddress = "";
                    String postalCode = "";
                    String city = "";
                    String country = "";
                    if (cnt != 0) {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            if (cell != null) {
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
                                } else if (cellcount == postalCodeIndex) {
                                    postalCode = getCellValue(cell);
                                } else if (cellcount == cityIndex) {
                                    city = getCellValue(cell);
                                } else if (cellcount == countryIndex) {
                                    country = getCellValue(cell);
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
                        Customer customer =null;
                        if (customerList.size() > 0) {
                            customer = customerList.get(0);
                            customer.setName(customerName);
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
                                custAddrMap.put("aliasName", customerAddressDetails.getAliasName());
                            }
                            msg += "\n---------------Customer updated - " + customerID + "-------------------------------------";
                            System.out.println("\n---------------Customer updated - " + customerID + "-------------------------------------");
                        } else {
                            HashMap<String, Object> customerRequestParams = new HashMap<String, Object>();
                            String uuid = StringUtil.generateUUID();
                            customerRequestParams.put("accid", uuid);
                            customerRequestParams.put("acccode", customerID);
                            customerRequestParams.put("accname", customerName);
                            customerRequestParams.put("termid", termId);
                            customerRequestParams.put("companyid", company.getCompanyID());
                            customerRequestParams.put("creationDate", authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getUTCToUserLocalDateFormatter(request).format(company.getCreatedOn())));
                            customerRequestParams.put("currencyid", company.getCurrency().getCurrencyID());
                            customerRequestParams.put("accountid", Trade_Debtors_AccountID);
                            KwlReturnObject result = accCustomerDAOobj.addCustomer(customerRequestParams);
                            customer = (Customer) result.getEntityList().get(0);
                            if (isBillingAddress) {
                                custAddrMap.put("aliasName", "Billing Address1");
                            } else {
                                custAddrMap.put("aliasName", "Shipping Address1");
                            }
                            
                            //check for MANI warehouse

                            ArrayList filternames = new ArrayList(), filterparams = new ArrayList();
                            filternames.add("company.companyID");
                            filterparams.add(company.getCompanyID());
                            filternames.add("name");
                            filterparams.add(Constants.WareHouseName);
                            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                            filterRequestParams.put("filter_names", filternames);
                            filterRequestParams.put("filter_params", filterparams);
                            KwlReturnObject cntResult = accCustomerDAOobj.getCustomerWarehouses(filterRequestParams);
                            int count = cntResult.getRecordTotalCount();
                            if (count > 0) {
                                String wareHouseId = ((InventoryWarehouse) cntResult.getEntityList().get(0)).getId();
                                accCustomerDAOobj.addCustomerWarehouseMapping(wareHouseId, uuid, false, null);
                            }

                        }
                        //save address detail after saving customer  
                        if (customer != null) {
                            custAddrMap.put("customerid", customer.getID());
                            custAddrMap.put("city", city);
                            custAddrMap.put("country", country);
                            custAddrMap.put("postalCode", postalCode);
                            custAddrMap.put("isDefaultAddress", true);
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
                    } else {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);

                            if (cell != null) {
                                String cellHeaderName = cell.getStringCellValue();
                                if (cellHeaderName.contains("Name 2") || cellHeaderName.contains("Name3")
                                        || cellHeaderName.contains("Search term") || cellHeaderName.contains("House number and street")
                                        || cellHeaderName.contains("Street 4")) {
                                    if (isBillingAddress) {
                                        billAddressIndex.add(cellcount);
                                    } else {
                                        shipAddressIndex.add(cellcount);
                                    }
                                } else if (cellHeaderName.contains("Customer Number") && customerIDIndex == -1) {
                                    customerIDIndex = cellcount;
                                } else if (cellHeaderName.contains("Name 1") && customerNameIndex == -1) {
                                    customerNameIndex = cellcount;
                                } else if (cellHeaderName.contains("Postal Code") && postalCodeIndex == -1) {
                                    postalCodeIndex = cellcount;
                                } else if (cellHeaderName.contains("City") && cityIndex == -1) {
                                    cityIndex = cellcount;
                                } else if (cellHeaderName.contains("Country Key") && countryIndex == -1) {
                                    countryIndex = cellcount;
                                }
                            }
                        }
                    }
                    cnt++;
                }
            } else {
                System.out.println("---------------Company not exist with subdomain " + olympusSubdomain + " - ");
            }
        } catch (ServiceException ex) {
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", msg);
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

    public ModelAndView importProducts(HttpServletRequest request, HttpServletResponse response) {
        String msg = "Product Master";
        int count = 0;
        try {
            String filePath = "";
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
            String olympusSubdomain = !StringUtil.isNullOrEmpty(request.getParameter("cdomain"))?request.getParameter("cdomain"):"";
            String scriptinfo = "select filepath,importinfo from olympus_importscriptinfo where id = ? and subdomain = ?";
            List scriptInfoList = null;
            scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{ImportScriptsFilePathAndDefaultValues.PRODUCTMASTER.toString(), olympusSubdomain});
            HashMap<String, Object> requestParams = new HashMap();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String companyid = "";
            Company company = null;
            /*
             * Finding Company from subdomain
             */
            company = getCompanyObject(olympusSubdomain);
            if (company != null && scriptInfoList.size()>0) {
                companyid = company.getCompanyID();
                if (scriptInfoList.size() > 0) {
                    Object[] row = (Object[]) scriptInfoList.get(0);
                    filePath = row[0].toString();
                    JSONObject scriptInfoData = new JSONObject(row[1].toString());
                    if (scriptInfoData.has("producttype")) {
                        defaultProductType = scriptInfoData.getString("producttype");
                    }
                    if (scriptInfoData.has("isbatchforproduct")) {
                        defaultIsBatchForProduct = scriptInfoData.getBoolean("isbatchforproduct");
                    }
                    if (scriptInfoData.has("isserialforproduct")) {
                        defaultIsSerialForProduct = scriptInfoData.getBoolean("isserialforproduct");
                    }
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
                }
                POIFSFileSystem fs;
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                int sheetNo = 0;
                int maxCol = 0;
                int cnt = 0;
                int materialNumberIndex = -1;
                int charecteristicValueIndex = -1;
                int materialDescriptionIndex = -1;
                int stockUOMIndex = -1;
                int numeratorstockUOMIndex = -1;
                int innerUOMIndex = -1;
                int numeratorinnerUOMIndex = -1;
                int packagingUOMIndex = -1;
                int HSCodeIndex = -1;
                DateFormat df = authHandler.getGlobalDateFormat();

                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);

                HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
                List headArrayList = new ArrayList();
                String customfield = "";

                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                    }

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
                    String casingUOM = "";
                    String HSCode="";

                    if (cnt != 0) {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            if (cellcount == materialNumberIndex) {
                                productID = cell != null ? getCellValue(cell) : "";
                            } else if (cellcount == charecteristicValueIndex) {
                                HSSFCell cellProdId = row.getCell(materialNumberIndex);
                                HSSFCell cellProdDesc = row.getCell(materialDescriptionIndex);
                                String prodId = cellProdId != null ? getCellValue(cellProdId) : "";
                                String prodDesc = cellProdDesc != null ? getCellValue(cellProdDesc) : "";
                                productName = prodId + "_" + prodDesc;
                            } else if (cellcount == materialDescriptionIndex) {
                                description = cell != null ? getCellValue(cell) : "";
                            } else if (cellcount == stockUOMIndex) {
                                stockUOM = cell != null ? getCellValue(cell) : "";
                                if (StringUtil.isNullOrEmpty(stockUOM)) {
                                    stockUOM = blankUOM;
                                }
                                stockuom = getUOMByName(stockUOM, companyid);
                                if (stockuom != null) {
                                    UOMID = stockuom.getID();
                                }
                            } else if (cellcount == numeratorstockUOMIndex) {
                                stockUOMValue = cell != null ? getCellValue(cell) : "";
                            } else if (cellcount == innerUOMIndex) {
                                String innerUOM = cell != null ? getCellValue(cell) : "";
                                if (StringUtil.isNullOrEmpty(innerUOM)) {
                                    innerUOM = blankUOM;
                                }
                                inneruom = getUOMByName(innerUOM, companyid);
                                if (inneruom != null) {
                                    innerUOMID = inneruom.getID();
                                }
                            } else if (cellcount == numeratorinnerUOMIndex) {
                                innerUOMValue = cell != null ? getCellValue(cell) : "";
                            } else if (cellcount == packagingUOMIndex) {
                                casingUOM = cell != null ? getCellValue(cell) : "";
                                if (StringUtil.isNullOrEmpty(casingUOM) || stockUOM.equals(casingUOM)) {
                                    casingUOM = blankUOM;
                                    casingUOMValue = "0";
                                }
                                casinguom = getUOMByName(casingUOM, companyid);
                                if (casinguom != null) {
                                    casingUOMID = casinguom.getID();
                                }
                            } else if(cellcount == HSCodeIndex) {
                                HSCode = cell!=null ? getCellValue(cell) : "";
                            }
                        }

                        HashMap<String, Object> productRequestParams = new HashMap<String, Object>();
                        productRequestParams.put("producttype", defaultProductType);
                        productRequestParams.put("productid", productID);
                        productRequestParams.put("name", productName);
                        productRequestParams.put("companyid", companyid);
                        productRequestParams.put("desc", description);
                        productRequestParams.put("syncable", false);
                        productRequestParams.put("multiuom", false);
                        productRequestParams.put("isBatchForProduct", defaultIsBatchForProduct);
                        productRequestParams.put("isSerialForProduct", defaultIsSerialForProduct);
                        productRequestParams.put("isLocationForProduct", defaultIslocationforproduct);
                        productRequestParams.put("isWarehouseForProduct", defaultIswarehouseforproduct);
                        productRequestParams.put("uomid", UOMID);
                        Packaging packaging = null;
                        if (stockuom != null) {
                            productRequestParams.put("purchaseuomid", stockuom);
                            productRequestParams.put("salesuomid", stockuom);
                            productRequestParams.put("transferUoM", stockuom);
                            productRequestParams.put("orderUoM", stockuom);
                            packaging = new Packaging();
                            packaging.setCasingUoM(casinguom);
                            packaging.setInnerUoM(inneruom);
                            packaging.setStockUoM(stockuom);
                            try {
                                packaging.setCasingUomValue(Double.parseDouble(casingUOMValue));
                                packaging.setInnerUomValue(Double.parseDouble(innerUOMValue));
                                packaging.setStockUomValue(Double.parseDouble(stockUOMValue));
                            } catch (NumberFormatException ex) {
                                Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, "NumberFormatException for product id : " + productID + " | product name : " + productName, ex);
                                continue;
                            }
                            packaging.setCompany(company);
                        }
                        if (packaging != null) {
                            accProductDAO.saveProductPackging(packaging);
                            productRequestParams.put("packaging", packaging);
                        }
                        productRequestParams.put("purchaseaccountid", defaultAccountID);
                        productRequestParams.put("purchaseretaccountid", defaultAccountID);
                        productRequestParams.put("salesaccountid", defaultAccountID);
                        productRequestParams.put("salesretaccountid", defaultAccountID);
                        if (!StringUtil.isNullOrEmpty(defaultfrqIds)) {
                            Set<Frequency> frequencys = new HashSet();
                            String[] fArray = defaultfrqIds.split(",");
                            for (String fId : fArray) {
                                Integer freqid = Integer.parseInt(fId);
                                KwlReturnObject jeresult = accProductDAO.getObject(Frequency.class.getName(), freqid);
                                Frequency frequency = (Frequency) jeresult.getEntityList().get(0);
                                frequencys.add(frequency);
                            }
                            productRequestParams.put("CCFrequency", frequencys);
                        }
                        productRequestParams.put("hsCode", HSCode);

                        KwlReturnObject productListObj = accProductDAO.getProductByProdID(productID, companyid,false);
                        List<Product> productList = productListObj.getEntityList();
                        KwlReturnObject productObj = null;
                        if (productList.size() > 0) {
                            Product product = productList.get(0);
                            productRequestParams.put("id", product.getID());
                            productObj = accProductDAO.updateProduct(productRequestParams);
                            System.out.println("\n---------------Product ID updated - " + productID + "-------------------------------------");
                            msg += "<br>---------------Product ID updated - " + productID + "-------------------------------------";
                        } else {
                            productObj = accProductDAO.addProduct(productRequestParams);
                            System.out.println("\n---------------Product ID added - " + productID + "-------------------------------------");
                            msg += "<br>---------------Product ID added - " + productID + "-------------------------------------";
                        }
                        Product p = (Product) productObj.getEntityList().get(0);
                        Date ondate = new Date();
                        if (p != null && defaultInitialPurchasePrice.length() > 0) {
                            // creating Price list Hashmap
                            HashMap<String, Object> initialPurchasePriceMap = new HashMap<String, Object>();
                            initialPurchasePriceMap.put("productid", p.getID());
                            initialPurchasePriceMap.put("companyid", companyid);
                            initialPurchasePriceMap.put("carryin", true);
                            initialPurchasePriceMap.put("price", Double.parseDouble(defaultInitialPurchasePrice));
                            initialPurchasePriceMap.put("applydate", ondate);
                            initialPurchasePriceMap.put("affecteduser", "-1");
                            initialPurchasePriceMap.put("currencyid", company != null ? company.getCurrency().getCurrencyID() : "");
                            initialPurchasePriceMap.put("uomid", p.getUnitOfMeasure().getID());
                            accProductDAO.addPriceList(initialPurchasePriceMap);
                        }
                        if (p != null && defaultInitialSalesPrice.length() > 0) {
                            // creating Price list Hashmap
                            HashMap<String, Object> salesPriceMap = new HashMap<String, Object>();
                            salesPriceMap.put("productid", p.getID());
                            salesPriceMap.put("companyid", companyid);
                            salesPriceMap.put("carryin", false);
                            salesPriceMap.put("price", Double.parseDouble(defaultInitialSalesPrice));
                            salesPriceMap.put("applydate", ondate);
                            salesPriceMap.put("affecteduser", "-1");
                            salesPriceMap.put("currencyid", company != null ? company.getCurrency().getCurrencyID() : "");
                            salesPriceMap.put("uomid", p.getUnitOfMeasure().getID());
                            accProductDAO.addPriceList(salesPriceMap);
                        }

                        // For creating custom field array
                        customfield = "";
                        JSONArray customJArr = new JSONArray();
                        for (int K = 0; K < headArrayList.size(); K++) {
                            HashMap<String, Object> requestParamsCF = new HashMap<String, Object>();
                            requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                            requestParamsCF.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, headArrayList.get(K)));
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
                                    customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                                    customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
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
                    } else {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);

                            if (cell != null) {
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
                                }else if (cellHeaderName.contains("HS Code") && HSCodeIndex == -1) {
                                    HSCodeIndex = cellcount;
                                }
                                cellHeaderName = cellHeaderName.replaceAll("[.]", " ");//As we cannot add a custom column with special characters like dot
                                if (!getIsIgnoreColumn(cellHeaderName)) {
                                    headArrayList.add(cellHeaderName);
                                    columnConfig.put(cellHeaderName, cellcount);
                                }
                            }
                        }
                    }
                    cnt++;
                    count++;
                }
            } else {
                System.out.println("---------------Company not exist with subdomain " + olympusSubdomain + " - ");
                msg += "<br><br>---------------Company not exist with subdomain " + olympusSubdomain + " - ";
            }
        } catch (ServiceException ex) {
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Count : " + count);
            System.out.println("Ex : " + ex);
            msg += "<br>Sorry! some problem occurred.";
        } catch (IOException ex) {
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Count : " + count);
            System.out.println("Ex : " + ex);
            msg += "<br>Sorry! some problem occurred.";
        } catch (Exception ex) {
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Count : " + count);
            System.out.println("Ex : " + ex);
            msg += "<br>Sorry! some problem occurred.";
        }
        return new ModelAndView("jsonView_ex", "model", msg);
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
                    if (itr!=null && itr.hasNext()) {
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

    public ModelAndView importStockMovementInData(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder msg = new StringBuilder();
        StringBuilder destinationFileName = new StringBuilder();
        int cnt = 0, total = 0;
        int failedCnt = 0;
        Company company =null;
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String olympusSubdomain = !StringUtil.isNullOrEmpty(request.getParameter("cdomain"))?request.getParameter("cdomain"):"";
            company = getCompanyObject(olympusSubdomain);
            String scriptinfo = "select filepath,importinfo from olympus_importscriptinfo where id = ? and subdomain = ?";
            List scriptInfoList = null;
            String filePath = "";
            String storeCode = "";
            String type = request.getParameter("type");
            if ("1".equals(type)) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT1.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/Incoming Demo Asset_explaination.xls";
//                storeCode = "WH-DE";
            } else if ("2".equals(type)) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT2.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/Incoming Free Stock-A900_explaination.xls";
//                storeCode = "WH-SS";
            } else if ("3".equals(type)) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT3.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/Incoming-K stocks_explaination.xls";
//                storeCode = "WH-KS";
            } else if ("4".equals(type)) {
                scriptInfoList = accCommonTablesDAO.executeSQLQuery(scriptinfo, new Object[]{ImportScriptsFilePathAndDefaultValues.STOCKMOVEMENT4.toString(), olympusSubdomain});
//                filePath = "/Users/sagar/Desktop/Incoming SP Stock-A900_explaination.xls";
//                storeCode = "WH-ES";
            }
            List<Object> fileData = new ArrayList();
            StringBuilder failedRecords = new StringBuilder();
            StringBuilder successRecords = new StringBuilder();
            if (company != null && scriptInfoList.size() > 0) {
                if (scriptInfoList.size() > 0) {
                    Object[] row = (Object[]) scriptInfoList.get(0);
                    filePath = row[0].toString();
                    JSONObject scriptInfoData = new JSONObject(row[1].toString());
                    if (scriptInfoData.has("storecode")) {
                        storeCode = scriptInfoData.getString("storecode");
                    }
                }

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
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);
                List<Integer> validCellIndex = new ArrayList();
                total = sheet.getLastRowNum();
                Store store = storeService.getStoreByAbbreviation(company, storeCode);
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                    }
                    String poReferenceNo = null;
                    Product product = null;
                    Date transactionDate = null;
                    Location location = store.getDefaultLocation();
                    String serialNames = "";
                    String batchName = "";
                    double qty = 0;
                    double cost = 0;
                    boolean invalidRec = false;
                    String invalidmsg = "";
                    if (cnt != 0) {
                        fileData.clear();
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            boolean allowData = true;
                            if (cell != null) {
                                String cellValue = getCellValue(cell);
                                if (cellcount == productIdIndex) {
                                    String productID = cellValue;
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
                            if(StringUtil.isNullOrEmpty(batchName)){
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
                            if(serialQty > qty){
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
                        } else {
                            serialNames = "";
                        }

                        StockMovement sm = new StockMovement(product, store, qty, cost, poReferenceNo, transactionDate, TransactionType.IN, TransactionModule.IMPORT, null);
                        sm.setRemark("Stock In data imported");
                        sm.setStockUoM(product.getUnitOfMeasure());
                        StockMovementDetail smd = new StockMovementDetail(sm, location, batchName, serialNames, qty);
                        sm.getStockMovementDetails().add(smd);
                        stockMovementService.addStockMovement(sm);

                        stockMovementService.stockMovementInERP(true, product, store, location, batchName, serialNames, qty);
                        stockService.increaseInventory(product, store, location, batchName, serialNames, qty);
                        stockService.updateERPInventory(true, transactionDate, product, product.getPackaging(), product.getUnitOfMeasure(), qty, batchName);
                        successRecords.append("\n" + OlympusImportDataHandler.createCSVrecord(fileData.toArray(new Object[fileData.size()])) + "Stock Movement done for po reference - " + poReferenceNo + ", Product - " + product.getProductid());
//                        msg += "</br>---------------Stock Movement done for po reference - " + poReferenceNo + ", Product - " + product.getProductid() + " -------------------------------------";
//                        System.out.println("\n---------------Stock Movement done for po reference  - " + poReferenceNo + ", Product - " + product.getProductid() + " -------------------------------------");

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
                                } else if (((cellHeaderName.contains("Batch Number"))  || cellHeaderName.contains("Batch")) && batchIndex == -1) {
                                    batchIndex = cellcount;
                                } else if ((cellHeaderName.contains("PO/PR information")) && POreferenceIndex == -1) {
                                    POreferenceIndex = cellcount;
                                } else if ((cellHeaderName.contains("Qty")) && qtyIndex == -1) {
                                    qtyIndex = cellcount;
                                } else if ((cellHeaderName.contains("Stock cost") || cellHeaderName.contains("Expense cost")) && costIndex == -1) {
                                    costIndex = cellcount;
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
                OlympusImportDataHandler.maintainLogFile(filePath, destinationFileName, successRecords, failedRecords, failedCnt, total, msg);
            } else {
                System.out.println("---------------Company not exist with subdomain " + olympusSubdomain + " - ");
            }
            issuccess = true;
            txnManager.commit(status);

            if (company != null && scriptInfoList.size() > 0) {
                TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
                try {
                    KwlReturnObject retObj = consignmentService.assignStockToPendingConsignmentRequests(request,company,null);
                    txnManager.commit(statusforBlockSOQty);
                } catch (Exception ex) {
                    txnManager.rollback(statusforBlockSOQty);
                    Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (ServiceException ex) {
            txnManager.rollback(status);
            issuccess = false;
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            txnManager.rollback(status);
            issuccess = false;
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            txnManager.rollback(status);
            issuccess = false;
            Logger.getLogger(OlympusImportDataController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(destinationFileName.length()>0) {
                DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
                ldef.setName("import_Tx");
                ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                TransactionStatus lstatus = txnManager.getTransaction(ldef);
                try {
                    OlympusImportDataHandler.addLogEntry(importDao, destinationFileName, msg, total, failedCnt, company, issuccess,"83");
                    txnManager.commit(lstatus);
                } catch (Exception ex) {
                    txnManager.rollback(lstatus);
                    Logger.getLogger(OlympusImportDataServiceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return new ModelAndView("jsonView_ex", "model", msg.toString());
    }
}