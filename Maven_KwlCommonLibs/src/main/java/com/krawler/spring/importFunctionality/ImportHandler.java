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
package com.krawler.spring.importFunctionality;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.DefaultHeader;
import com.krawler.common.admin.ImportLog;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.admin.Modules;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.*;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.ServerEventManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class ImportHandler implements Runnable, MessageSourceAware {

    private String df = "yyyy-MM-dd";
    private String df_full = "yyyy-MM-dd hh:mm:ss";
    private String df_customfield = "MMM dd, yyyy hh:mm:ss aaa";
    private String EmailRegEx = "^[\\w-]+([\\w!#$%&'*+/=?^`{|}~-]+)*(\\.[\\w!#$%&'*+/=?^`{|}~-]+)*@[\\w-]+(\\.[\\w-]+)*(\\.[\\w-]+)$";
    private String TimeRegEx = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private int importLimit = Constants.MAXIMUM_ALLOWED_RECORDS_FOR_IMPORT;
    private int importLimit_10000 = Constants.MAXIMUM_ALLOWED_RECORDS_FOR_IMPORT_10000;
    private static String[] masterTables = {"MasterItem"};
    private HibernateTransactionManager txnManager;
    private ImportDAO importDao;
    private kwlCommonTablesDAO KwlCommonTablesDAOObj;
    private MessageSource messageSource;
    private companyDetailsDAO companyDetailsDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private auditTrailDAO auditTrailObj;
    
    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setKwlCommonTablesDAO(kwlCommonTablesDAO KwlCommonTablesDAOObj1) {
        this.KwlCommonTablesDAOObj = KwlCommonTablesDAOObj1;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setmasterTables(String[] masterModules) {
        masterTables = masterModules;
    }
    
     public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }
    public static boolean isMasterTable(String module) {
        boolean isMasterModule = false;
        for (int i = 0; i < masterTables.length; i++) {
            if (module.equalsIgnoreCase(masterTables[i])) {
                isMasterModule = true;
                break;
            }
        }
        return isMasterModule;
    }

    public String getEmailRegExPattern() {
        return EmailRegEx;
    }

    public String getTimeRegExPattern() {
        return TimeRegEx;
    }

    public String getDFPattern() {
        return df;
    }

    public String getDFPatternCustomField() {
        return df_customfield;
    }

    public String getDFPatternFull() {
        return df_full;
    }
    
    boolean isWorking = false;
    ArrayList processQueue = new ArrayList();

    public void setIsWorking(boolean isWorking) {
        this.isWorking = isWorking;
    }

    public boolean isIsWorking() {
        return isWorking;
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
        try {
            while (!processQueue.isEmpty()) {
                HashMap<String, Object> requestParams = (HashMap<String, Object>) processQueue.get(0);
                try {
                    this.isWorking = true;
                    String modulename = requestParams.get("modName").toString();
                    JSONObject jobj=new JSONObject();
                     
                   
                    boolean allowropagatechildcompanies = requestParams.containsKey("allowropagatechildcompanies") ? Boolean.parseBoolean(requestParams.get("allowropagatechildcompanies").toString()) : false;
                    
                    if (allowropagatechildcompanies) {
                        Object childcompanies = requestParams.containsKey("childcompanylist") ? requestParams.get("childcompanylist") : "";
                        List childcompanylist = (List) childcompanies;
                        int totalChildCompanies = childcompanylist.size();
                        
                        int currentChildCompanycount=0;
                        JSONObject extraParams =requestParams.containsKey("extraParams")? (JSONObject)requestParams.get("extraParams") : new JSONObject();
                        requestParams.put("totalChildCompanies", totalChildCompanies);
                        for (Object childObj : childcompanylist) {
                            try {
                                currentChildCompanycount++;
                                Object[] childdataOBj = (Object[]) childObj;
                                String childCompanyID = (String) childdataOBj[0];
                                extraParams.put("Company", childCompanyID);
                                requestParams.put("extraParams", extraParams);
                                requestParams.put("companyid", childCompanyID);
                                requestParams.put("currentChildCompanycount", currentChildCompanycount);
                                importFileData(requestParams);
                            } catch (Exception ex) {
                                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    } else {
                        jobj = importFileData(requestParams);
                        User user = (User) KwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.User", requestParams.get("userid").toString());
                        Company company = (Company) KwlCommonTablesDAOObj.getClassObject(Company.class.getName(), user.getCompany().getCompanyID());
                        String htmltxt = "Report for data imported.<br/>";
                        htmltxt += "<br/>Module Name: " + modulename + "<br/>";
                        htmltxt += "<br/>File Name: " + jobj.get("filename") + "<br/>";
                        htmltxt += "Total Records: " + jobj.get("totalrecords") + "<br/>";
                        htmltxt += "Records Imported Successfully: " + jobj.get("successrecords");
                        htmltxt += "<br/>Failed Records: " + jobj.get("failedrecords");
                        htmltxt += "<br/>URL: " + requestParams.get("baseUrl").toString()+"<br/>";
                        htmltxt += "<br/><br/>Please check the import log in the system for more details.";
                        htmltxt += "<br/>For queries, email us at support@deskera.com<br/>";
                        htmltxt += "Deskera Team";

                        String plainMsg = "Report for data imported.\n";
                        plainMsg += "\nModule Name: " + modulename + "\n";
                        plainMsg += "\nFile Name:" + jobj.get("filename") + "\n";
                        plainMsg += "Total Records: " + jobj.get("totalrecords");
                        plainMsg += "\nRecords Imported Successfully: " + jobj.get("successrecords");
                        plainMsg += "\nFailed Records: " + jobj.get("failedrecords");
                        plainMsg += "\nURL: " + requestParams.get("baseUrl").toString();
                        plainMsg += "\n\nPlease check the import log in the system for more details.";

                        plainMsg += "\nFor queries, email us at support@deskera.com\n";
                        plainMsg += "Deskera Team";
                        String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                        Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                        SendMailHandler.postMail(new String[]{user.getEmailID()}, "Deskera Accounting - Report for data imported", htmltxt, plainMsg, fromEmailId, smtpConfigMap);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    processQueue.remove(requestParams);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.isWorking = false;
        }
    }

    public String getCellValue(Cell cell) {
       return getCellValue(cell, null);
    }
    public String getCellValue(Cell cell, DateFormat dateFormat) {
        String cellval = "";
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                cellval = cell.getRichStringCellValue().getString();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    if(dateFormat == null){
                        dateFormat = new SimpleDateFormat(df);
                        dateFormat.setLenient(false);
                    }
                    cellval = dateFormat.format(cell.getDateCellValue());
                } else {
                    double value = cell.getNumericCellValue();
                    cellval = String.valueOf(value);
                    if (cellval.contains(".")) {
                        if (value == Math.round(value)) {
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
        }
        return cellval.trim();
    }
    
    public JSONObject getMappingCSVHeader(HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        CsvReader csvReader = null;
        JSONObject jtemp1 = new JSONObject();
        JSONObject jobj = new JSONObject();
        JSONObject jsnobj = new JSONObject();
        String delimiterType = request.getParameter("delimiterType");
        String str = "";
        boolean isBomlessFile = !StringUtil.isNullOrEmpty(request.getParameter("isBomlessFile")) ? Boolean.parseBoolean(request.getParameter("isBomlessFile")) : false;
        {
            FileInputStream fstream = null;
            try {
                if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {

                    String fileid = UUID.randomUUID().toString();
                    fileid = fileid.replaceAll("-", ""); // To append UUID without "-" [SK]
//                    String Module = request.getParameter("type")==null?"":"_"+request.getParameter("type");
                    String f1 = uploadDocument(request, fileid);

                    if (f1.length() != 0) {
                        String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                        File csv = new File(destinationDirectory + "/" + f1);
                        fstream = new FileInputStream(csv);
                        csvReader = new CsvReader(new InputStreamReader(fstream), delimiterType);

                        csvReader.readHeaders();
                        String columnsArr[] = csvReader.getHeaders();
                        if (ArrayUtils.contains(columnsArr, "BOM Product ID*") && isBomlessFile) {  //User importing Assembly Product with BOM file through without BOM option
                            jobj.put("isInvalidFileForWithoutBOM", true);
                        }
                        int cols = csvReader.getHeaderCount();
                        jobj.put("headerCount", cols);
                        for (int k = 0; k < csvReader.getHeaderCount(); k++) {
                            jtemp1 = new JSONObject();
                            if (!StringUtil.isNullOrEmpty(csvReader.getHeader(k).trim())) {
                                jtemp1.put("header", csvReader.getHeader(k));
                                jtemp1.put("index", k);
                                jobj.append("Header", jtemp1);
                            }
                        }

                        if (jobj.isNull("Header")) {
                            jsnobj.put("success", "true");

                            str = jsnobj.toString();
                        } else {
                            jobj.append("success", "true");
                            jobj.append("FileName", f1);
                            jobj.put("name", f1);
                            jobj.put("delimiterType", delimiterType);
                            jobj.put("FilePath", csv);
                            jobj.put("cols", cols);
                            str = jobj.toString();
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                csvReader.close();
                fstream.close();
            }
        }
        return jobj;
    }

    public JSONObject getMappingCSVHeaderForDocument(HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        CsvReader csvReader = null;
        JSONObject jtemp1 = new JSONObject();
        JSONObject jobj = new JSONObject();
        JSONObject jsnobj = new JSONObject();
        String delimiterType = request.getParameter("delimiterType");
        String str = "";
        {
            FileInputStream fstream = null;
            BufferedReader br = null;
            try {
                if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
                    String record = "";
                    FileInputStream fileInputStream = null;
                    String fileid = UUID.randomUUID().toString();
                    fileid = fileid.replaceAll("-", ""); // To append UUID without "-" [SK]
//                    String Module = request.getParameter("type")==null?"":"_"+request.getParameter("type");
                    String f1 = uploadDocument(request, fileid);

                    if (f1.length() != 0) {
                        String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                        File csv = new File(destinationDirectory + "/" + f1);
                        fstream = new FileInputStream(csv);
                        csvReader = new CsvReader(new InputStreamReader(fstream), delimiterType);

                        csvReader.readHeaders();

                        int cols = csvReader.getHeaderCount();
                        jobj.put("headerCount", cols);
                        for (int k = 0; k < csvReader.getHeaderCount(); k++) {
                            jtemp1 = new JSONObject();
                            if (!StringUtil.isNullOrEmpty(csvReader.getHeader(k).trim())) {
                                jtemp1.put("header", csvReader.getHeader(k));
                                if (!csvReader.getHeader(k).equalsIgnoreCase("L") || !csvReader.getHeader(k).equalsIgnoreCase("T")) {
                                    break;
                                }
//                                jtemp1.put("index", k);
//                                jobj.append("Header", jtemp1);
                            }
                        }
                        fileInputStream = new FileInputStream(destinationDirectory + "/" + f1);
                        br = new BufferedReader(new InputStreamReader(fileInputStream));
                        int i = 0;
                        String headerName = "";
                        record = record = br.readLine();
                        if ((record = br.readLine()) != null) {
                            String[] recarr = record.split(",");
                            for (i = 0; i < recarr.length; i++) {
                                headerName = recarr[i];
                                if (!StringUtil.isNullOrEmpty(headerName)) {
                                    headerName = headerName.replaceAll("\"", "").trim();
                                }
                                jtemp1 = new JSONObject();
                                if (!StringUtil.isNullOrEmpty(headerName.trim())) {
                                    jtemp1.put("header", headerName);
                                    jtemp1.put("index", i);
                                    jobj.append("Header", jtemp1);
                                }
                            }
                        }

                        if (jobj.isNull("Header")) {
                            jsnobj.put("success", "true");

                            str = jsnobj.toString();
                        } else {
                            jobj.append("success", "true");
                            jobj.append("FileName", f1);
                            jobj.put("name", f1);
                            jobj.put("delimiterType", delimiterType);
                            jobj.put("FilePath", csv);
                            jobj.put("cols", cols);
                            str = jobj.toString();
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                csvReader.close();
                fstream.close();
            }
        }
        return jobj;
    }

    public String cleanHTML(String strText) throws IOException {
        return strText != null ? StringUtil.serverHTMLStripper(strText) : null;
    }

    public String cleanSpeacialChar(String strText) throws IOException {
        if (strText != null) {
            String data = strText;
            data = data.replaceAll("“", "");      // Special quotes opening
            data = data.replaceAll("”", "");      // Special quotes closing
            data = data.replaceAll("\"", "");     // Normal quotes
            return data;
        } else {
            return null;
        }
    }

    public String uploadDocument(HttpServletRequest request, String fileid) throws ServiceException {
        String result = "";
        try {
            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            org.apache.commons.fileupload.DiskFileUpload fu = new org.apache.commons.fileupload.DiskFileUpload();
            org.apache.commons.fileupload.FileItem fi = null;
            org.apache.commons.fileupload.FileItem docTmpFI = null;

            List fileItems = null;
            try {
                fileItems = fu.parseRequest(request);
            } catch (FileUploadException e) {
                KrawlerLog.op.warn("Problem While Uploading file :" + e.toString());
            }

            long size = 0;
            String Ext = "";
            String fileName = null;
            boolean fileupload = false;
            java.io.File destDir = new java.io.File(destinationDirectory);
            fu.setSizeMax(-1);
            fu.setSizeThreshold(4096);
            fu.setRepositoryPath(destinationDirectory);
            java.util.HashMap arrParam = new java.util.HashMap();
            for (java.util.Iterator k = fileItems.iterator(); k.hasNext();) {
                fi = (org.apache.commons.fileupload.FileItem) k.next();
                arrParam.put(fi.getFieldName(), fi.getString());
                if (!fi.isFormField()) {
                    size = fi.getSize();
                    fileName = new String(fi.getName().getBytes(), "UTF8");

                    docTmpFI = fi;
                    fileupload = true;
                }
            }

            if (fileupload) {

                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                if (fileName.contains(".")) {
                    Ext = fileName.substring(fileName.lastIndexOf("."));
                }
                if (size != 0) {
                    int startIndex = fileName.contains("\\") ? (fileName.lastIndexOf("\\") + 1) : 0;
                    fileName = fileName.substring(startIndex, fileName.lastIndexOf("."));
                    fileName = fileName.replaceAll(" ", "");
                    fileName = fileName.replaceAll("/", "");
                    fileName = fileName.replaceAll("&", "");
                    result = fileName + "_" + fileid + Ext;

                    File uploadFile = new File(destinationDirectory + "/" + result);
                    docTmpFI.write(uploadFile);
//                    fildoc(fileid, fileName, fileid + Ext, AuthHandler.getUserid(request), size);

                }
            }

        } //        catch (ConfigurationException ex) {
        //            Logger.getLogger(ExportImportContacts.class.getName()).log(Level.SEVERE, null, ex);
        //            throw ServiceException.FAILURE("ExportImportContacts.uploadDocument", ex);
        //        }
        catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("ExportImportContacts.uploadDocument", ex);
        }
        return result;
    }

    public JSONObject importCSVFile(HashMap<String, Object> requestParams, JSONObject extraParams, Object extraObj) throws IOException, ServiceException, DataInvalidateException {
        CsvReader csvReader = null;
        FileInputStream fstream = null;
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;

        int total = 0, failed = 0;
        String csvFile = "";
        Modules module = null;
        String companyid = requestParams.get("companyid").toString();
        String userid = requestParams.get("userid").toString();
        try {
            String mode = requestParams.get("modName").toString();
            String delimiterType = requestParams.get("delimiterType").toString();
            csvFile = requestParams.get("filename").toString();
            StringBuilder failedRecords = new StringBuilder();
            String jsondata = requestParams.get("resjson").toString();
            JSONArray jarr = new JSONArray("[" + jsondata + "]");
            JSONObject rootcsvjobj = jarr.getJSONObject(0);

            String rootcsvjson = rootcsvjobj.getString("root");
            JSONArray mapping = new JSONArray(rootcsvjson);

            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            File csv = new File(destinationDirectory + "/" + csvFile);
            fstream = new FileInputStream(csv);

            csvReader = new CsvReader(new InputStreamReader(fstream), delimiterType);
            csvReader.readHeaders();
            failedRecords.append(csvReader.getRawRecord() + ",\"Error Message\"");


            Map<Integer, Object> invalidColumn = new HashMap<>();//It only needs to pass as parameter to access the method
            HashMap<String, Object> columnCSVindexMap = new HashMap<String, Object>();//It only needs to pass as parameter to access the method
            String classPath = "", primaryKey = "", uniqueKeyMethodName = "", uniqueKeyHbmName = "";
            try {
                List list = importDao.getModuleObject(mode);
                module = (Modules) list.get(0); //Will throw null pointer if no module entry found
            } catch (Exception ex) {
                throw new DataInvalidateException("Column config not available for module " + mode);
            }

            try {
                classPath = module.getPojoClassPathFull().toString();
                primaryKey = module.getPrimaryKey_MethodName().toString();
            } catch (Exception ex) {
                throw new DataInvalidateException("Please set proper properties for module " + mode);
            }
            uniqueKeyMethodName = module.getUniqueKey_MethodName();
            uniqueKeyHbmName = module.getUniqueKey_HbmName();

            int subModuleFlag = 0;
            //Replaced the multiple arguments of getModuleColumnConfig() with single HashMap object
            HashMap<String, Object> params = new HashMap<String, Object> ();
            params.put("moduleId", module.getId());
            params.put("companyid", companyid);
            params.put("isdocumentimport", "F");
            params.put("subModuleFlag", new Integer(subModuleFlag));
            JSONArray columnConfig = getModuleColumnConfig(params);
            Map rowrefDataMap = new HashMap();
            for(int i=0 ; i<columnConfig.length() ; i++){
                JSONObject ccObject = columnConfig.getJSONObject(i);
                if("ref".equalsIgnoreCase(ccObject.optString("validatetype", null))){
                    rowrefDataMap.put(ccObject.optString("pojoName", ""), new HashMap());
                }
            }
            Set modulePrimaryKeuValues = new HashSet();
            while (csvReader.readRecord()) {
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                HashMap<String, Object> columnHeaderMap = new HashMap<String, Object>();
                JSONArray customfield = new JSONArray();
                for (int k = 0; k < mapping.length(); k++) {
                    JSONObject mappingJson = mapping.getJSONObject(k);
                    String datakey = mappingJson.getString("columnname");
                    Object dataValue = cleanHTML(csvReader.get(mappingJson.getInt("csvindex")));
                    dataMap.put(datakey, dataValue);
                    columnHeaderMap.put(datakey, mappingJson.getString("csvheader"));
                }

                for (int j = 0; j < extraParams.length(); j++) {
                    String datakey = (String) extraParams.names().get(j);
                    Object dataValue = extraParams.get(datakey);
                    dataMap.put(datakey, dataValue);
                }

                Object object = null;
                try {
                    CheckUniqueRecord(requestParams, dataMap, invalidColumn, classPath, uniqueKeyMethodName, uniqueKeyHbmName, primaryKey, columnHeaderMap, columnCSVindexMap,modulePrimaryKeuValues);
                    validateDataMAP(requestParams, dataMap, columnConfig, customfield, columnHeaderMap, rowrefDataMap);                    
                    object = importDao.saveRecord(requestParams, dataMap, csvReader, mode, classPath, primaryKey, extraObj, customfield);                     
                } catch (Exception ex) {
                    failed++;
                    failedRecords.append("\n" + csvReader.getRawRecord() + ",\"" + ex.getMessage() + "\"");
                    Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                total++;
            }

            if (failed > 0) {
                createFailureFiles(csvFile, failedRecords, null);
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
//        } catch (JSONException e) {
//            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, e);
//            issuccess = false;
//            msg = ""+e.getMessage();
//        } catch (IOException e) {
//            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, e);
//            issuccess = false;
//            msg = ""+e.getMessage();
        } catch (Exception e) {
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + e.getMessage();
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            csvReader.close();
            fstream.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);
            try {
                //Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", ImportLog.getActualFileName(csvFile));
                logDataMap.put("StorageName", csvFile);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", module.getId());
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userid);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("totalrecords", total);
                jobj.put("successrecords", total - failed);
                jobj.put("failedrecords", failed);
                jobj.put("filename", ImportLog.getActualFileName(csvFile));
            } catch (JSONException ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public JSONObject doXLSImport(HashMap<String, Object> requestParams, JSONObject extraParams, Object extraObj) throws IOException, JSONException {

        JSONObject parserResponse = new JSONObject();
        JSONObject jobj = new JSONObject();
        String xlsFile = requestParams.get("filepath").toString();
        String xlsFileName = requestParams.get("onlyfilename").toString();
        String mode = requestParams.get("moduleName").toString();
        int sheetindex = Integer.parseInt(requestParams.get("sheetindex").toString());
        int startindex = Integer.parseInt(requestParams.get("startindex").toString());
        String companyid = requestParams.get("companyid").toString();
        String userid = requestParams.get("userid").toString();

        String msg = "";
        boolean issuccess = true;
        FileInputStream fstream = null;
        boolean commitedEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);


        int total = 0, failed = 0;
        Modules module = null;

        try {
            parserResponse = parseXLS1(xlsFile, sheetindex, startindex);
            JSONArray xlsHeaders = new JSONArray(parserResponse.getString("Header"));
            String xlsjson = parserResponse.getString("data");
            JSONArray jarr2 = new JSONArray(xlsjson);

            String jsondata = requestParams.get("resjson").toString();
            JSONArray jarr = new JSONArray("[" + jsondata + "]");
            JSONObject rootcsvjobj = jarr.getJSONObject(0);

            String rootcsvjson = rootcsvjobj.getString("root");
            JSONArray jarr1 = new JSONArray(rootcsvjson);

            File xls = new File(xlsFile);
            fstream = new FileInputStream(xls);

            StringBuilder failedRecords = new StringBuilder();
            for (int j = 0; j < xlsHeaders.length(); j++) {
                JSONObject header = xlsHeaders.getJSONObject(j);
                failedRecords.append("\"" + header.getString("header") + "\",");
            }
            failedRecords.append("\"Error Message\"");

            Map<Integer, Object> invalidColumn = new HashMap<>();//It only needs to pass as parameter to access the method
            HashMap<String, Object> columnCSVindexMap = new HashMap<String, Object>();//It dnly needs to pass as parameter to access the method
            String classPath = "", primaryKey = "", uniqueKeyMethodName = "", uniqueKeyHbmName = "";
            try {
                List list = importDao.getModuleObject(mode);
                module = (Modules) list.get(0); //Will throw null pointer if no module entry found
            } catch (Exception ex) {
                throw new DataInvalidateException("Column config not available for module " + mode);
            }

            try {
                classPath = module.getPojoClassPathFull().toString();
                primaryKey = module.getPrimaryKey_MethodName().toString();
            } catch (Exception ex) {
                throw new DataInvalidateException("Please set proper properties for module " + mode);
            }
            uniqueKeyMethodName = module.getUniqueKey_MethodName();
            uniqueKeyHbmName = module.getUniqueKey_HbmName();


            int subModuleFlag = 0;
            //Replaced the multiple arguments of getModuleColumnConfig() with single HashMap object
            HashMap<String, Object> params = new HashMap<String, Object> ();
            params.put("moduleId", module.getId());
            params.put("companyid", companyid);
            params.put("isdocumentimport", "F");
            params.put("subModuleFlag", new Integer(subModuleFlag));
            JSONArray columnConfig = getModuleColumnConfig(params);
            Map rowRefDataMap = new HashMap();
            for(int i=0 ; i<columnConfig.length() ; i++){
                JSONObject ccObject = columnConfig.getJSONObject(i);
                if("ref".equalsIgnoreCase(ccObject.optString("validatetype", null))){
                    rowRefDataMap.put(ccObject.optString("pojoName", ""), new HashMap());
                }
            }
            Set modulePrimaryKeyValues = new HashSet();
            for (int a = 1; a <= (jarr2.length() - 1); a++) {
                JSONObject xlsjrecobj = jarr2.getJSONObject(a);

                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                HashMap<String, Object> culmnHeaderMap = new HashMap<String, Object>();
                JSONArray customfield = new JSONArray();
                for (int k = 0; k < jarr1.length(); k++) {
                    JSONObject xlsjobj = jarr1.getJSONObject(k);
                    String datakey = xlsjobj.getString("columnname");
                    String xlsHeader = xlsjobj.getString("csvheader");
                    String xlsValue = "";
                    if (xlsjrecobj.has(xlsHeader)) {
                        xlsValue = cleanHTML(xlsjrecobj.getString(xlsHeader));
                    }
                    dataMap.put(datakey, xlsValue);
                    culmnHeaderMap.put(datakey, xlsHeader);
                }

                for (int j = 0; j < extraParams.length(); j++) {
                    String datakey = (String) extraParams.names().get(j);
                    Object dataValue = extraParams.get(datakey);
                    dataMap.put(datakey, dataValue);
                }

                Object object = null;
                try {
                    CheckUniqueRecord(requestParams, dataMap, invalidColumn,classPath, uniqueKeyMethodName, uniqueKeyHbmName, primaryKey, culmnHeaderMap, columnCSVindexMap,modulePrimaryKeyValues);
                    validateDataMAP(requestParams, dataMap, columnConfig, customfield, culmnHeaderMap, rowRefDataMap);
                    object = importDao.saveRecord(requestParams, dataMap, xlsjrecobj, mode, classPath, primaryKey, extraObj, customfield);
                } catch (Exception ex) {
                    failed++;
//                        failedRecords.append("\n"+csvReader.getRawRecord()+",\""+ex.getMessage()+"\"");
                    failedRecords.append("\n");
                    for (int j = 0; j < xlsHeaders.length(); j++) {
                        JSONObject header = xlsHeaders.getJSONObject(j);
                        if (xlsjrecobj.opt(header.getString("header")) != null) {
                            failedRecords.append("\"" + xlsjrecobj.opt(header.getString("header")) + "\",");
                        } else {
                            failedRecords.append(",");
                        }
                    }
                    failedRecords.append("\"" + ex.getMessage() + "\"");
//                        Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                total++;
            }

            if (failed > 0) {
                createFailureFiles(xlsFileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
                //                issuccess = false;
                msg = "Failed to import all the records.";
            } else if (success == total) {
                msg = "All records imported successfully.";
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

        } catch (Exception e) {
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + e.getMessage();
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            fstream.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);
            boolean exCommit = false;
            try {
                //Insert Integration log

                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", ImportLog.getActualFileName(xlsFileName));
                logDataMap.put("StorageName", xlsFileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "xls");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", module.getId());
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userid);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                try {
                    txnManager.commit(lstatus);
                } catch (Exception ex) {
                    exCommit = true;
                    throw ex;
                }
            } catch (Exception ex) {
                if (!exCommit) { //if exception occurs during commit then dont call rollback
                    txnManager.rollback(lstatus);
                }
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("totalrecords", total);
                jobj.put("successrecords", total - failed);
                jobj.put("failedrecords", failed);
                jobj.put("filename", ImportLog.getActualFileName(xlsFileName));
            } catch (JSONException ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public void validateDataMAP(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, JSONArray columnConfigArray, JSONArray customfield, HashMap<String, Object> columnHeaderMap, Map rowRefDataMap) throws DataInvalidateException {
        String errorMsg = "";
        for (int k = 0; k < columnConfigArray.length(); k++) {
            try {
                JSONObject columnConfig = columnConfigArray.getJSONObject(k);
                String column = columnConfig.getString("pojoName");

                if (dataMap.containsKey(column)) {
                    validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, null, rowRefDataMap);
                } else {
                    if (columnConfig.has("defaultValue")) {
                        dataMap.put(column, getDefaultValue(columnConfig,requestParams));
                    }
                }
            } catch (Exception ex) {
                errorMsg += ex.getMessage() + ",";
            }
        }
        if (errorMsg.length() > 0) {
            errorMsg = errorMsg.substring(0, errorMsg.length() - 1) + ".";
            throw new DataInvalidateException(errorMsg);
        }
    }

    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + (ext.equalsIgnoreCase(".csv") ? "importplans" : "xlsfiles");
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

    public JSONArray getModuleColumnConfig(HashMap<String, Object> params) throws ServiceException {
        JSONArray jArr = new JSONArray();
        String moduleId = "", companyid = "", isdocumentimport = "";
        int subModuleFlag = 0;
        boolean isBomlessFile = false;
        boolean updateExistingRecordFlag = false;
        boolean isExpenseInvoiceImport = false;
        boolean isAssetsImportFromDoc = false;
        boolean incash = false;
        if(params.containsKey("moduleId") && params.get("moduleId")!=null){
            moduleId = (String) params.get("moduleId");
        }
        if(params.containsKey("companyid") && params.get("companyid")!=null){
            companyid = (String) params.get("companyid");
        }
        if(params.containsKey("isdocumentimport") && params.get("isdocumentimport")!=null){
            isdocumentimport = (String) params.get("isdocumentimport");
        }
        if(params.containsKey("updateExistingRecordFlag") && params.get("updateExistingRecordFlag")!=null){
            updateExistingRecordFlag = (Boolean) params.get("updateExistingRecordFlag");
        }
        if(params.containsKey("subModuleFlag") && params.get("subModuleFlag")!=null){
            subModuleFlag = ((Integer)params.get("subModuleFlag")).intValue();
        }
        if(params.containsKey("isBomlessFile") && params.get("isBomlessFile")!=null){
            isBomlessFile = (Boolean) params.get("isBomlessFile");  //User cannot import the without BOM file through import Assembly Product with BOM option
        }
        if(params.containsKey("isExpenseInvoiceImport") && params.get("isExpenseInvoiceImport")!=null){
            isExpenseInvoiceImport = (Boolean) params.get("isExpenseInvoiceImport");  //User cannot import the without BOM file through import Assembly Product with BOM option
        }
        if(params.containsKey("isAssetsImportFromDoc") && params.get("isAssetsImportFromDoc")!=null){
            isAssetsImportFromDoc = (Boolean) params.get("isAssetsImportFromDoc");  //User cannot import the without BOM file through import Assembly Product with BOM option
        }
        if(params.containsKey("incash") && params.get("incash")!=null){
            incash = (Boolean) params.get("incash");  //User cannot import the without BOM file through import Assembly Product with BOM option
        }
       
        Company company = (Company) KwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);        
        String countryid = company!=null?company.getCountry().getID():String.valueOf(0);
        /*Check in Company Account Preference is inventoryValuationType  0- Periodic, 1- Perpetual*/
        boolean isPerpetualInventory= importDao.isPerpetualInventory(companyid);
        boolean isPartNumberActivated = importDao.isPartNumberActivated(companyid);
        boolean isMinMaxOrdering = importDao.isMinMaxOrdering(companyid);
        Map<String, Object> importParams=new HashMap<>();
        importParams.put("moduleId", moduleId);
        importParams.put("companyid", companyid);
        importParams.put("isdocumentimport", isdocumentimport);
        importParams.put("countryid", countryid);
        importParams.put("subModuleFlag", subModuleFlag);
        importParams.put("isExpenseInvoiceImport", isExpenseInvoiceImport);
        importParams.put("isAssetsImportFromDoc", isAssetsImportFromDoc);
        importParams.put("incash", incash);
        List list = importDao.getModuleColumnConfig(importParams);       //getting entries from default header
        boolean isCurrencyCode = false;
        boolean isactivatedTodate = false;
        boolean notMandatory = false;
        boolean withoutBOM = false;
        if (companyid.equals(Constants.COMPANYID_HINSINTSU)) {
            withoutBOM = true;
        }
        try {
            KwlReturnObject kobj = KwlCommonTablesDAOObj.getIsCurrenyCodeAndIsActivatedTodate(companyid);
            List listobj = kobj.getEntityList();
            if (listobj.size() > 0) {
                Object[] row = (Object[]) listobj.get(0);
                if (row != null) {
                    isCurrencyCode = Boolean.parseBoolean(row[0].toString());
                    isactivatedTodate = Boolean.parseBoolean(row[1].toString());
                }
            }
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                DefaultHeader dh = (DefaultHeader) KwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.DefaultHeader", row[0].toString());
                
                if (!isPartNumberActivated && (dh.getDefaultHeader().equalsIgnoreCase("Supplier Part Number") || dh.getDefaultHeader().equalsIgnoreCase("Part Number") || dh.getDefaultHeader().equalsIgnoreCase("Customer Part Number"))) {
                    continue;
                }
                
//                if(moduleId.equals(""+Constants.Acc_Product_Master_ModuleId) && !isPerpetualInventory && (dh.getDataIndex() !=null && (dh.getDataIndex().equalsIgnoreCase("stockadjustmentaccountid") || dh.getDataIndex().equalsIgnoreCase("inventoryaccountid") || dh.getDataIndex().equalsIgnoreCase("cogsaccountid")))){
                if(moduleId.equals(""+Constants.Acc_Product_Master_ModuleId) && !isPerpetualInventory && (dh.getDataIndex().equalsIgnoreCase("stockadjustmentaccountid") || dh.getDataIndex().equalsIgnoreCase("inventoryaccountid") || dh.getDataIndex().equalsIgnoreCase("cogsaccountid"))){
                    continue;
                }
                if (moduleId.equals("" + Constants.Acc_Product_Master_ModuleId) && (dh.getDataIndex().equalsIgnoreCase("purchaseprice") || dh.getDataIndex().equalsIgnoreCase("initialquantity") || dh.getDataIndex().equalsIgnoreCase("initialprice"))) {
                    // Remove initial purchase price & initial quantity from product import ERP-39020
                    continue;
                }
                
                if(!isMinMaxOrdering && (dh.getDefaultHeader().equalsIgnoreCase("Minimum Ordering Quantity") || dh.getDefaultHeader().equalsIgnoreCase("Maximum Ordering Quantity"))){
                    continue;
                }
                
                JSONObject jtemp = new JSONObject();
                notMandatory = false;
                jtemp.put("id", dh.getId());
                params.put("defaultheader", dh);
                String columnName = getColumnNameForModuleColumnConfig(params);
                jtemp.put("columnName", columnName);
                
                jtemp.put("pojoName", dh.getPojoMethodName());
                String pojomethodname = dh.getPojoMethodName();
                /*
                 When update Existing case only Customer or vendor code is mandatory other fields maks as optional while importin Customer or vendor master
                */
                if (updateExistingRecordFlag && moduleId.equals(Constants.CUSTOMER_MODULE_UUID) && (!dh.getDefaultHeader().equalsIgnoreCase("Customer Code"))) {
                    notMandatory=true;
                }else if (updateExistingRecordFlag && moduleId.equals(Constants.Vendor_MODULE_UUID) && (!dh.getDefaultHeader().equalsIgnoreCase("Vendor Code"))){
                    notMandatory=true;
                }else if (updateExistingRecordFlag && (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Product_Master_ModuleId)) || (isBomlessFile && moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Assembly_Product_Master_ModuleId)))) && (!dh.getDefaultHeader().equalsIgnoreCase("Product ID"))){
                    notMandatory=true;
                }
                
                if(isBomlessFile && (pojomethodname.equals("BOMProductid") || pojomethodname.equals("BOMQuantity"))){ //To remove the dependancy of the company check - withoutBOM
                    jtemp.put("isMandatory", false);//Kept non-mandatory for 'HINSINTSU' Client
                    jtemp.put("isNotNull", false);//Kept non-mandatory for 'HINSINTSU' Client
                } else {
                    if (notMandatory) {
                        jtemp.put("isMandatory", false);
                        jtemp.put("isNotNull", false);
                    } else {
                        jtemp.put("isMandatory", dh.isMandatory());
                        jtemp.put("isNotNull", dh.isHbmNotNull());
                    }
                }                
                jtemp.put("maxLength", dh.getMaxLength());
                jtemp.put("defaultValue", dh.getDefaultValue());
                jtemp.put("validatetype", dh.getValidateType());
                jtemp.put("refModule", dh.getRefModule_PojoClassName());
                jtemp.put("refFetchColumn", dh.getRefFetchColumn_HbmName());
                jtemp.put("refDataColumn", dh.getRefDataColumn_HbmName()); 
                jtemp.put("customflag", dh.isCustomflag());
                jtemp.put("pojoHeader", dh.getPojoheadername());
                jtemp.put("xtype", dh.getXtype());
                jtemp.put("fieldtype", dh.isIsbatchdetail() == true ? Constants.DefaultHeader_BatchDetails:((dh.isIslineitem()) ? Constants.DefaultHeader_DefaultLineItems : Constants.DefaultHeader_DefaultGlobalItems));
                jtemp.put("configid", dh.getConfigid() == null ? "" : dh.getConfigid());
                jtemp.put("dataindex", dh.getDataIndex() == null ? "" : dh.getDataIndex());
                if(!StringUtil.isNullOrEmpty(dh.getPojoMethodName()) && dh.getPojoMethodName().equals("Currency")&&isCurrencyCode) {
                    jtemp.put("pojoName", "currencyCode");
                    jtemp.put("dataindex", dh.getDataIndex() == null ? "" : "currencyCode");
                    jtemp.put("refDataColumn", "currencycode");
                }
                if (isactivatedTodate && dh.getDefaultHeader().equals("Applied To")&& (moduleId.equals("123") || moduleId.equals("126"))) { //import currency exchange rate Todate is Activated
                    jtemp.put("isMandatory", true);
                    jtemp.put("isNotNull", true);
                }
                if(!StringUtil.isNullOrEmpty(dh.getPojoMethodName()) && dh.getPojoMethodName().equals("SalesCurrency")&&isCurrencyCode&&moduleId.equals("80")){
                     jtemp.put("refDataColumn", "currencycode");
                }
                jtemp.put("renderertype", dh.getRendererType() == null ? "none" : dh.getRendererType());
                jtemp.put("formfieldname", dh.getFormFieldName()!=null ? dh.getFormFieldName() : "");
                        jtemp.put("isConditionalMandetory", dh.isConditionalMandetory());
                jtemp.put("countryid", dh.getCountryID());
                if (moduleId.equalsIgnoreCase(""+Constants.Acc_Product_Master_ModuleId) || moduleId.equalsIgnoreCase(""+Constants.Acc_Assembly_Product_Master_ModuleId)) {
                    /*
                    * ERP-31005 - column-wise tool tip for below columns
                    * 1. Cost Of Goods Sold Account
                    * 2. Inventory Account
                    * 3. Stock Adjustment Account.
                    */
                    if(dh.getDefaultHeader().equals(Constants.defaultHeader_CostofGoodsSoldAccount) || dh.getDefaultHeader().equals(Constants.defaultHeader_InventoryAccount)
                            || dh.getDefaultHeader().equals(Constants.defaultHeader_StockAdjustmentAccount)){
                        
                        jtemp.put("qtip", "Selected column is mandatory when perpetual Inventory is activated.");
                    } else {
                        jtemp.put("qtip", "Selected column is mandatory for Inventory Part, Inventory Assembly,Non-Inventory Part,Inventory Non-Sale type of product.");
                    }
                } else if(moduleId.equalsIgnoreCase(Constants.Account_ModuleId)){
                    jtemp.put("qtip", "Selected column is mandatory for IBG Bank Details.");
                }else if(dh.isIsbatchdetail()){
                    jtemp.put("qtip", "Selected column is mandatory for product having Warehouse,Location,Batch,Row,Rack and Bin options activated.");
                }
                jtemp.put("islineitem", dh.isIslineitem());
                
                /**
                 * Update isMandatory and isNotnull flag for Customer/Vendor import
                 * for Billing and Shipping Address
                 */
                if (!isdocumentimport.equalsIgnoreCase("T") && (moduleId.equalsIgnoreCase(Constants.CUSTOMER_MODULE_UUID) || moduleId.equalsIgnoreCase(Constants.Vendor_MODULE_UUID))) {
                    addAddressFieldsValidationForNewGST(params, jtemp, dh.getId(), updateExistingRecordFlag);
                }
                jArr.put(jtemp);
            }
        } catch (JSONException ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    /**
     * Update isMandatory and isNotnull flag for Customer/Vendor import for
     * Billing and Shipping Address
     * @param params
     * @param jtemp
     * @param defaultHeaderID
     * @throws ServiceException
     */
    public void addAddressFieldsValidationForNewGST(Map<String, Object> params, JSONObject jtemp, String defaultHeaderID, boolean updateExistingRecordFlag) throws ServiceException {
        String moduleId = "", companyid = "";
        if (params.containsKey("moduleId") && params.get("moduleId") != null) {
            moduleId = (String) params.get("moduleId");
        }
        if (params.containsKey("companyid") && params.get("companyid") != null) {
            companyid = (String) params.get("companyid");
        }
        try {
            Company company = (Company) KwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
            String countryid = company != null  && company.getCountry() != null ? company.getCountry().getID() : "";
            List list = importDao.getExtraCompanyPref(companyid);
            if (!StringUtil.isNullOrEmpty(countryid) && list != null && !list.isEmpty() && list.get(0) != null) {
                Object[] extraCompanyPref = (Object[]) list.get(0);
                boolean isNewGST = extraCompanyPref[0] != null ? (String.valueOf(extraCompanyPref[0]).equalsIgnoreCase("1") || String.valueOf(extraCompanyPref[0]).equalsIgnoreCase("T")) : false;
                String columnPref = extraCompanyPref[1] != null ? (String) extraCompanyPref[1] : "";
                if (isNewGST) {
                    boolean isGSTCalBasedOnShippingAddress = false;
                    /**
                     * Added Try catch if any case columnpref JSON string not
                     * able to parse in JSON.
                     */
                    if (!StringUtil.isNullOrEmpty(columnPref)) {
                        JSONObject columnPrefJObj = new JSONObject(columnPref);
                        isGSTCalBasedOnShippingAddress = columnPrefJObj.optBoolean(Constants.columnPref.GSTCalculationOnShippingAddress.get(), false);
                    }
                    /**
                     * add fields mandatory if Billing State or Shipping State.
                     * Get Shipping or Billing tax calculation from
                     * extracompanypreferences -> columnpref ->
                     * isGSTCalculationOnShippingAddress
                     */
                    boolean isValidateAddressFields = false;
                    if (isGSTCalBasedOnShippingAddress) {
                        /**
                         * This condition for GST calculation based on Shipping
                         * Address
                         */
                        if (Integer.valueOf(countryid) == Constants.indian_country_id) {
                            if (defaultHeaderID.equalsIgnoreCase(Constants.VENDOR_SHIPP_STATE) || defaultHeaderID.equalsIgnoreCase(Constants.CUSTOMER_SHIPP_STATE)) {
                                isValidateAddressFields = true;
                            }
                        } else if (moduleId.equalsIgnoreCase(Constants.CUSTOMER_MODULE_UUID) && Integer.valueOf(countryid) == Constants.USA_country_id) {
                            if (defaultHeaderID.equalsIgnoreCase(Constants.CUSTOMER_SHIPP_STATE) || defaultHeaderID.equalsIgnoreCase(Constants.CUSTOMER_SHIPP_CITY) || defaultHeaderID.equalsIgnoreCase(Constants.CUSTOMER_SHIPP_COUNTY)) {
                                isValidateAddressFields = true;
                            }
                        }
                    } else if (!isGSTCalBasedOnShippingAddress) {
                        /**
                         * This condition for GST calculation based on Billing
                         * Address
                         */
                        if (Integer.valueOf(countryid) == Constants.indian_country_id) {
                            if (defaultHeaderID.equalsIgnoreCase(Constants.VENDOR_BILL_STATE) || defaultHeaderID.equalsIgnoreCase(Constants.CUSTOMER_BILL_STATE)) {
                                isValidateAddressFields = true;
                            }
                        } else if (moduleId.equalsIgnoreCase(Constants.CUSTOMER_MODULE_UUID) && Integer.valueOf(countryid) == Constants.USA_country_id) {
                            if (defaultHeaderID.equalsIgnoreCase(Constants.CUSTOMER_BILL_STATE) || defaultHeaderID.equalsIgnoreCase(Constants.CUSTOMER_BILL_CITY) || defaultHeaderID.equalsIgnoreCase(Constants.CUSTOMER_BILL_COUNTY)) {
                                isValidateAddressFields = true;
                            }
                        }
                    }
                    if (isValidateAddressFields) {
                        /**
                         * If updateExistingRecordFlag then address fields not mandatory
                         */
                        if (!updateExistingRecordFlag) {
                            jtemp.put("isMandatory", true);
                            jtemp.put("isNotNull", true);
                        }
                        jtemp.put(Constants.GSTAddressMapping, true);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }
    public String getColumnNameForModuleColumnConfig(Map<String, Object> params) {
        String columnName = "";
        String moduleId = "";
        DefaultHeader dh=null;
        int subModuleFlag=0;
        boolean isExpenseInvoiceImport=false;
        if(params.containsKey("moduleId") && params.get("moduleId")!=null){
            moduleId = (String) params.get("moduleId");
        }
        if(params.containsKey("subModuleFlag") && params.get("subModuleFlag")!=null){
            subModuleFlag = ((Integer)params.get("subModuleFlag")).intValue();
        }
        if(params.containsKey("defaultheader") && params.get("defaultheader")!=null){
            dh = (DefaultHeader)params.get("defaultheader");
        }
        if(params.containsKey("isExpenseInvoiceImport") && params.get("isExpenseInvoiceImport")!=null){
            isExpenseInvoiceImport = (Boolean) params.get("isExpenseInvoiceImport");  //User cannot import the without BOM file through import Assembly Product with BOM option
        }
        if ((moduleId.equalsIgnoreCase("" + Constants.Acc_Invoice_ModuleId) || moduleId.equalsIgnoreCase("" + Constants.Acc_Vendor_Invoice_ModuleId)) && dh.isIslineitem() && dh.getDefaultHeader().equalsIgnoreCase("Tax Name")) {
                columnName = "Product Tax";
        } else if ((moduleId.equalsIgnoreCase("" + Constants.Acc_Invoice_ModuleId) || moduleId.equalsIgnoreCase("" + Constants.Acc_Vendor_Invoice_ModuleId) || moduleId.equalsIgnoreCase("" + Constants.Acc_Sales_Order_ModuleId)) && dh.getDefaultHeader().equalsIgnoreCase("Tax Name")) {
            columnName = "Tax Code";
        } else {
            columnName = dh.getDefaultHeader();
        }
        return columnName;
    }
    
    public JSONArray getCustomModuleColumnConfig(String moduleId, String companyid, boolean isExport,HashMap<String, Object> params) throws ServiceException {
        JSONArray jArr = new JSONArray();
        int moduleid = 0;
        // if moduleid is for Account
        if (moduleId.equalsIgnoreCase(Constants.Account_ModuleId)) {
            moduleid = Constants.Account_Statement_ModuleId;
            moduleId = Integer.toString(moduleid);
            isExport=true;
        } else if (moduleId.equalsIgnoreCase(Constants.CUSTOMER_MODULE_UUID)) {
            moduleid = Constants.Acc_Customer_ModuleId ;
            isExport=true;
            moduleId = Integer.toString(moduleid);
        } else if (moduleId.equalsIgnoreCase(Constants.Vendor_MODULE_UUID)) {
            moduleid = Constants.Acc_Vendor_ModuleId ;
            isExport=true;
            moduleId = Integer.toString(moduleid);
        } else if (!StringUtil.isNullOrEmpty(moduleId)) {
            moduleid = Integer.parseInt(moduleId);
        }
        boolean updateExistingRecordFlag = false;
        if (params.containsKey("updateExistingRecordFlag") && params.get("updateExistingRecordFlag")!=null && !StringUtil.isNullOrEmpty(params.get("updateExistingRecordFlag").toString())) {   
            updateExistingRecordFlag = (Boolean)(params.get("updateExistingRecordFlag"));
        }
        List list = importDao.getCustomModuleColumnConfig(moduleId, companyid, isExport);
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JSONObject jtemp = new JSONObject();
                jtemp.put("id", row[0] != null ? row[0] : "");
                jtemp.put("isNotNull", row[1] != null ? (Integer.parseInt(row[1].toString()) == 0 ? false : true) : false);
                jtemp.put("maxLength", row[2] != null ? row[2] : "");
                jtemp.put("configid", "");
                if (row[3] != null) {
                    if (Integer.parseInt(row[3].toString()) == 4 || Integer.parseInt(row[3].toString()) == 7) {
                        if (moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId || moduleid == Constants.Acc_Product_Master_ModuleId 
                                || moduleid == Constants.Acc_opening_Sales_Invoice || moduleid == Constants.Acc_opening_Customer_CreditNote || moduleid == Constants.Acc_opening_Customer_DebitNote || moduleid == Constants.Acc_opening_Receipt || moduleid == Constants.Acc_opening_Prchase_Invoice || moduleid == Constants.Acc_opening_Payment || moduleid == Constants.Acc_opening_Vendor_CreditNote || moduleid == Constants.Acc_opening_Vendor_DebitNote) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "FieldComboData");
                        }

                        jtemp.put("refModule", "FieldComboData");
                        jtemp.put("validatetype", "custom");
                        jtemp.put("pojoHeader", "FieldComboData");
                        jtemp.put("refDataColumn", "value");
                        jtemp.put("refFetchColumn", "id");
                    } else if (Integer.parseInt(row[3].toString()) == 1 || Integer.parseInt(row[3].toString()) == 13) {
                        if (moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId || moduleid == Constants.Acc_Product_Master_ModuleId 
                                || moduleid == Constants.Acc_opening_Sales_Invoice || moduleid == Constants.Acc_opening_Customer_CreditNote || moduleid == Constants.Acc_opening_Customer_DebitNote || moduleid == Constants.Acc_opening_Receipt || moduleid == Constants.Acc_opening_Prchase_Invoice || moduleid == Constants.Acc_opening_Payment || moduleid == Constants.Acc_opening_Vendor_CreditNote || moduleid == Constants.Acc_opening_Vendor_DebitNote) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "");
                        }

                        jtemp.put("refModule", "");
                        jtemp.put("validatetype", "text");
                    } else if (Integer.parseInt(row[3].toString()) == 2) {
                        if (moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId || moduleid == Constants.Acc_Product_Master_ModuleId 
                                || moduleid == Constants.Acc_opening_Sales_Invoice || moduleid == Constants.Acc_opening_Customer_CreditNote || moduleid == Constants.Acc_opening_Customer_DebitNote || moduleid == Constants.Acc_opening_Receipt || moduleid == Constants.Acc_opening_Prchase_Invoice || moduleid == Constants.Acc_opening_Payment || moduleid == Constants.Acc_opening_Vendor_CreditNote || moduleid == Constants.Acc_opening_Vendor_DebitNote) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "");
                        }

                        jtemp.put("refModule", "");
                        jtemp.put("validatetype", "double");
                    } else if (Integer.parseInt(row[3].toString()) == 3) {
                        if (moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId || moduleid == Constants.Acc_Product_Master_ModuleId 
                                || moduleid == Constants.Acc_opening_Sales_Invoice || moduleid == Constants.Acc_opening_Customer_CreditNote || moduleid == Constants.Acc_opening_Customer_DebitNote || moduleid == Constants.Acc_opening_Receipt || moduleid == Constants.Acc_opening_Prchase_Invoice || moduleid == Constants.Acc_opening_Payment || moduleid == Constants.Acc_opening_Vendor_CreditNote || moduleid == Constants.Acc_opening_Vendor_DebitNote) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "");
                        }
                        jtemp.put("refModule", "");
                        jtemp.put("validatetype", "date");
                    } else if (Integer.parseInt(row[3].toString()) == 11) {
                        if (moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId || moduleid == Constants.Acc_Product_Master_ModuleId 
                                || moduleid == Constants.Acc_opening_Sales_Invoice || moduleid == Constants.Acc_opening_Customer_CreditNote || moduleid == Constants.Acc_opening_Customer_DebitNote || moduleid == Constants.Acc_opening_Receipt || moduleid == Constants.Acc_opening_Prchase_Invoice || moduleid == Constants.Acc_opening_Payment || moduleid == Constants.Acc_opening_Vendor_CreditNote || moduleid == Constants.Acc_opening_Vendor_DebitNote) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "");
                        }
                        jtemp.put("refModule", "");
                        jtemp.put("validatetype", "boolean");
                    } else {
                        if (moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId || moduleid == Constants.Acc_Product_Master_ModuleId 
                                || moduleid == Constants.Acc_opening_Sales_Invoice || moduleid == Constants.Acc_opening_Customer_CreditNote || moduleid == Constants.Acc_opening_Customer_DebitNote || moduleid == Constants.Acc_opening_Receipt || moduleid == Constants.Acc_opening_Prchase_Invoice || moduleid == Constants.Acc_opening_Payment || moduleid == Constants.Acc_opening_Vendor_CreditNote || moduleid == Constants.Acc_opening_Vendor_DebitNote) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "");
                        }
                        jtemp.put("refModule", "");
                        jtemp.put("validatetype", "");
                    }
                }

                jtemp.put("xtype", row[3] != null ? row[3] : "");
                if (updateExistingRecordFlag) {
                    jtemp.put("isMandatory", false);
                } else {
                    jtemp.put("isMandatory", row[1] != null ? (Integer.parseInt(row[1].toString()) == 0 ? false : true) : false);
                }
                jtemp.put("columnName", row[4] != null ? row[4] : "");
                jtemp.put("renderertype", "none");
                jtemp.put("fieldtype", row[5] != null ? (Integer.parseInt(row[5].toString()) == 0 ? "Global Custom Items" : "Line Level Custom  Items") : "Line Level Custom  Items");
                jtemp.put("customflag", true);
                if (isExport && (moduleid == Constants.Acc_Product_Master_ModuleId || moduleid == Constants.Account_Statement_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId 
                        || moduleid == Constants.Acc_opening_Sales_Invoice || moduleid == Constants.Acc_opening_Customer_CreditNote || moduleid == Constants.Acc_opening_Customer_DebitNote || moduleid == Constants.Acc_opening_Receipt || moduleid == Constants.Acc_opening_Prchase_Invoice || moduleid == Constants.Acc_opening_Payment || moduleid == Constants.Acc_opening_Vendor_CreditNote || moduleid == Constants.Acc_opening_Vendor_DebitNote)) {
                    jtemp.put("dataindex", row[6] != null ? row[6] : "");
                } else {
                    /*
                     * If diamension values are type of GSt Module 
                     */
                    if (moduleid == Constants.GSTModule) {
                        jtemp.put("dataindex", row[6] != null ? row[6] : ""); 
                        jtemp.put("moduleid", moduleId != null ? moduleId : "");
                        jtemp.put("gstconfigtype", row[10] != null ? row[10] : "");
                    }else{
                        jtemp.put("dataindex", "");
                    }
                }
                if (moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId || moduleid == Constants.Acc_Product_Master_ModuleId 
                        || moduleid == Constants.Acc_opening_Sales_Invoice || moduleid == Constants.Acc_opening_Customer_CreditNote || moduleid == Constants.Acc_opening_Customer_DebitNote || moduleid == Constants.Acc_opening_Receipt || moduleid == Constants.Acc_opening_Prchase_Invoice || moduleid == Constants.Acc_opening_Payment || moduleid == Constants.Acc_opening_Vendor_CreditNote || moduleid == Constants.Acc_opening_Vendor_DebitNote || moduleid == Constants.GSTModule) {
                    jtemp.put("refcolnum", row[7] != null ? row[7] : "");
                    jtemp.put("colnum", row[8] != null ? row[8] : "");
                }
                jtemp.put("relatedmoduleid", row[9] != null ? row[9] : "");
                jArr.put(jtemp);
            }
            /*
             * For display diamension  column on Validation window.
             */
            if (!StringUtil.isNullOrEmpty(moduleId) && moduleid==Constants.GSTModule) {
                params.put("termType", 7);
                List listGstTerm = importDao.getGSTTermDetails(params);
                Iterator gstTermItr = listGstTerm.iterator();
                while (gstTermItr.hasNext()) {
                    Object[] row = (Object[]) gstTermItr.next();
                    JSONObject jtemp = new JSONObject();
                    jtemp.put("id", row[0] != null ? row[0] : "");
                    jtemp.put("dataindex", row[1] != null ? row[1] : "");
                    jtemp.put("fieldtype", "Default GST Master");
                    jtemp.put("columnName", row[1] != null ? row[1] : "");
                    jtemp.put("pojoName", row[1] != null ? row[1] : "");
                    jtemp.put("isMandatory", true);
                    jtemp.put("xtype", 1);
                    jArr.put(jtemp);
                }
            }
                         
             
        } catch (JSONException ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    public HashMap<String,Object> getCustomModuleColumnConfigForSharingMastersData(String moduleId, String companyid, boolean isExport) throws ServiceException {
         HashMap<String,Object> columnConfigMap=new HashMap<String,Object>();
        int moduleid = 0;
        // if moduleid is for Account
        if (moduleId.equalsIgnoreCase(Constants.Account_ModuleId)) {
            moduleid = Constants.Account_Statement_ModuleId;
            moduleId = Integer.toString(moduleid);
        } else if (moduleId.equalsIgnoreCase(Constants.CUSTOMER_MODULE_UUID)) {
            moduleid = Constants.Acc_Customer_ModuleId ;
            isExport=true;
            moduleId = Integer.toString(moduleid);
        } else if (moduleId.equalsIgnoreCase(Constants.Vendor_MODULE_UUID)) {
            moduleid = Constants.Acc_Vendor_ModuleId ;
            isExport=true;
            moduleId = Integer.toString(moduleid);
        } else if (!StringUtil.isNullOrEmpty(moduleId)) {
            moduleid = Integer.parseInt(moduleId);
        }
        List list = importDao.getCustomModuleColumnConfig(moduleId, companyid, isExport);
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JSONObject jtemp = new JSONObject();
                jtemp.put("id", row[0] != null ? row[0] : "");
                jtemp.put("isNotNull", row[1] != null ? (Integer.parseInt(row[1].toString()) == 0 ? false : true) : false);
                jtemp.put("maxLength", row[2] != null ? row[2] : "");
                jtemp.put("configid", "");
                if (row[3] != null) {
                    if (Integer.parseInt(row[3].toString()) == 4 || Integer.parseInt(row[3].toString()) == 7) {
                        if (moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "FieldComboData");
                        }

                        jtemp.put("refModule", "FieldComboData");
                        jtemp.put("validatetype", "custom");
                        jtemp.put("pojoHeader", "FieldComboData");
                        jtemp.put("refDataColumn", "value");
                        jtemp.put("refFetchColumn", "id");
                    } else if (Integer.parseInt(row[3].toString()) == 1 || Integer.parseInt(row[3].toString()) == 13) {
                        if (moduleid == Constants.Acc_Product_Master_ModuleId || moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "");
                        }

                        jtemp.put("refModule", "");
                        jtemp.put("validatetype", "text");
                    } else if (Integer.parseInt(row[3].toString()) == 2) {
                        if (moduleid == Constants.Acc_Product_Master_ModuleId || moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "");
                        }

                        jtemp.put("refModule", "");
                        jtemp.put("validatetype", "integer");
                    } else if (Integer.parseInt(row[3].toString()) == 3) {
                        if (moduleid == Constants.Acc_Product_Master_ModuleId || moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "");
                        }
                        jtemp.put("refModule", "");
                        jtemp.put("validatetype", "date");
                    } else if (Integer.parseInt(row[3].toString()) == 11) {
                        if (moduleid == Constants.Acc_Product_Master_ModuleId || moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "");
                        }
                        jtemp.put("refModule", "");
                        jtemp.put("validatetype", "boolean");
                    } else {
                        if (moduleid == Constants.Acc_Product_Master_ModuleId || moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId) {
                            jtemp.put("pojoName", row[4] != null ? row[4] : "");
                        } else {
                            jtemp.put("pojoName", "");
                        }
                        jtemp.put("refModule", "");
                        jtemp.put("validatetype", "");
                    }
                }

                jtemp.put("xtype", row[3] != null ? row[3] : "");
                jtemp.put("isMandatory", row[1] != null ? (Integer.parseInt(row[1].toString()) == 0 ? false : true) : false);
                jtemp.put("columnName", row[4] != null ? row[4] : "");
                jtemp.put("renderertype", "none");
                jtemp.put("fieldtype", row[5] != null ? (Integer.parseInt(row[5].toString()) == 0 ? "Global Custom Items" : "Line Level Custom  Items") : "Line Level Custom  Items");
                jtemp.put("customflag", true);
                if (isExport && (moduleid == Constants.Acc_Product_Master_ModuleId || moduleid == Constants.Account_Statement_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId)) {
                    jtemp.put("dataindex", row[6] != null ? row[6] : "");
                } else {
                    jtemp.put("dataindex", "");
                }
                if (moduleid == Constants.Acc_Product_Master_ModuleId || moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId) {
                    jtemp.put("refcolnum", row[7] != null ? row[7] : "");
                    jtemp.put("colnum", row[8] != null ? row[8] : "");
                }
                String fieldName="Custom_"+row[4];
                columnConfigMap.put(fieldName,jtemp);
            }
        } catch (JSONException ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return columnConfigMap;
    }

    public void CheckUniqueRecord(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, Map<Integer,Object>invalidColumn,String classPath, String uniqueKeyMethodName, String uniqueKeyHbmName, String primaryKey, HashMap<String, Object> columnHeaderMap, HashMap<String, Object> columnCSVindexMap,Set modulePrimaryKeyValues ) throws DataInvalidateException {
        
        if (!StringUtil.isNullOrEmpty(uniqueKeyMethodName) && !StringUtil.isNullOrEmpty(uniqueKeyHbmName)) {
            int invalidColumnIndex=-1;
            String moduleName = "";
            if (requestParams.containsKey("moduleName") && requestParams.get("moduleName") != null) {
                moduleName = (String) requestParams.get("moduleName");
            }
            try {
                if(columnCSVindexMap.containsKey("uniqueKeyMethodName")){
                    invalidColumnIndex=(int)columnCSVindexMap.get(uniqueKeyMethodName);
                }
                Set<String> uniqueRecordModules = new HashSet<>();
                uniqueRecordModules.add("Product");
                uniqueRecordModules.add("Assembly Product");
                uniqueRecordModules.add("Vendor");
                uniqueRecordModules.add("Customer");
                uniqueRecordModules.add("Opening Sales Invoice");
                uniqueRecordModules.add("Opening Purchase Invoice");
                uniqueRecordModules.add("Opening Receipt");
                uniqueRecordModules.add("Opening Payment");
                uniqueRecordModules.add("Opening Customer Credit Note");
                uniqueRecordModules.add("Opening Vendor Credit Note");
                uniqueRecordModules.add("Opening Customer Debit Note");
                uniqueRecordModules.add("Opening Vendor Debit Note");
                uniqueRecordModules.add("Convert Sales Invoice in to Cash Sales");// ERP-32642 - check duplicate invoice number in file
                if (uniqueRecordModules.contains(moduleName) && !moduleName.equals("Assembly Product")) {   //SDP-11688
                    Object uniqueKeyObj=dataMap.get(uniqueKeyMethodName);
                    if (!StringUtil.isNullObject(uniqueKeyObj) && !StringUtil.isNullOrEmpty(uniqueKeyObj.toString()) ) {
                        boolean added = modulePrimaryKeyValues.add(dataMap.get(uniqueKeyMethodName));
                        if (!added) {
                            throw new DataInvalidateException("Duplicate " + columnHeaderMap.get(uniqueKeyMethodName) + " '" + dataMap.get(uniqueKeyMethodName) + "' in file");
                        }
                    } else {//value of unique key is empty or null  
                        throw new DataInvalidateException("Empty data found in " + columnHeaderMap.get(uniqueKeyMethodName) + ", cannot set empty data for " + columnHeaderMap.get(uniqueKeyMethodName) + ".");
                    }
                }
                boolean updateExistingRecordFlag = false;
                if (requestParams.containsKey("updateExistingRecordFlag") && requestParams.get("updateExistingRecordFlag") != null) {
                    updateExistingRecordFlag = (Boolean) requestParams.get("updateExistingRecordFlag");
                }

                boolean allowDuplcateRecord = false;
                if (requestParams.containsKey("allowDuplcateRecord") && requestParams.get("allowDuplcateRecord") != null) {
                    allowDuplcateRecord = (Boolean) requestParams.get("allowDuplcateRecord");
                }


                if(!moduleName.equals("Convert Sales Invoice in to Cash Sales")){// ERP-32642 - No need to check duplicate number in database.
                    //@@@ What for CrmAccount table.
                    Object uniqueKeyObj=dataMap.get(uniqueKeyMethodName);
                    if (!StringUtil.isNullObject(uniqueKeyObj) && !StringUtil.isNullOrEmpty(uniqueKeyObj.toString()) ) { //value of unique key is not empty or null 
                        List list = getRefDataWithPrimaryKey(requestParams, classPath, uniqueKeyHbmName, uniqueKeyHbmName, "", dataMap.get(uniqueKeyMethodName));
                        if (!list.isEmpty()) {
                            if (updateExistingRecordFlag) {
                                // Get ID of existing record
                                Object[] dataArray = (Object[]) list.get(0);
                                String uniqueKeyColumnValue = dataArray[0].toString();
                                String primaryKeyValue = dataArray[1].toString();
                                dataMap.put(primaryKey, primaryKeyValue);
                            } else {
                                String validationMsg = null;
                                if (allowDuplcateRecord) {
                                    Set<String> skippedModules = new HashSet<>();
                                    skippedModules.add("Product");
                                    skippedModules.add("Vendor");
                                    skippedModules.add("Customer");
                                    if (!skippedModules.contains(moduleName)) { // for product module if update existing is allowed, do not show it as invalid data
                                        Object[] dataArray = (Object[]) list.get(0);

                                        String primaryKeyValue = dataArray[1].toString();

                                        String msg = getNonEditableFields(requestParams, primaryKeyValue);

                                        validationMsg = " already exists so it will get update" + msg;
                                    }
                                } else {
                                    validationMsg = " already exists.";
                                }
                                if (!StringUtil.isNullOrEmpty(validationMsg)) {
                                    throw new DataInvalidateException("Record for " + columnHeaderMap.get(uniqueKeyMethodName) + " " + dataMap.get(uniqueKeyMethodName) + validationMsg);
                                }
                            }
                        }else if(allowDuplcateRecord && (moduleName.equalsIgnoreCase("Customer") ||moduleName.equalsIgnoreCase("Vendor") )){
                            throw new DataInvalidateException("Record for " + columnHeaderMap.get(uniqueKeyMethodName) + " " + dataMap.get(uniqueKeyMethodName) + " not present in Master");
                        }
                    } else {//value of unique key is empty or null  
                        throw new DataInvalidateException("Empty data found in " + columnHeaderMap.get(uniqueKeyMethodName) + ", cannot set empty data for " + columnHeaderMap.get(uniqueKeyMethodName) + ".");
                    }
                }
            } catch (DataInvalidateException ex) {
                try{
                    if (moduleName.equals("Vendor") || moduleName.equals("Customer")) {
                        invalidColumn.put(invalidColumnIndex-1, "Invalid");//-1 for exact index location
                    }
                    JSONObject errorLog = new JSONObject();
                    errorLog.put("errorMsg", ex.getMessage());
                    errorLog.put("invalidColumns", ("col" + invalidColumnIndex + ","));
                    throw new DataInvalidateException (errorLog.toString());
                } catch(JSONException jsonEx){
                    
                }
            } catch (ServiceException ex) {
                if (moduleName.equals("Vendor") || moduleName.equals("Customer")) {
                    invalidColumn.put(invalidColumnIndex-1, "Invalid");//-1 for exact index location
                }
                throw new DataInvalidateException("Incorrect reference mapping for unique key column " + uniqueKeyMethodName);
            }
        }
    }
    
    public void CheckGlobalLevelData(HashMap<String, Object> requestParams, JSONArray columnConfigArray, HashMap<String, Object> dataMap, String uniqueKeyMethodName, Set globalDatakeySet, Set modulePrimaryKeyValueSet) throws DataInvalidateException {
        String moduleName = "";
        if (requestParams.containsKey("moduleName") && requestParams.get("moduleName") != null) {
            moduleName = (String) requestParams.get("moduleName");
        }
        Set<String> modulesList = new HashSet<>();
        modulesList.add("Delivery Order");
        modulesList.add("Purchase Order");
        modulesList.add("Receipt");
        if (modulesList.contains(moduleName) && dataMap.containsKey(uniqueKeyMethodName) && dataMap.get(uniqueKeyMethodName)!=null && !StringUtil.isNullOrEmpty(dataMap.get(uniqueKeyMethodName).toString())){
            StringBuffer globalDatakey = new StringBuffer();
            for (int k = 0; k < columnConfigArray.length(); k++) {
                try {
                    JSONObject columnConfig = null;
                    String column = "";
                    columnConfig = columnConfigArray.getJSONObject(k);
                    if (columnConfig.has("islineitem") && !columnConfig.isNull("islineitem") && !columnConfig.getBoolean("islineitem")) {
                        column = columnConfig.optString("pojoName");
                        if(dataMap.containsKey(column) && dataMap.get(column)!=null){
                            globalDatakey.append(dataMap.get(column).toString());
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            boolean added=globalDatakeySet.add(globalDatakey.toString());
            if(modulePrimaryKeyValueSet.contains(dataMap.get(uniqueKeyMethodName).toString())){
                if(added){
                    throw new DataInvalidateException("Global level data are not same for document no "+dataMap.get(uniqueKeyMethodName).toString());
                }
            } else {
                modulePrimaryKeyValueSet.add(dataMap.get(uniqueKeyMethodName).toString());
            }
        }
    }
    
    public String getNonEditableFields(HashMap<String, Object> requestParams, String primaryKeyValue) throws ServiceException, DataInvalidateException {
        String moduleName = "";
        if (requestParams.containsKey("moduleName") && requestParams.get("moduleName") != null) {
            moduleName = (String) requestParams.get("moduleName");
        }

        String companyId = requestParams.get("companyid").toString();

        String textMsg = ".";

        if (!StringUtil.isNullOrEmpty(moduleName)) {
            if (moduleName.equalsIgnoreCase("Accounts")) {
                boolean isAccountHavingTransactions = importDao.isAccountHavingTransactions(primaryKeyValue, companyId);
                if (isAccountHavingTransactions) {
                    textMsg = " except Account Type, Master Type, Group, Creation Date, Currency Fields as this account is linked with other documents or has opening amount.";
                }

            }
            if (moduleName.equalsIgnoreCase("Customer") || moduleName.equalsIgnoreCase("Vendor")) {
                textMsg = " except Creation Date Field.";
            }

            if (moduleName.equalsIgnoreCase("Product")) {
                textMsg = " except Product Type, Initial Purchase Price, Sales Price, Activate Batches, Activate Serial No, Activate Warehouse, Activate Location Fields as in case this product is linked with other documents.";
            }
        }

        return textMsg;

    }

    public void validateColumnData(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, JSONObject columnConfig, String column, JSONArray customfield, HashMap<String, Object> columnHeaderMap, String dateFormat, Map rowRefDataMap) throws JSONException, DataInvalidateException, ParseException, ServiceException {
        int maxLength = columnConfig.getInt("maxLength");
        boolean action = (requestParams.containsKey("action")  ? (((String)requestParams.get("action")).equals("import") ? true : false ) :false);
        boolean isMandatory=columnConfig.optBoolean("isNotNull",false);
        boolean isCondionalMandatory=columnConfig.optBoolean("isConditionalMandetory",false);
        boolean isMandatoryConditionFound=isCondionalMandatory?isMandatoryConditionForColumn(requestParams,dataMap,column):false;//Some specific columns are become mandatory for specific cases to handle those columns this code is written
        boolean IndiaCountryCheck = (requestParams.containsKey("countryid") && Integer.parseInt(requestParams.get("countryid").toString()) == Constants.indian_country_id);
        boolean isTDSapplicable = (requestParams.containsKey("isTDSapplicable") && Boolean.parseBoolean(requestParams.get("isTDSapplicable").toString()));
        boolean isWarehouseCompulsory = (requestParams.containsKey("isWarehouseCompulsory") && Boolean.parseBoolean(requestParams.get("isWarehouseCompulsory").toString()));
        boolean isLocationCompulsory = (requestParams.containsKey("isLocationCompulsory") && Boolean.parseBoolean(requestParams.get("isLocationCompulsory").toString()));
        JSONObject columnPrefJObj = null;
        if (requestParams.containsKey("columnPref") && requestParams.get("columnPref")!=null && !StringUtil.isNullOrEmpty(requestParams.get("columnPref").toString())) {
            columnPrefJObj = new JSONObject((String) requestParams.get("columnPref"));
        }
        String companyid = "";
        if (requestParams.containsKey("companyid")) {
            companyid = (String) requestParams.get("companyid");
        }
        if (IndiaCountryCheck && isTDSapplicable) {
            //For Vendor
            String isTDSapplicableonvendor_Value = dataMap.get("IsTDSapplicableonvendor") != null ? dataMap.get("IsTDSapplicableonvendor").toString() : null;
            if (!StringUtil.isNullOrEmpty(isTDSapplicableonvendor_Value)) {
                boolean TDSapplicableOnParticularVendor = true;
                if (isTDSapplicableonvendor_Value.equalsIgnoreCase("true") || isTDSapplicableonvendor_Value.equalsIgnoreCase("1") || isTDSapplicableonvendor_Value.equalsIgnoreCase("T") || isTDSapplicableonvendor_Value.equalsIgnoreCase("YES")) {
                    TDSapplicableOnParticularVendor = true;
                } else if (isTDSapplicableonvendor_Value.equalsIgnoreCase("false") || isTDSapplicableonvendor_Value.equalsIgnoreCase("0") || isTDSapplicableonvendor_Value.equalsIgnoreCase("F") || isTDSapplicableonvendor_Value.equalsIgnoreCase("NO")) {
                    TDSapplicableOnParticularVendor = false;
                }
                isTDSapplicable = TDSapplicableOnParticularVendor;
            }
            //For Customer
            String isTDSapplicableonCustomer_Value = dataMap.get("IsTDSapplicableoncust") != null ? dataMap.get("IsTDSapplicableoncust").toString() : null;
            if (!StringUtil.isNullOrEmpty(isTDSapplicableonCustomer_Value)) {
                boolean TDSapplicableOnParticularCustomer = true;
                if (isTDSapplicableonCustomer_Value.equalsIgnoreCase("true") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("1") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("T") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("YES")) {
                    TDSapplicableOnParticularCustomer = true;
                } else if (isTDSapplicableonCustomer_Value.equalsIgnoreCase("false") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("0") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("F") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("NO")) {
                    TDSapplicableOnParticularCustomer = false;
                }
                isTDSapplicable = TDSapplicableOnParticularCustomer;
            }
            }
        
        String csvHeader = (String) columnHeaderMap.get(column);
        csvHeader = (csvHeader == null ? csvHeader : csvHeader.replaceAll("\\.", " "));//remove '.' from csv Header
        String columnHeader = columnConfig.getString("columnName");
        /**
          * If custom field have value of multiple line not remove from \n from that. SDP-14482 
          */
        boolean customfielddata = columnConfig.optBoolean("customflag",false);
        String data = "";
        if (columnHeader.equals("Billing Address") || columnHeader.equals("Shipping Address") || columnHeader.equals("Description") || columnHeader.equals("Description in foreign language") || columnHeader.equals("Additional Description") || customfielddata){
            data = dataMap.get(column) == null ? null : dataMap.get(column).toString();
        } else {
            data = dataMap.get(column) == null ? null : dataMap.get(column).toString().replaceAll("\n", " ");
        }
        Object vDataValue = data;
        String createUomSchemaTypeFlag = "";
        if (!StringUtil.isNullOrEmpty((String) requestParams.get("createUomSchemaTypeFlag"))) {
            createUomSchemaTypeFlag = (String) requestParams.get("createUomSchemaTypeFlag");
        }
        Locale locale = null;
        if(requestParams.containsKey("locale")){
            locale = (Locale) requestParams.get("locale");
        }
        Set openingModuleSet = new HashSet();
        openingModuleSet.add("Opening Sales Invoice");
        openingModuleSet.add("Opening Purchase Invoice");
        openingModuleSet.add("Opening Receipt");
        openingModuleSet.add("Opening Payment");
        openingModuleSet.add("Opening Customer Credit Note");
        openingModuleSet.add("Opening Vendor Credit Note");
        openingModuleSet.add("Opening Customer Debit Note");
        openingModuleSet.add("Opening Vendor Debit Note");
        openingModuleSet.add("Sales Order");
        openingModuleSet.add("Purchase Order");
                
        Map refColumnData = (rowRefDataMap.containsKey(column) && rowRefDataMap.get(column) != null)  ? (Map) rowRefDataMap.get(column) : null;

        String ContactNo = "Contact Number", AltContactNo = "Alternate Contact Number";	// Validation for phone numbers only Neeraj
        if (columnHeader.equals(ContactNo) || columnHeader.equals(AltContactNo)) {
            if (data != null && data.length() < 10) {
                throw new DataInvalidateException("Data length less than 10 for column " + csvHeader + ".");
            } else if (data != null && data.length() > 25) {
                throw new DataInvalidateException("Data length more than 25 for column " + csvHeader + ".");
            }
        }

        String mode = (String) requestParams.get("modName");
        String pref = (String) requestParams.get("masterPreference"); //0:Skip Record, 1:Skip Column, 2:Add new

        if (mode.equalsIgnoreCase("Product") && columnHeader.equals("Cycle Count Frequency")) {
            if (!dataMap.get("Producttype").toString().equalsIgnoreCase("Service") && !dataMap.get("Producttype").toString().equalsIgnoreCase("4efb0286-5627-102d-8de6-001cc0794cfa")) {
                if (!StringUtil.isNullOrEmpty(data)) {
                    String[] frqs = data.split(",");
                    String notFoundNames = "";
                    for (int c = 0; c < frqs.length; c++) {
                        if (!("Daily".equalsIgnoreCase(frqs[c]) || "Weekly".equalsIgnoreCase(frqs[c]) || "Fortnightly".equalsIgnoreCase(frqs[c]) || "Monthly".equalsIgnoreCase(frqs[c]))) {
                            notFoundNames += frqs[c] + ",";
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(notFoundNames) && pref.equalsIgnoreCase("0")) {
                        notFoundNames = notFoundNames.substring(0, notFoundNames.lastIndexOf(","));
                        throw new DataInvalidateException("Cycle Count Frequency is not found for " + notFoundNames);
                    }
                }
            }
        }
        if (columnConfig.has("validatetype")) {
            String validatetype = columnConfig.getString("validatetype");
            boolean customflag = false;
            if (columnConfig.has("customflag")) {
                customflag = columnConfig.getBoolean("customflag");
            }
            if (validatetype.equalsIgnoreCase("integer")) {
                try {
                    if (!StringUtil.isNullOrEmpty(data)) { // Remove ","(comma) from number
                        data = data.replaceAll(",", "");
                    }
                    if (StringUtil.isNullOrEmpty(data)) {
                        if (isMandatory || isMandatoryConditionFound) {//most resticted or data is mandatory
                            throw new DataInvalidateException(messageSource.getMessage("acc.export.Emptydatafoundin", null, locale)+" " + csvHeader + ", "+messageSource.getMessage("acc.export.cannotsetemptydatafor", null, locale)+" " + columnHeader + ".");
                        } else {
                            vDataValue = 0; 
                        }
                    } else if (maxLength > 0 && data != null && data.length() > maxLength) {// Added null value check for data[Sandeep k]
                        throw new DataInvalidateException("Data length greater than " + maxLength + " for column " + csvHeader + ".");
                    } else {//block for parsing data into integer
                        // this is had codded check due to xls file read all integer numbers as double so handle this having this check
                        if (mode.equalsIgnoreCase("Product") && requestParams.containsKey("importMethod") && requestParams.get("importMethod") != null && requestParams.get("importMethod").toString().equalsIgnoreCase("xls") && (column.equalsIgnoreCase("CountInterval") || column.equalsIgnoreCase("LeadTimeInDays") || column.equalsIgnoreCase("Tolerance") || column.equalsIgnoreCase("Warrantyperiodsal") || column.equalsIgnoreCase("Warrantyperiod"))) {
                            vDataValue = ((Double) Double.parseDouble(data)).intValue();
                        } else {
                            vDataValue = Integer.parseInt(data);
                        }
                    }
                } catch (NumberFormatException ex) { //when exception came under parsing
                    if (pref.equals("1") && !isMandatory) {//add empty case when field is not mandatory
                            vDataValue = 0;
                    } else {
                        throw new DataInvalidateException("Incorrect numeric value for " + csvHeader + ", Please ensure that value type of " + csvHeader + " matches with the " + columnHeader + ".");
                    }
                } catch (Exception ex) {
                    throw new DataInvalidateException(ex.getMessage());
                }
            } else if (validatetype.equalsIgnoreCase("double")) {
                try {
                    if (!StringUtil.isNullOrEmpty(data)) { // Remove ","(comma) from number
                        data = data.replaceAll(",", "");
                        if (columnHeader.equals("Credit Sales Limit") || columnHeader.equals("Credit Purchase Limit")|| columnHeader.equals("Batch Quantity")) {			//TODO: replace currency symbol or any alphabet from currency header
                            data = data.replaceAll("[^.0-9]", "");
                        }
                    }
                    if (StringUtil.isNullOrEmpty(data)) {
                        if (isMandatory) {//most resticted or data is mandatory
                            if((columnHeader.equals("Batch Quantity") || columnHeader.equals("Initial Quantity")) && dataMap.containsKey("isBatchQtyMandatory") && ((Boolean)dataMap.get("isBatchQtyMandatory")) != true){
                                vDataValue = 0;
                            }else{
                                throw new DataInvalidateException(messageSource.getMessage("acc.export.Emptydatafoundin", null, locale)+" " + csvHeader + ", "+messageSource.getMessage("acc.export.cannotsetemptydatafor", null, locale)+" "+ columnHeader + ".");
                            }
                        } else {
                            vDataValue = 0;
                        }
                    } else if (maxLength > 0 && data != null && data.length() > maxLength) {
                        throw new DataInvalidateException("Data length greater than " + maxLength + " for column " + csvHeader + ".");
                    } else {
                        vDataValue = Double.parseDouble(data);
                        String itemHeight = "Item Height", itemLength = "Item Length";	//SDP-11271
                        if (columnHeader.equals(itemHeight) || columnHeader.equals(itemLength)) {
                            if (data.length() > 11) {
                                throw new DataInvalidateException("Data length greater than 11 for column " + csvHeader + ".");
                            }
                        }
                        // ERP-39163 - Round off decimal value.
                        if(mode.equalsIgnoreCase("Accounts") && (columnHeader.equals("Opening Balance") || columnHeader.equals("Minimum Budget Limit"))){
                            vDataValue = authHandler.round((double) vDataValue, companyid);
                        }
                        // ERP-39347 - Value should be greater than zero 0.
                        if((double) vDataValue<=0){
                            if((mode.equalsIgnoreCase("Payment") || mode.equalsIgnoreCase("Receipt")) && columnHeader.equals("Enter Amount")){
                                throw new DataInvalidateException(columnHeader+" should be greater than Zero.");
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(data) && Double.parseDouble(data) < 0 && (columnHeader.equals("Unit Price") || columnHeader.equals("Discount") || columnHeader.equals("Tax Amount") || columnHeader.equals("Unit Price Including GST") || columnHeader.equals("Initial Quantity") || (mode.equalsIgnoreCase("Cash Purchase") && columnHeader.equals("Amount")))) {
                        throw new DataInvalidateException(columnHeader+" should not be negative.");
                    }
                } catch (NumberFormatException ex) {//when exception came under parsing
                    if (pref.equals("1") && !isMandatory) {//add empty case when field is not mandatory
                        vDataValue = 0.0;
                    } else {
                        throw new DataInvalidateException("Incorrect numeric value for " + csvHeader + ", Please ensure that value type of " + csvHeader + " matches with the " + columnHeader + ".");
                    }
                } catch (Exception ex) {
                    throw new DataInvalidateException(ex.getMessage());
                }
            } else if (validatetype.equalsIgnoreCase("date")) {
                String ldf = dateFormat != null ? dateFormat : (data==null?df:(data.length() > 10 ? df_full : df));
                if (!StringUtil.isNullOrEmpty(data)) {//when data given
                    vDataValue=validateDataForDate(requestParams,columnConfig,csvHeader,vDataValue,data,dateFormat);
                    if ((mode.equalsIgnoreCase("Currency Exchange") || mode.equalsIgnoreCase(Constants.Tax_Currency_Exchange)) && column.equalsIgnoreCase("toDate") && requestParams.containsKey("isActivateToDate") && (boolean) requestParams.get("isActivateToDate")) {
                        DateFormat sdf = new SimpleDateFormat(ldf);
                        Date appliedfrom = (Date) dataMap.get("applyDate");
                        String Appledtodate = (String) dataMap.get("toDate");
                        Date Appliedto = sdf.parse(Appledtodate);
                        if (appliedfrom.compareTo(Appliedto) > 0) {
                            throw new DataInvalidateException("Applied Form date can't be greater than Applied To date.");
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(column) && (column.equals("OrderDate") || column.equals("CreationDate") || ((mode.equalsIgnoreCase("Customer Invoices") || mode.equalsIgnoreCase("Cash Sales")) && column.equals("EntryDate")))) {
                        boolean isInActivePeriod = importDao.checkActiveDateRange((Date)vDataValue,companyid);
                        authHandlerDAOObj.checkLockDatePeroid((Date)vDataValue,companyid);
                        if(!isInActivePeriod){
                            throw new DataInvalidateException("Transaction cannot be completed. Date must belong to Active Date Range Period.");
                        }
                    }
                } else {
                    //Clearence Date should be entered in the case of Bank Reconciliation means payment status set to be cleared.
                    if (mode.equalsIgnoreCase(Constants.Acc_Opening_Receipt_modulename) || mode.equalsIgnoreCase(Constants.Acc_Opening_Payment_modulename) || ((mode.equalsIgnoreCase("Cash Sales") || mode.equalsIgnoreCase("Convert Sales Invoice in to Cash Sales") && columnHeader.equalsIgnoreCase("Clearance Date")))) {
                        if (dataMap.containsKey("PaymentStatus") && !StringUtil.isNullObject(dataMap.get("PaymentStatus")) && !StringUtil.isNullOrEmpty(dataMap.get("PaymentStatus").toString())) {
                            if (dataMap.get("PaymentStatus").equals("Cleared")) {
                                throw new DataInvalidateException("You have entered the Payment Status as Cleared. So you cannot set empty data for " + columnHeader + ".");
                            }
                        }
                    }
                    // if mandatory then throwing exception
                    if(isMandatory){//most resticted
                        throw new DataInvalidateException(messageSource.getMessage("acc.export.Emptydatafoundin", null, locale)+" " + csvHeader + ","+messageSource.getMessage("acc.export.cannotsetemptydatafor", null, locale)+" " + columnHeader + ".");
                    } else {// setting default value
                        if(columnConfig.has("defaultValue")){
                            vDataValue=getDefaultValue(columnConfig,requestParams);
                        } else {
                            vDataValue=null;
                        }
                    } 
                }
            } else if (validatetype.equalsIgnoreCase("time")) {
                if (!StringUtil.isNullOrEmpty(data)) {
                    //@@@ need to uncomment
//                    Pattern pattern = Pattern.compile(EmailRegEx);
//                    if(!pattern.matcher(data).matches()){
//                        throw new DataInvalidateException("Incorrect time format for "+columnConfig.getString("columnName")+" use HH:MM AM or PM");
//                    }
                    vDataValue = data;
                } else {
                    vDataValue = null;
                }
            } else if (validatetype.equalsIgnoreCase("ref")) {
                if (!StringUtil.isNullOrEmpty(data)) {
                    try {
                        if (columnConfig.has("refModule") && columnConfig.has("refDataColumn") && columnConfig.has("refFetchColumn") && columnConfig.has("configid")) {
                        List list = null;
                            if(refColumnData != null && refColumnData.containsKey(vDataValue)){
                                list = (List)refColumnData.get(vDataValue);
                            }else{
                                if (columnConfig.getString("refModule").equalsIgnoreCase("LandingCostCategory") && data.contains(",")) {
                                    String[] lcc = data.split(",");
                                    for (int i = 0; i < lcc.length; i++) {
                                        list = getRefData(requestParams, columnConfig.getString("refModule"), columnConfig.getString("refDataColumn"), columnConfig.getString("refFetchColumn"), columnConfig.getString("configid"), lcc[i]);
                                        refColumnData.put(vDataValue, list);
                                        if (list == null || list.size() == 0 || list.get(0) == null) {
                                            data = lcc[i];
                                            break;
                                        }
                                    }
                                } else {
                                    list = getRefData(requestParams, columnConfig.getString("refModule"), columnConfig.getString("refDataColumn"), columnConfig.getString("refFetchColumn"), columnConfig.getString("configid"), data);
                                    refColumnData.put(vDataValue, list);
                                }
                            }
                            
                            if (list.size() == 0 && (mode.equalsIgnoreCase("Customer") || mode.equalsIgnoreCase("Vendor")) && (columnHeader.equals("Account"))) {
                                throw new DataInvalidateException("Incorrect reference mapping(" + columnHeader + ") for " + csvHeader + ".");
                            } else if (mode.equalsIgnoreCase("Product Category") && data.equalsIgnoreCase("None")) {
                                vDataValue = data;
                            } else if (list.size() == 0) {
                                if(isCondionalMandatory){// If is condition mandatory then we check the condition is satifying or not for conditional mandatory.
                                    if (isMandatoryConditionFound) {
                                        if(mode.equalsIgnoreCase("Product") && columnHeader.equals("Stock UOM") && pref.equalsIgnoreCase("2")){
                                            vDataValue=data; //Stock UOM can be created at run time so for case 2 not givig any exception
                                        } else{
                                            throw new DataInvalidateException(csvHeader + " entry not found in master list for " + columnHeader + " dropdown."); // Throw ex to skip record.
                                        }
                                    } else {
                                        vDataValue=null;
                                    }                              
                                } else if (pref.equalsIgnoreCase("0")) { //Skip Record Most restricted.
                                    boolean giveValidationMessage=true;
                                    if(mode.equalsIgnoreCase("UOM Schema") && columnHeader.equals("Schema Name") && createUomSchemaTypeFlag.equals("true")){
                                        giveValidationMessage = false;
                                        vDataValue = null;
                                    }
                                    if(giveValidationMessage){
                                        throw new DataInvalidateException(csvHeader + " entry not found in master list for " + columnHeader + " dropdown."); // Throw ex to skip record.
                                    }
                                    
                                } else if (pref.equalsIgnoreCase("1")) {
                                    vDataValue = null;  // Put 'null' value to skip column data.
                                    if (isMandatory) {
                                        throw new DataInvalidateException(csvHeader + " entry not found in master list for " + columnHeader + " dropdown, cannot set empty data for " + columnHeader + ".");
                                    }
                                } else if (pref.equalsIgnoreCase("2")) {
                                    boolean isSkip = false;       // ERP-20234-"isSkip" flag to skip validation while Import UOM Schema with selection of "Create UOM Schema Type If Not Present" checkbox.
                                    if(mode.equalsIgnoreCase("UOM Schema") && columnHeader.equals("Schema Name")){
                                        if(createUomSchemaTypeFlag.equals("true")){
                                            isSkip=true; 
                                       }
                                    }
                                    if((mode.equalsIgnoreCase("Opening Sales Invoice") && columnHeader.equals("Sales Person")) || (mode.equalsIgnoreCase("Opening Purchase Invoice") && columnHeader.equals("Agent (Salesman)"))  || (!isMasterTable(columnConfig.getString("refModule")) && !isSkip)){ // Cant't create entry for ref. module
                                        throw new DataInvalidateException(csvHeader + " "+messageSource.getMessage("acc.import.entrynotpresentin", null, locale)+" " + columnHeader + " "+messageSource.getMessage("acc.import.listPleasecreatenew", null, locale)+" " + columnHeader + " "+messageSource.getMessage("acc.import.entryfor", null, locale)+" '"+(data.replaceAll("\\.", ""))+"' "+messageSource.getMessage("acc.import.asitrequiressomeotherdetails", null, locale));
                                    }
                                }
                            } else if (!list.isEmpty() && (mode.equals("Accounts") || mode.equals("Credit Note") || mode.equals("Payment") || mode.equalsIgnoreCase("Receipt")) && (columnHeader.equals("Map Tax") || columnHeader.equals("Tax"))) {
                                /**
                                 * In "else" block of above "if" block, was assigning random value of tax like "vDataValue = list.get(0).toString();" without checking activated tax code
                                 * so we have get only activated tax code id & assigned to "vDataValue" in import document
                                 * Changes done in ERP-40963.
                                 */
                                Map taxMap = new HashMap<>();
                                taxMap.put(Constants.companyKey, companyid);
                                taxMap.put(Constants.TAXCODE, data);
                                ArrayList taxList = getTax(taxMap);
                                if (!StringUtil.isNullOrEmpty((String) taxList.get(1))) {
                                    String taxId = (String) taxList.get(1);
                                    vDataValue = taxId;
                                } else if (!StringUtil.isNullOrEmpty((String) taxList.get(2))) {
                                    /**
                                     * If value of 0th or 1st index of taxlist is null
                                     * then tax code is deactivated or tax code is
                                     * not found.
                                     */
                                    String failureMsg = (String) taxList.get(2) + data;
                                    throw new DataInvalidateException(failureMsg);
                                }
                            } else {
                                if (action && columnConfig.getString("refModule").equals("Account") && (mode.equalsIgnoreCase("Customer") || mode.equalsIgnoreCase("Vendor"))) {
                                   String usedIn="";
                                    if (mode.equalsIgnoreCase("Customer")) {
                                        usedIn = "Customer Default Account";
                                    }
                                    if (mode.equalsIgnoreCase("Vendor")) {
                                        usedIn = "Vendor Default Account";
                                    }
                                    updateManualJePostSettingForAccount(list.get(0).toString(), usedIn);
                                } 
//                                else if (mode.equals("Delivery Order") && columnConfig.getString("refModule").equals("Product")) {
//                                    KwlReturnObject result = importDao.getProductIdIfLocationWarehouseBatchSerialIsActivated(list.get(0).toString());
//                                    int count = result.getRecordTotalCount();
//                                    if (count != 0) {
//                                        throw new DataInvalidateException("Cannot import record for product having warehouse/location/batch/serial activated. ");
//                                    }
//                                }
                                vDataValue = list.get(0).toString();
                            }

                        } else {
                            throw new DataInvalidateException("Incorrect reference mapping(" + columnHeader + ") for " + csvHeader + ".");
                        }
                    } catch (ServiceException ex) {
                        throw new DataInvalidateException("Incorrect reference mapping(" + columnHeader + ") for " + csvHeader + ".");
                    } catch (DataInvalidateException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        throw new DataInvalidateException(csvHeader + " entry not found in master list for " + columnHeader + " dropdown.");
                    }
                } else if(openingModuleSet.contains(mode) && columnHeader.equals("Customer Code") && dataMap.containsKey("CustomerName") && !StringUtil.isNullObject(dataMap.get("CustomerName")) && !StringUtil.isNullOrEmpty(dataMap.get("CustomerName").toString()) ){
                    //for opening transactions if data is empty in file for customer code then need to check for name
                    List list;
                    String customerName = dataMap.get("CustomerName").toString();
                    if (refColumnData != null && refColumnData.containsKey(customerName)) {
                        list = (List) refColumnData.get(customerName);
                    } else {
                        list = getRefData(requestParams, columnConfig.getString("refModule"), "name", columnConfig.getString("refFetchColumn"), columnConfig.getString("configid"), customerName);
                        refColumnData.put(customerName, list);
                    }
                    if (list.isEmpty()) {
                        vDataValue = null;
                    } else {
                        vDataValue = list.get(0).toString();
                    }
                } else if(openingModuleSet.contains(mode) && columnHeader.equals("Vendor Code") && dataMap.containsKey("VendorName") && !StringUtil.isNullObject(dataMap.get("VendorName")) && !StringUtil.isNullOrEmpty(dataMap.get("VendorName").toString()) ){
                    //for opening transactions if data is empty in file for venddor code then check for name
                    List list;
                    String vendorName = dataMap.get("VendorName").toString();
                    if (refColumnData != null && refColumnData.containsKey(vendorName)) {
                        list = (List) refColumnData.get(vendorName);
                    } else {
                        list = getRefData(requestParams, columnConfig.getString("refModule"), "name", columnConfig.getString("refFetchColumn"), columnConfig.getString("configid"), vendorName);
                        refColumnData.put(vendorName, list);
                    }
                    if (list.isEmpty()) {
                        vDataValue = null;
                    } else {
                        vDataValue = list.get(0).toString();
                    }
                } else {
                    vDataValue = null;
                }
                
                boolean validateWarhouseLoc = true;
                if (dataMap.containsKey("GenerateDeliveryOrder") && dataMap.get("GenerateDeliveryOrder") != null) {
                    validateWarhouseLoc = dataMap.get("GenerateDeliveryOrder").toString().equalsIgnoreCase("true");
                }       
                 
               // Warehouse name validation 
                if (columnHeader.equals("Warehouse") && dataMap.containsKey("isWarehouse") && ((Boolean)dataMap.get("isWarehouse")) != false ) {  //&& dataMap.get("isWarehouse")==true
                     List list;
                    if (vDataValue != null) {
                        if (refColumnData != null && refColumnData.containsKey(vDataValue)) {
                            list = (List) refColumnData.get(vDataValue);
                        } else {
                            list = getRefData(requestParams, columnConfig.getString("refModule"), columnConfig.getString("refDataColumn"), columnConfig.getString("refFetchColumn"), columnConfig.getString("configid"), data);
                            refColumnData.put(vDataValue, list);
                        }
                        if (list.isEmpty()) {
                            throw new DataInvalidateException("Warehouse '" + vDataValue + "' does  not exists.");
                        } else {
                            vDataValue = data;
                        }

                    } else if(validateWarhouseLoc) {
                        throw new DataInvalidateException("Warehouse '" + data + "' does  not exists.");
                    }
                }
                //Location name validation
                if (columnHeader.equals("Location") && dataMap.containsKey("isLocation") && ((Boolean)dataMap.get("isLocation"))!= false ) {  //&& dataMap.get("isWarehouse")==true
                     List list;
                    if (vDataValue != null) {
                        if (refColumnData != null && refColumnData.containsKey(vDataValue)) {
                            list = (List) refColumnData.get(vDataValue);
                        } else {
                            list = getRefData(requestParams, columnConfig.getString("refModule"), columnConfig.getString("refDataColumn"), columnConfig.getString("refFetchColumn"), columnConfig.getString("configid"), data);
                            refColumnData.put(vDataValue, list);
                        }
                        if (list.isEmpty()) {
                            throw new DataInvalidateException("Location '" + vDataValue + "' does  not exists.");
                        } else {
                            vDataValue = data;
                        }

                    } else if(validateWarhouseLoc){
                        throw new DataInvalidateException("Location '" + data + "' does  not exists.");
                    }
                }
                
            } else if (validatetype.equalsIgnoreCase("email")) {
                if (maxLength > 0 && data != null && data.length() > maxLength) {
                    if(pref.equalsIgnoreCase("0")){// most restricted case and data is empty
                        throw new DataInvalidateException("Data length greater than " + maxLength + " for column " + csvHeader + ".");
                    } else {//truncating data for other two cases
                        data = data.substring(0, maxLength);
                        vDataValue=data;
                        if (data.contains(",")) {//if mail is seperated by comma and if we truncate then it is possible that last mail Id become invalid. in this case need to remove that invalid email id
                            String lastMailID = data.substring(data.lastIndexOf(",") + 1, data.length());//taking last mail id
                            if (!StringUtil.isNullOrEmpty(lastMailID)) {
                                Pattern pattern = Pattern.compile(EmailRegEx);
                                if (!pattern.matcher(lastMailID).matches()) {//if not valid
                                    data = data.substring(0, data.lastIndexOf(","));
                                    vDataValue=data;
                                }
                            }
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(data)) {
                    String validEmails="";
                    String invalidEmails="";
                    String emails[]=data.split(",");
                    for (int i = 0; i < emails.length; i++) {
                        if (!StringUtil.isNullOrEmpty(emails[i])) {
                            Pattern pattern = Pattern.compile(EmailRegEx);
                            if (pattern.matcher(emails[i]).matches()) {
                                validEmails += validEmails.equals("") ? emails[i] : "," + emails[i];
                            } else {
                                invalidEmails += invalidEmails.equals("") ? emails[i] : "," + emails[i];
                            }
                        }
                    }
                    if (pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("2")) {//most lanient and most restrict case
                        if (!invalidEmails.equals("")) {//if any email is invalid
                            throw new DataInvalidateException("Invalid email address for " + csvHeader + ".");
                        } 
                    } else if (pref.equalsIgnoreCase("1")) {//skip invalid data
                        vDataValue = validEmails;
                    }
                } else if(isMandatory){// data is empty and mandatory case
                    throw new DataInvalidateException(messageSource.getMessage("acc.export.Emptydatafoundin", null, locale)+" " + csvHeader + ", "+messageSource.getMessage("acc.export.cannotsetemptydatafor", null, locale)+" "+ columnHeader + ".");
                }
            } else if (validatetype.equalsIgnoreCase("boolean")) {
                if (!StringUtil.isNullOrEmpty(data)) {
                    if (data.equalsIgnoreCase("true") || data.equalsIgnoreCase("1") || data.equalsIgnoreCase("T") || data.equalsIgnoreCase("YES")) {
                        vDataValue = true;
                    } else if (data.equalsIgnoreCase("false") || data.equalsIgnoreCase("0") || data.equalsIgnoreCase("F") || data.equalsIgnoreCase("NO")) {
                        vDataValue = false;
                    } else {
                        throw new DataInvalidateException("Incorrect boolean value for " + csvHeader + ".");
                    }
                } else {
                    vDataValue = null;
                }
            } else if (validatetype.equalsIgnoreCase("discount")) {
                if (!StringUtil.isNullOrEmpty(data)) {
                    if (data.equalsIgnoreCase("percentage")) {
                        vDataValue = 1;
                    } else if (data.equalsIgnoreCase("flat")) {
                        vDataValue = 0;
                    } else {
                        throw new DataInvalidateException("Incorrect discount type value for " + csvHeader + ".");
                    }
                } else {
                    vDataValue = null;
                }
            } else if(validatetype.equalsIgnoreCase("sequenceformat")){
                //before checking any sequence number validation, checking maxlength validation
                if (maxLength > 0 && data != null && data.length() > maxLength) {
                    if(pref.equalsIgnoreCase("0")){// most restricted case and data is empty
                        throw new DataInvalidateException("Data length greater than " + maxLength + " for column " + csvHeader + ".");
                    } else {//truncating data for other two cases
                        data=data.substring(0, maxLength);
                        vDataValue=data;
                    }
                }
                /*this validation is for those reocord
                 *which does not preexist in system            
                 * if datamap contain ID field then it is preexist in system. So no need for validating here
                */
                if(!dataMap.containsKey("ID")){ 
                    int moduleid = -1;
//                    String companyid = (String) requestParams.get("companyid");
                    if (mode.equalsIgnoreCase("Customer")) {
                        moduleid = Constants.Acc_Customer_ModuleId;
                    }
                    if (mode.equalsIgnoreCase("Vendor")) {
                        moduleid = Constants.Acc_Vendor_ModuleId;
                    }
                    if ( moduleid != -1) {
                        Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                        sequenceNumberDataMap.put("moduleID", String.valueOf(moduleid));
                        sequenceNumberDataMap.put("entryNumber", data);
                        sequenceNumberDataMap.put("companyID", companyid);
                        List list = checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
                        if (!list.isEmpty()) {
                            boolean isvalidEntryNumber = (Boolean) list.get(0);
                            if (!isvalidEntryNumber) {//i.e. number belongs to sequence format
                                String formatID = (String) list.get(2);
                                int intSeq = (Integer) list.get(3);
                                dataMap.put("Seqnumber",intSeq);
                                dataMap.put("Seqformat",formatID);
                                dataMap.put("AutoGenerated",true);
                            }
                        }
                    }
                }
            } else if (validatetype.equalsIgnoreCase("custom")) {
                /*
                 * This vaildation used for validating data on some customize cases for each column
                 * code should goes for only those column which can not
                 * be validate by above type
                 */  
                
                  // Row , Rack and Bin name validation  
                if ((columnHeader.equals("Row") && dataMap.containsKey("isRow") && ((Boolean) dataMap.get("isRow")) != false) || (columnHeader.equals("Rack") && dataMap.containsKey("isRack") && ((Boolean)dataMap.get("isRack")) != false)|| (columnHeader.equals("Bin") && dataMap.containsKey("isBin") && ((Boolean)dataMap.get("isBin")) != false)) {  //&& dataMap.get("isWarehouse")==true
                    
                    int type = 0;
                    if (columnHeader.equals("Row")) {
                        type = 1;
                    } else if (columnHeader.equals("Rack")) {
                        type = 2;
                    } else if (columnHeader.equals("Bin")) {
                        type = 3;
                    }
                    if(!StringUtil.isNullOrEmpty(data)){
                        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                        filter_names.add("company.companyID");
                        filter_names.add("type");
                        filter_names.add("name");
                        filter_params.add(requestParams.get(Constants.companyKey));
                        filter_params.add(type); 
                        filter_params.add(data); 
                        order_by.add("name");
                        order_type.add("asc");
                        filterRequestParams.put("filter_names", filter_names);
                        filterRequestParams.put("filter_params", filter_params);
                        filterRequestParams.put("order_by", order_by);
                        filterRequestParams.put("order_type", order_type);
                        KwlReturnObject result = importDao.getStoreMasters(filterRequestParams);

                        List list = result.getEntityList();
                        if (list.isEmpty()) {
                             if (columnHeader.equals("Row")) {
                                throw new DataInvalidateException("Row '" + data + "' does  not exists.");
                            } else if (columnHeader.equals("Rack")) {
                                throw new DataInvalidateException("Rack '" + data + "' does  not exists.");
                            } else if (columnHeader.equals("Bin")) {
                                throw new DataInvalidateException("Bin '" + data + "' does  not exists.");
                            }
                        } 

                    } else {
                        if (columnHeader.equals("Row")) {
                            throw new DataInvalidateException("Row '" + data + "' does  not exists.");
                        } else if (columnHeader.equals("Rack")) {
                            throw new DataInvalidateException("Rack '" + data + "' does  not exists.");
                        } else if (columnHeader.equals("Bin")) {
                            throw new DataInvalidateException("Bin '" + data + "' does  not exists.");
                        }

                    }
                }
              
              
                if (mode.equalsIgnoreCase("Customer") || mode.equalsIgnoreCase("Vendor")) {
                    Map<String,Object> map=validateCustomerVendorCommonCustomValidation(requestParams,dataMap,columnHeader,vDataValue,data,mode);
                    vDataValue=map.get("validatedDataValue");
                    boolean isColumnFound=Boolean.parseBoolean(map.get("columnFound").toString());
                    if (!isColumnFound) {//if column found then no need to call below methods
                        if (mode.equalsIgnoreCase("Customer")) {//validation for customer only
                            vDataValue = validateCustomerSpecificCustomValidation(requestParams, dataMap, columnHeader, vDataValue, data, maxLength);
                        } else if (mode.equalsIgnoreCase("Vendor")) {//validation for vendor only
                            vDataValue = validateVendorSpecificCustomValidation(requestParams, dataMap, columnHeader, vDataValue, data, maxLength);
                        }
                    }
                        }
                else if (mode.equalsIgnoreCase(Constants.Acc_BankReconciliation_modulename)) {
//                    String companyid = (String) requestParams.get("companyid");
                    /*
                     * Here we are validating whether this customer/vendor/account is valid or not.
                     * Either it is present in our system or not.
                     */
                    vDataValue = validatePayee(companyid, data);
                }
                else if (mode.equalsIgnoreCase(Constants.Acc_Opening_Receipt_modulename) || mode.equalsIgnoreCase(Constants.Acc_Opening_Payment_modulename) || mode.equalsIgnoreCase("Cash Sales") || mode.equalsIgnoreCase("Convert Sales Invoice in to Cash Sales")) {
                    //below method used for validating opening payment specific validation.
                    vDataValue = validateOpeningPaymentSpecificCustomValidation(requestParams, dataMap,columnConfig,csvHeader, data,dateFormat,vDataValue);
                } else if(mode.equalsIgnoreCase(Constants.DBS_Bank_Module_Name)){
                    if (!StringUtil.isNullOrEmpty(data)) {//below all columns are mandatory so null check is before of all 
                        if (columnHeader.equals("Receiving Bank Number/Code")) {
                            validateDBSBankNumber(data);
                        } else if (columnHeader.equals("Receiving Branch Number/Code")) {
                            validateDBSBranchNumber(data);
                        } else if (columnHeader.equals("Receiving Account Number")) {
                            validateDBSAccountNumber(data);
                        } else if (columnHeader.equals("Vendor Code")) {
//                            String companyid = (String) requestParams.get("companyid");
                            validateDBSVendorCode(companyid, data);
                        }
                    } else {
                        vDataValue = null;
                    }
                } else if(mode.equalsIgnoreCase("Accounts")){
                    //below method used for validating IBG Columns
                    vDataValue = validateAccountSpecificCustomValidation(requestParams, dataMap, columnHeader,csvHeader,vDataValue, data, maxLength);
                }
            } else if (validatetype.equalsIgnoreCase("string") && maxLength > 0 && data != null && data.length() > maxLength) {
                if (pref.equalsIgnoreCase("0")) {// most restricted case and data is empty
                    throw new DataInvalidateException("Data length greater than " + maxLength + " for column " + csvHeader + ".");
                } else {//truncating data for other two cases
                    data = data.substring(0, maxLength);
                    vDataValue=data;
                }                
            }
             /**
             * Validate Data for Input/ Output line level terms
             * Mode: Product and Assembly Product
             */
            if ((mode.equals("Product") || mode.equals("Assembly Product")) && dataMap.containsKey("InputTax") && columnHeader.equals("Input Tax")) {
                if(!StringUtil.isNullOrEmpty(data)){
                    Map<String,Object> requestData = new HashMap<String,Object>();
                    requestData.put("companyid", requestParams.get("companyid").toString());
                    requestData.put("colData", dataMap.get("InputTax"));
                    requestData.put("isOutputTax", false);//ERP-29870
                    List<String> termsList = importDao.getLineLevelTermPresentByName(requestData); // This method check given Input terms present in system or not if not present then Mark this reocrd as Invalid
                    if(termsList.size()>0){
                        String invalidData = Arrays.toString(termsList.toArray());
                        throw new DataInvalidateException(columnHeader + " column Data is not valid. Input tax should be present in system: " + invalidData );
                    }
                }                        
            }if ((mode.equals("Product") || mode.equals("Assembly Product")) && dataMap.containsKey("OutputTax") && columnHeader.equals("Output Tax")) {
                if(!StringUtil.isNullOrEmpty(data)){
                    Map<String,Object> requestData = new HashMap<String,Object>();
                    requestData.put("companyid", requestParams.get("companyid").toString());
                    requestData.put("colData", dataMap.get("OutputTax"));
                    requestData.put("isOutputTax", true); //ERP-29870
                    List<String> termsList = importDao.getLineLevelTermPresentByName(requestData); // This method check given Output terms present in system or not if not present then Mark this reocrd as Invalid
                    if(termsList.size()>0){
                        String invalidData = Arrays.toString(termsList.toArray());
                        throw new DataInvalidateException(columnHeader + " column Data is not valid. Output tax should be present in system: " + invalidData );
                    } 
                }
            }
            
            if(mode.equals("Product") || mode.equals("Assembly Product")){
                if (columnHeader.equals("Display UOM") && columnPrefJObj!=null && columnPrefJObj.has(Constants.isDisplayUOM) && columnPrefJObj.get(Constants.isDisplayUOM) != null && (Boolean) columnPrefJObj.get(Constants.isDisplayUOM) != false) {
                    if (!StringUtil.isNullOrEmpty(data) && dataMap.containsKey("UomSchemaType") && dataMap.get("UomSchemaType")!=null) {
                        boolean isValid = importDao.isValidDisplayUOM(dataMap.get("UomSchemaType").toString(), data, requestParams.get("companyid").toString());
                        if (!isValid) {
                            throw new DataInvalidateException("Wrong value entered into Display UOM");
                        }
                    }
                }
            }
            
             // import validation to check ,product creation date with book beginning date.
            //if greater then fail the import.
            if (dataMap.containsKey("AsOfDate") && columnHeader.equals("As of Date") && (mode.equals("Product opening stock") 
                    || (mode.equals("Product") && !isWarehouseCompulsory && !isLocationCompulsory && dataMap.containsKey("Quantity") && dataMap.get("Quantity")!=null && !StringUtil.isNullOrEmptyWithTrim(dataMap.get("Quantity").toString()) && Integer.parseInt(dataMap.get("Quantity").toString())>0 ))) {
                String AsOfDate_temp = (String) dataMap.get("AsOfDate");
                if (!StringUtil.isNullOrEmpty(AsOfDate_temp)) {
                    String ldf = dateFormat != null ? dateFormat : (data == null ? df : (data.length() > 10 ? df_full : df));
                    SimpleDateFormat sdf = new SimpleDateFormat(ldf);
                    Date AsOfDate = (Date) sdf.parse(AsOfDate_temp.toString());
                    Date bookBeginningDate = (Date) requestParams.get("bookbeginning");
                    if (AsOfDate.after(bookBeginningDate)) {
                        throw new DataInvalidateException(messageSource.getMessage("acc.importproduct.beforeOrEqualToBookBeginningDate", null , locale));
                    }
                }
            }
            
            Set accountModuleColumnHeaderSet = new HashSet();//this set contains those column of Account module on which we need custom (specific cases) validation
            accountModuleColumnHeaderSet.add("Master Type");
            accountModuleColumnHeaderSet.add("Type");
            accountModuleColumnHeaderSet.add("IBG Bank");
            accountModuleColumnHeaderSet.add("IBG Bank Details");            
            accountModuleColumnHeaderSet.add("Parent Account");            
            boolean isExciseApplicable = (requestParams.containsKey("isExciseApplicable") && Boolean.parseBoolean(requestParams.get("isExciseApplicable").toString())); //ERP-26934
            boolean isEnableVatCst = (requestParams.containsKey("isEnableVatCst") && Boolean.parseBoolean(requestParams.get("isEnableVatCst").toString())); 
            // Currently isIndiaCompany is sent through Customer, Vendor , Account Master
            boolean isIndiaCompany = (requestParams.containsKey("countryid") && Integer.parseInt(requestParams.get("countryid").toString()) == Constants.indian_country_id);
            boolean stateCheckMaharashtra = (requestParams.containsKey("stateid") && Integer.parseInt(requestParams.get("stateid").toString()) == IndiaComplianceConstants.MAHARASHTRA_STATE_ID);
            
            if (mode.equalsIgnoreCase("Customer") || mode.equalsIgnoreCase("Vendor")) {
                    if((column.equalsIgnoreCase("PanStatus") && vDataValue == null) && !StringUtil.isNullOrEmpty((String) dataMap.get("PANnumber"))){
                        isMandatoryConditionFound=false;
                    }
                /**
                * Conditional Validation Customer/ Vendor GSTIN/ UIN Number
                * ERP-35237
                * New Update  - ERP-37978
                */
                if (isIndiaCompany && (column.equalsIgnoreCase("GSTCustomerType") || column.equalsIgnoreCase("GSTVendorType") || column.equalsIgnoreCase("GSTIN") || column.equalsIgnoreCase("GSTRegistrationType")|| column.equalsIgnoreCase("gstapplicabledate"))) {
                    JSONObject GSTDetails = new JSONObject();
                    // Data key GSTCustomerType , GSTIN , GSTRegistrationType
                    GSTDetails.put("GSTIN", dataMap.containsKey("GSTIN") && dataMap.get("GSTIN") != null ? (String) dataMap.get("GSTIN") : "");
                    GSTDetails.put("custVenProdID", dataMap.containsKey("ID") && dataMap.get("ID") != null ? (String) dataMap.get("ID") : "");
                    GSTDetails.put("gstapplicabledate", dataMap.containsKey("gstapplicabledate") && dataMap.get("gstapplicabledate") != null ? (String) dataMap.get("gstapplicabledate") : "");
                    if (mode.equalsIgnoreCase("Customer")) {
                        Map gstrefColumnData = (rowRefDataMap.containsKey("GSTCustomerType") && rowRefDataMap.get("GSTCustomerType") != null) ? (Map) rowRefDataMap.get("GSTCustomerType") : null;
                        if (gstrefColumnData != null && !gstrefColumnData.isEmpty()) {
                            String GSTCustomerType = "";
                            String GSTCustomerTypeID = dataMap.containsKey("GSTCustomerType") && dataMap.get("GSTCustomerType") != null ? (String) dataMap.get("GSTCustomerType") : "";
                            Iterator it = gstrefColumnData.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry map = (Map.Entry) it.next();
                                List typeName = (List) map.getValue();
                                if (typeName.contains(GSTCustomerTypeID)) {
                                    GSTCustomerType = map.getKey().toString();
                                }
                            }
                            if (StringUtil.isNullOrEmpty(GSTCustomerType)) {
                                GSTDetails.put("GSTCustomerVendorType", GSTCustomerTypeID);
                            } else {
                                GSTDetails.put("GSTCustomerVendorType", GSTCustomerType);
                            }
                        } else {
                            GSTDetails.put("GSTCustomerVendorType", dataMap.containsKey("GSTCustomerType") && dataMap.get("GSTCustomerType") != null ? (String) dataMap.get("GSTCustomerType") : "");
                        }
                    } else if (mode.equalsIgnoreCase("Vendor")) {
                        Map gstrefColumnData = (rowRefDataMap.containsKey("GSTVendorType") && rowRefDataMap.get("GSTVendorType") != null) ? (Map) rowRefDataMap.get("GSTVendorType") : null;
                        if (gstrefColumnData != null && !gstrefColumnData.isEmpty()) {
                            String GSTVendorType = "";
                            String GSTVendorTypeID = dataMap.containsKey("GSTVendorType") && dataMap.get("GSTVendorType") != null ? (String) dataMap.get("GSTVendorType") : "";
                            Iterator it = gstrefColumnData.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry map = (Map.Entry) it.next();
                                List typeName = (List) map.getValue();
                                if (typeName.contains(GSTVendorTypeID)) {
                                    GSTVendorType = map.getKey().toString();
                                }
                            }
                            if (StringUtil.isNullOrEmpty(GSTVendorType)) {
                                GSTDetails.put("GSTCustomerVendorType", GSTVendorTypeID);
                            } else {
                                GSTDetails.put("GSTCustomerVendorType", GSTVendorType);
                            }
                        } else {
                            GSTDetails.put("GSTCustomerVendorType", dataMap.containsKey("GSTVendorType") && dataMap.get("GSTVendorType") != null ? (String) dataMap.get("GSTVendorType") : "");
                        }
                    }
                    Map gstrefColumnData = (rowRefDataMap.containsKey("GSTRegistrationType") && rowRefDataMap.get("GSTRegistrationType") != null) ? (Map) rowRefDataMap.get("GSTRegistrationType") : null;
                    if (gstrefColumnData != null && !gstrefColumnData.isEmpty()) {
                        String GSTRegistrationType = "";
                        String GSTRegTypeId = dataMap.containsKey("GSTRegistrationType") && dataMap.get("GSTRegistrationType") != null ? (String) dataMap.get("GSTRegistrationType") : "";
                        Iterator it = gstrefColumnData.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry map = (Map.Entry) it.next();
                            List typeName = (List) map.getValue();
                            if (typeName.contains(GSTRegTypeId)) {
                                GSTRegistrationType = map.getKey().toString();
                            }
                        }
                        if (StringUtil.isNullOrEmpty(GSTRegistrationType)) {
                            GSTDetails.put("GSTRegistrationType", GSTRegTypeId);
                        } else {
                            GSTDetails.put("GSTRegistrationType", GSTRegistrationType);
                        }
                    } else {
                        GSTDetails.put("GSTRegistrationType", dataMap.containsKey("GSTRegistrationType") && dataMap.get("GSTRegistrationType") != null ? (String) dataMap.get("GSTRegistrationType") : "");
                    }
                    GSTDetails.put("csvHeader", csvHeader);
                    GSTDetails.put("columnHeader", columnHeader);
                    GSTDetails.put("column", column);
                    GSTDetails = isCustomVendorGSTMasterDetailsValidForImport(requestParams, GSTDetails);
                    String validationMsg = GSTDetails.optString("validationMSG", "");
                    if (!StringUtil.isNullOrEmpty(validationMsg)) {
                        throw new DataInvalidateException(validationMsg);
                    }
                }
            }
            if ((vDataValue == null || (vDataValue != null && StringUtil.isNullOrEmpty(vDataValue.toString()))) && (isMandatory || isMandatoryConditionFound)) {
                if(!column.equalsIgnoreCase("PanStatus") || (column.equalsIgnoreCase("PanStatus") && StringUtil.isNullOrEmpty((String) dataMap.get("PANnumber")) && StringUtil.isNullOrEmpty(vDataValue.toString()))){
                    throw new DataInvalidateException(messageSource.getMessage("acc.export.Emptydatafoundin", null, locale)+" " + csvHeader + ", "+messageSource.getMessage("acc.export.cannotsetemptydatafor", null, locale)+" " + columnHeader + ".");
                }
            } else {
                if (customflag) {
                    JSONObject jobj = new JSONObject();
                    if (columnConfig.getString("xtype").equals("4") || columnConfig.getString("xtype").equals("7")) {//Drop down & Multi Select Drop down
                        try {
                            if (vDataValue != null) {
                                String[] fieldComboDataArr = vDataValue.toString().split(";");
                                String fieldComboDataStr = "";

                                for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                    if (!StringUtil.isNullOrEmpty(fieldComboDataArr[dataArrIndex])) {
                                        List list = getCustomComboID(fieldComboDataArr[dataArrIndex], columnConfig.getString("id"), "id");

                                        if (list != null && !list.isEmpty()) {
                                            fieldComboDataStr += list.get(0).toString() + ",";
                                        }
                                    }
                                }

                                if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                    vDataValue = fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1);
                                } else if (!vDataValue.equals("")) {
                                    throw new DataInvalidateException(csvHeader + " entry not found in master list for " + columnHeader + " dropdown.");
                                }
                            } else if (vDataValue == null && isMandatory) {
                                throw new DataInvalidateException("Incorrect reference mapping(" + columnHeader + ") for " + csvHeader + ".");
                            }
                        } catch (ServiceException ex) {
                            throw new DataInvalidateException("Incorrect reference mapping(" + columnHeader + ") for " + csvHeader + ".");
                        } catch (DataInvalidateException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            throw new DataInvalidateException(csvHeader + " entry not found in master list for " + columnHeader + " dropdown.");
                        }
                    } else if (columnConfig.getString("xtype").equals("8")) {//Reference Drop down & Multi Select Drop down
                        try {
                            if (vDataValue != null) {
                                if (!StringUtil.isNullOrEmpty(vDataValue.toString())) {
                                    if (columnConfig.has("refModule") && columnConfig.has("refDataColumn") && columnConfig.has("refFetchColumn") && columnConfig.has("configid")) {
                                        List list;
                                        if(refColumnData != null && refColumnData.containsKey(vDataValue)){
                                            list = (List)refColumnData.get(vDataValue);
                                        }else{
                                            list = getRefData(requestParams, columnConfig.getString("refModule"), columnConfig.getString("refDataColumn"), columnConfig.getString("refFetchColumn"), columnConfig.getString("configid"), vDataValue);
                                            refColumnData.put(vDataValue, list);
                                        }
                                        vDataValue = list.get(0).toString();
                                    } else {
                                        throw new DataInvalidateException("Incorrect reference mapping(" + columnHeader + ") for " + csvHeader + ".");
                                    }
                                }
                            } else if (vDataValue == null && isMandatory) {
                                throw new DataInvalidateException("Incorrect reference mapping(" + columnHeader + ") for " + csvHeader + ".");
                            }
                        } catch (ServiceException ex) {
                            throw new DataInvalidateException("Incorrect reference mapping(" + columnHeader + ") for " + csvHeader + ".");
                        } catch (DataInvalidateException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            throw new DataInvalidateException(csvHeader + " entry not found in master list for " + columnHeader + " dropdown.");
                        }
                    } else {
                        if (maxLength > 0 && data != null && data.length() > maxLength) {
                            throw new DataInvalidateException("Data length greater than " + maxLength + " for column " + csvHeader + ".");
                        }
                    }

                    if (mode.equalsIgnoreCase("Accounts") || mode.equalsIgnoreCase("Journal Entry") ||  mode.equalsIgnoreCase("Customer") ||  mode.equalsIgnoreCase("Vendor") || mode.equalsIgnoreCase("Product")) {
                        jobj.put("fieldid", columnConfig.getString("id"));
                        jobj.put("refcolumn_name", "Col" + columnConfig.get("refcolnum"));
                        jobj.put("fieldname", "Custom_" + columnConfig.get("columnName"));
                        jobj.put("xtype", columnConfig.getString("xtype"));

                        if (columnConfig.getString("xtype").equalsIgnoreCase("3") && vDataValue != null) {
                            Date dateFromFile=(new SimpleDateFormat(df_customfield).parse(vDataValue.toString()));
                            DateFormat df=new SimpleDateFormat(Constants.MMMMdyyyy);
                            jobj.put("Col" + columnConfig.get("colnum"),df.format(dateFromFile));
                            jobj.put("fieldDataVal",df.format(dateFromFile));
                        } else {
                            jobj.put("Col" + columnConfig.get("colnum"), vDataValue);
                            jobj.put("fieldDataVal", vDataValue);
                        }

                        jobj.put("Custom_" + columnConfig.get("columnName"), "Col" + columnConfig.get("colnum"));
                    } else {
                        jobj.put(columnConfig.getString("pojoName"), vDataValue == null ? "" : vDataValue);
                        jobj.put("filedid", columnConfig.getString("pojoHeader"));
                        jobj.put("xtype", columnConfig.getString("xtype"));
                    }

                    customfield.put(jobj);
                    if (dataMap.containsKey(column)) {
                        dataMap.remove(column);
                    }	
                } else if(mode.equalsIgnoreCase("UOM Schema") && columnHeader.equals("Schema Type")){
                    if (!(data.equalsIgnoreCase("Purchase") || data.equalsIgnoreCase("Sales") || data.equalsIgnoreCase("Transfer") || data.equalsIgnoreCase("Order"))) {
                        throw new DataInvalidateException("Value is not valid for column " + csvHeader + ",  It should be like 'Purchase','Sales','Order' or 'Transfer'.");
                    }
                } else if (mode.equalsIgnoreCase("Accounts") && accountModuleColumnHeaderSet.contains(columnHeader)) {                      
                    if(columnHeader.equals("Master Type")) {
                        int masterVal = getMasterValue(data);
                        if (masterVal == -1) {
                            if(isIndiaCompany){
                                throw new DataInvalidateException("Value is not valid for column " + csvHeader + ".  It should be '"+Constants.MASTER_TYPE_GENERAL_LEDGER+"','"+Constants.MASTER_TYPE_BANK+"','"+Constants.MASTER_TYPE_CASH+"' or '"+Constants.MASTER_TYPE_DUTIES_AND_TAXES+"'.");
                            } else{
                                throw new DataInvalidateException("Value is not valid for column " + csvHeader + ".  It should be '"+Constants.MASTER_TYPE_GENERAL_LEDGER+"','"+Constants.MASTER_TYPE_BANK+"','"+Constants.MASTER_TYPE_CASH+"' or '"+Constants.MASTER_TYPE_GST+"'.");
                            }
                        }
                        dataMap.put(column, masterVal);
                    } else if (columnHeader.equals("Type")) {
                        int typeVal = getAccountTypeValue(data);
                        if (typeVal == -1) {
                            throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". It should be 'Balance sheet' or 'Profit & Loss'.");
                        }
                        dataMap.put(column, typeVal);
                    } else if (columnHeader.equals("IBG Bank")) {
                        boolean isIBGDetails = false;
                        if (dataMap.containsKey("IBGBank") && dataMap.get("IBGBank") != null) {
                            String isIBGDetailsValue = "";
                            isIBGDetailsValue = dataMap.get("IBGBank").toString();
                            if (isIBGDetailsValue.equalsIgnoreCase("true") || isIBGDetailsValue.equalsIgnoreCase("1") || isIBGDetailsValue.equalsIgnoreCase("T") || isIBGDetailsValue.equalsIgnoreCase("YES")) {
                                isIBGDetails = true;
                            }
                        }
                        int ibgBankVal = 0;
                        if (isIBGDetails) {//if true is given in IBG Details column then only need to check for IBG Bank name
                            if (StringUtil.isNullOrEmpty(data)) {
                                throw new DataInvalidateException(messageSource.getMessage("acc.export.Emptydatafoundin", null, locale)+" " + csvHeader + ", "+messageSource.getMessage("acc.export.cannotsetemptydatafor", null, locale)+" " + columnHeader + ".");
                            } else {
                                ibgBankVal = getIBGBankValue(data);
                                if (ibgBankVal == -1) {
                                    throw new DataInvalidateException("Value is not valid for column " + csvHeader + ".  It should be 'Development Bank Of Singapore'.");
                                }
                            }
                        }
                        dataMap.put(column, ibgBankVal);
                    } else if(columnHeader.equals("IBG Bank Details")){
                        if (vDataValue != null && vDataValue.equals(true)) {//if value is true for column "IBG Bank Details" then need to check wheather master type of account is bank or not. If bank then allow othrwise given validation message
                            String masterTypevalue =  dataMap.get("Mastertypevalue").toString();
                            if (!(masterTypevalue.equals("3") ||masterTypevalue.equals("Bank"))) {// when Account master type not equlas to Bank
                                boolean isActivateIBG = (requestParams.containsKey("isActivateIBG") && requestParams.get("isActivateIBG") != null) ? Boolean.parseBoolean(requestParams.get("isActivateIBG").toString()) : false;
                                if(isActivateIBG){
                                    throw new DataInvalidateException("Value is not valid for column " + csvHeader + ", You can only provide "+csvHeader+" as "+data+" when Master Type is Bank and Activate IBG is checked in system control.");
                                } else {
                                    throw new DataInvalidateException("Value is not valid for column " + csvHeader + ", You can only provide "+csvHeader+" as "+data+" when Master Type is Bank.");
                                }
                            }
                        }
                        dataMap.put(column, vDataValue);
                    } else if(columnHeader.equals("Parent Account")){
                        if (vDataValue != null){
                            String parentAccountGroupId = "";
                            KwlReturnObject returnObject = importDao.getAccountDetailsFromAccountId(vDataValue.toString());
                            List listGroupId = returnObject.getEntityList();
                            Object[] dateValuesobj = (Object[]) listGroupId.get(0);
                            if (dateValuesobj != null) {
                                if (dateValuesobj[0] != null) {
                                    parentAccountGroupId=(String) dateValuesobj[0];
                                }
                            }
                            if(!parentAccountGroupId.equals(dataMap.get("Group"))){
                                throw  new DataInvalidateException("Wrong Parent Account is mapped.");
                            }
                        }
                        dataMap.put(column, vDataValue);
                    } 

                } else if (mode.equalsIgnoreCase("Group") && validatetype.equalsIgnoreCase("string")) {
//                    if (validatetype.equalsIgnoreCase("string")) {
                    if (StringUtil.isNullOrEmpty(data)) {
                        throw new DataInvalidateException("Data Should not empty for column " + csvHeader + ".");
                    } else if (columnHeader.equals("Nature")) {
                        int natureVal = getNatureValue("Nature", data);
                        if (natureVal == -1) {
                            throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". It should be 'Asset' or 'Liability' or 'Income' or 'Expenses'.");
                        } 
                        
                        // Below code for checking combination of Group nature and COGS flag. As per current working COGS can only be true for Expense 
                        boolean isCOGSGroup = false;
                        if (dataMap.containsKey("CostOfGoodsSoldGroup") && dataMap.get("CostOfGoodsSoldGroup") != null && !StringUtil.isNullOrEmpty(dataMap.get("CostOfGoodsSoldGroup").toString())) {
                            String cogs = dataMap.get("CostOfGoodsSoldGroup").toString();
                            if (cogs.equalsIgnoreCase("true") || cogs.equalsIgnoreCase("1") || cogs.equalsIgnoreCase("T") || cogs.equalsIgnoreCase("YES")) {
                                isCOGSGroup = true;
                            }
                        }
                        if(isCOGSGroup && natureVal!=2){// When isCOGS Flag is true but nature is not expense the we give alert to user
                            throw new DataInvalidateException("Value is not valid for column " + csvHeader + ".You can not privide nature other than Expenses when value for column 'Is a Cost of Goods Sold Group' is ture.");
                        }
//                        }
                    }
                } else if (mode.equalsIgnoreCase("Product") && validatetype.equalsIgnoreCase("string")) {
                    if (!StringUtil.isNullOrEmpty(data)) {
                        if (columnHeader.equals("Valuation Method")) {
                            int valmethodValue = getValuationMethodValue("Valuation Method", data);
                            if (valmethodValue == -1) {
                                throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". It should be 'FIFO' or 'LIFO' or 'Moving Average'.");
                            }
                        }
                        
                        if ((columnHeader.equals("Vat Valuation Type") || columnHeader.equals("Valuation Type")) && isMandatoryConditionFound) {  //ERP-28454
                                if (IndiaComplianceConstants.valuationType.containsKey(data.trim())) {
                                    IndiaComplianceConstants.valuationType.get(data.trim());
                                    if(columnHeader.equals("Vat Valuation Type") && data.equals("Specific basis")){
                                        throw new DataInvalidateException(data +" whish is not found in "+columnHeader +" (1.Ad Valorem Method,2.Quantity,3.MRP (Maximum Retail Price))");    
                                    }
                                }else{
                                    throw new DataInvalidateException(data +" whish is not found in "+columnHeader +" (1.Ad Valorem Method,2.Quantity,3.Specific basis,4.MRP (Maximum Retail Price))");
                                }                           
                        }
                        
                        if(columnHeader.equals("Product Name")){
                            if(data.contains(",")){
                                throw new DataInvalidateException("Please enter valid " + csvHeader + ".");
                            }
                        }
                    } else {
                        vDataValue = null;
                    }
                } else if (mode.equalsIgnoreCase("Product") && validatetype.equalsIgnoreCase("integer")) {
                    if (!StringUtil.isNullOrEmpty(data)) {
                        if (columnHeader.equals("Lead Time (in days)")) {
                            int leadTime=(int) vDataValue; //data is taking from vDataValue because it either zero or some numberic value 
                            if (leadTime > 365) {
                                throw new DataInvalidateException("Product Lead Time should not be greater than 365.");
                            } else if (leadTime < 0) {
                                throw new DataInvalidateException("Product Lead Time should not be less than 0.");
                            }
                        }
                    } else if (StringUtil.isNullOrEmpty(data) && columnHeader.equals("Lead Time (in days)")) {
                        if (pref.equalsIgnoreCase("1")) {
                            vDataValue = null;
                        } else {
                            if (dataMap.get("Producttype").toString().equalsIgnoreCase("Service") && !dataMap.get("Producttype").toString().equalsIgnoreCase("4efb0286-5627-102d-8de6-001cc0794cfa")) { // for service type product
                                vDataValue = null;
                            } else {
                                throw new DataInvalidateException("Product Lead Time is not available.");
                            }
                        }
                    } else {                        
                        vDataValue = null;
                    }
                } else if (mode.equalsIgnoreCase("Product") && validatetype.equalsIgnoreCase("ref")) {
                    if (StringUtil.isNullOrEmpty(data)) {
                        if (columnHeader.equals("Stock UOM") && (!dataMap.get("Producttype").toString().equalsIgnoreCase("Service") && !dataMap.get("Producttype").toString().equalsIgnoreCase("4efb0286-5627-102d-8de6-001cc0794cfa"))) {
                            throw new DataInvalidateException("Data should not be empty for column " + csvHeader + ".");
                        } else if (columnHeader.equals("Default Location")) {
                            if ((!dataMap.containsKey("Location") || "".equals(dataMap.get("Location")) || dataMap.get("Location")==null) && (dataMap.containsKey("Activate Location") && ("T".equals(dataMap.get("Activate Location")) || (dataMap.get("Activate Location")!=null && "true".equals(dataMap.get("Activate Location").toString()))))) {
                                throw new DataInvalidateException("Location is activated but Default Location Value is not Provided.");
                            }
                        } else if (columnHeader.equals("Default Warehouse")) {
                            if ((!dataMap.containsKey("Warehouse") || "".equals(dataMap.get("Warehouse")) || dataMap.get("Warehouse")==null) && (dataMap.containsKey("Activate Warehouse") && ("T".equals(dataMap.get("Activate Warehouse")) || (dataMap.get("Activate Warehouse")!=null && "true".equals(dataMap.get("Activate Warehouse").toString()))))) {
                                throw new DataInvalidateException("Warehouse is activated but Default Warehouse Value is not Provided.");
                            }
                        } 
                    } else {
                        if (columnHeader.equals("UOM Schema")) {
                            if (dataMap.containsKey("UnitOfMeasure")) {
                                boolean isMatched = importDao.isStockUomMatchWithStockUomOfUomSchema(data, dataMap.get("UnitOfMeasure").toString(), (String) requestParams.get("companyid"));
                                if (!isMatched) {
                                    throw new DataInvalidateException("Stock UOM of Product and UOM Schema's Stock UOM should be same.");
                                }
                            }
                        } else if (columnHeader.equals("Ordering UOM") || columnHeader.equals("Transfer UOM") || columnHeader.equals("Purchase UOM") || columnHeader.equals("Sales UOM")) {
                            Set packagingUOMs = new HashSet();
                            if (dataMap.containsKey("UnitOfMeasure") && dataMap.get("UnitOfMeasure") != null && !StringUtil.isNullOrEmpty(dataMap.get("UnitOfMeasure").toString())) {
                                packagingUOMs.add(dataMap.get("UnitOfMeasure").toString());
                            }
                            if (dataMap.containsKey("innerUoM") && dataMap.get("innerUoM") != null && !StringUtil.isNullOrEmpty(dataMap.get("innerUoM").toString())) {
                                packagingUOMs.add(dataMap.get("innerUoM").toString());
                            }
                            if (dataMap.containsKey("casingUoM") && dataMap.get("casingUoM") != null && !StringUtil.isNullOrEmpty(dataMap.get("casingUoM").toString())) {
                                packagingUOMs.add(dataMap.get("casingUoM").toString());
                            }

                            if (!(packagingUOMs.contains(vDataValue) || packagingUOMs.contains(data))) {
                                throw new DataInvalidateException(columnHeader + " are not matching with either Stock UOM or Inner UOM or Casing UOM.");
                            }
                        } else if (columnHeader.equals("Product Type")) {
                            if (data.equalsIgnoreCase("Inventory Assembly")) {
                                throw new DataInvalidateException("Assembly products need to be imported separately through 'Import Assembly Product'.");
                            }
                        }else  if (columnHeader.equals("VAT Commodity Name")) {
                            if ((dataMap.containsKey("Vatcommoditycode") && dataMap.get("Vatcommoditycode")!=null && !StringUtil.isNullOrEmpty(dataMap.get("Vatcommoditycode").toString()))) {
                                String temp = dataMap.get("Vatcommoditycode").toString();
                                String vatcommodityname = temp;
                                Map tempData = new HashMap<String, String>();
                                tempData.put("vatcommodityname", vatcommodityname);
                                tempData.put("company", requestParams.get("companyid").toString());
                                KwlReturnObject vatCommresult = importDao.getVATCommodityCodeByName(tempData);
                                boolean isVatonmrp = false;
                                if (dataMap.get("Vatonmrp") != null) {
                                    isVatonmrp = Boolean.valueOf(dataMap.get("Vatonmrp").toString().equals("T") || dataMap.get("Vatonmrp").toString().equals("true"));
                                }
                                if (vatCommresult.getRecordTotalCount() == 0 && isVatonmrp && isEnableVatCst && pref.equalsIgnoreCase("2")) {
                                    throw new DataInvalidateException(vatcommodityname + " which is not present in system.");
                                }
                            }
                        } else if(columnHeader.equalsIgnoreCase("Default Warehouse")){
                            if(!StringUtil.isNullOrEmpty(data) && vDataValue!=null && !StringUtil.isNullOrEmpty(vDataValue.toString())){
                                KwlReturnObject result = importDao.getStoreDetails(vDataValue.toString());
                                if(result!=null && result.getRecordTotalCount()>0){
                                    int storeType = (int) result.getEntityList().get(0);
                                    // Check if store type is not 0=Warehouse or 2=Headquarter as such Default Warehouse are not allowed to map to product.
                                    if(storeType != 0 && storeType != 2){
                                        throw new DataInvalidateException("Store type for " + data + " store must be either Warehouse or Headquarter.");
                                    }
                                }
                            }
                        }
                    }
                } else if  ((mode.equalsIgnoreCase("Vendor")|| mode.equalsIgnoreCase("Customer")) && (columnHeader.equals("Vendor PAN Status")||columnHeader.equals("Customer PAN Status")) && validatetype.equalsIgnoreCase("string")) { //ERP-26934
                    /**
                      * For INDIA Compliance - Vendor and Customer Import added PAN status  If TDS is Applicable then Mandatory field  else none  Mandatory.
                      * ERP-24274 and ERP-24302
                    */          
                    if(isTDSapplicable){
                    if (columnHeader.equals("Vendor PAN Status") || columnHeader.equals("Customer PAN Status")) {
                        if (StringUtil.isNullOrEmpty(data) ) {
                            if (StringUtil.isNullOrEmpty((String) dataMap.get("PANnumber"))) {
                                throw new DataInvalidateException("Data Should not empty for column " + csvHeader + ".");
                            }
                        } else {
                                String PANStatus = getPANStatus("Vendor/Customer PAN Status", data);
                                if (PANStatus.equalsIgnoreCase("-1")) {
                                throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". It should be '"+IndiaComplianceConstants.PAN_NOT_AVAILABLE+"' or '"+IndiaComplianceConstants.PAN_APPLIED_FOR+"'.");
                            } else if(!StringUtil.isNullOrEmpty((String) dataMap.get("PANnumber"))){
                                dataMap.put(column, "");
                            } else{
                                    dataMap.put(column, PANStatus);
                                }
                            }
                        }
                    } else {
                        dataMap.put(column, "");
                    }
                    
                }else if  (isIndiaCompany && (mode.equalsIgnoreCase("Vendor")|| mode.equalsIgnoreCase("Customer")) && (columnHeader.equals("Vendor PAN")||columnHeader.equals("Customer PAN")) && validatetype.equalsIgnoreCase("string")) { 
                    /**
                      * For INDIA Compliance
                    */                    
                    if(isTDSapplicable){
                    if (columnHeader.equals("Vendor PAN") || columnHeader.equals("Customer PAN")) {
                        if (!StringUtil.isNullOrEmpty(data) ) {
                            boolean isValid = isPANvalid(data);
                            if(!isValid){
                                throw new DataInvalidateException("Value is not valid for column " + csvHeader+". Valid PAN is like 'AAAAA1234A'");
                            }
                        }else if (StringUtil.isNullOrEmpty((String) dataMap.get("PanStatus"))) {
                            throw new DataInvalidateException(csvHeader + " cannot be empty if PAN status is empty. In case " + csvHeader + " is not available, Please provide PAN status.");
                        }else{
                            dataMap.put(column,"");
                        }
                    }
                    } else {
                            dataMap.put(column,"");
                    }
                    
                } else if (isIndiaCompany && (mode.equalsIgnoreCase("Vendor") || mode.equalsIgnoreCase("Customer")) && (columnHeader.equals("Vendor VAT TIN") || columnHeader.equals("Vendor CST TIN") || columnHeader.equals("Customer VAT TIN") || columnHeader.equals("Customer CST TIN"))) {
                    /*
                     * For INDIA Compliance
                     */
                    if (!StringUtil.isNullOrEmpty(data)) {
                        boolean isValid = isTINvalid(data);
                        if (!isValid && pref.equalsIgnoreCase("0") && dataMap.containsKey("Dealertype") && dataMap.get("Dealertype")!=null && dataMap.get("Dealertype").toString().equals("1")) { // most restricted case and data is empty // 1 - Registered Dealer
                            throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". Valid TIN is like '1234567890A' or '12345678901'");
                        } else if(!isValid) {//truncating data for other two cases
                            dataMap.put(column, "");
                        }
                    } else if (pref.equalsIgnoreCase("0") && dataMap.containsKey("Dealertype") && dataMap.get("Dealertype") != null && dataMap.get("Dealertype").toString().equals("1")) {
                        throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". Valid TIN is like '1234567890A' or '12345678901'");
                    }

                }else if (isIndiaCompany && (mode.equalsIgnoreCase("Vendor") || mode.equalsIgnoreCase("Customer")) && columnHeader.equals("C-Form Applicable") && validatetype.equalsIgnoreCase("string")) {
                    /*
                     * For INDIA Compliance - Interstate Party , C-Form Applicable
                     */
                    boolean isCformApplicable=false;
                    if (dataMap.containsKey("Interstateparty") && dataMap.get("Interstateparty")!=null && !StringUtil.isNullOrEmpty(data)) {
                        String Interstateparty = dataMap.get("Interstateparty").toString();
                        if (Interstateparty.equalsIgnoreCase("true") || Interstateparty.equalsIgnoreCase("1") || Interstateparty.equalsIgnoreCase("T") || Interstateparty.equalsIgnoreCase("YES")) {
                            if (data.equalsIgnoreCase("true") || data.equalsIgnoreCase("1") || data.equalsIgnoreCase("T") || data.equalsIgnoreCase("YES")) {
                                isCformApplicable=true;
                            }else if(data.equalsIgnoreCase("false") || data.equalsIgnoreCase("0") || data.equalsIgnoreCase("F") || data.equalsIgnoreCase("NO")) {
                                isCformApplicable=false;
                            }else{
                                throw new DataInvalidateException("Incorrect boolean value for " + csvHeader + ".");
                            }
                        }
                    } 
                    dataMap.put(column,isCformApplicable);
                }else if  ((mode.equalsIgnoreCase("Vendor") || mode.equalsIgnoreCase("Customer")) && (columnHeader.equals("Type of Manufacturer")) && validatetype.equalsIgnoreCase("string")) {
                    /**
                      * For INDIA Compliance - Vendor  Import added Manufacturer Status mandatory field
                      * 
                    */ 
                    boolean createAsVendor = false;                    
                    String columnValue = dataMap.get("Mapcustomervendor")!=null?dataMap.get("Mapcustomervendor").toString():"";                    
                    if (columnValue.equalsIgnoreCase("true") || columnValue.equalsIgnoreCase("1") || columnValue.equalsIgnoreCase("T") || columnValue.equalsIgnoreCase("YES")) {
                        createAsVendor = true; 
                    }
                    
                    if((createAsVendor || mode.equalsIgnoreCase("Vendor")) && isExciseApplicable ){
                    if (columnHeader.equals("Type of Manufacturer")) {
                        if (!StringUtil.isNullOrEmpty(data)) {//As this field is non-mandatory, so check only if data isnot Empty.
                            String ManufacturerStatus = getManufacturerStatus("Type of Manufacturer", data);
                            if (ManufacturerStatus.equalsIgnoreCase("-1")) {
                                throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". It should be 'Regular' or 'Small Scale Industries(SSI)'.");
                            } else {
                                dataMap.put(column, ManufacturerStatus);
                            } 
                        }
                    }
                    }
                }else if  ((mode.equalsIgnoreCase("Vendor")  || mode.equalsIgnoreCase("Customer")) && (columnHeader.equals("Deductee Type")) && validatetype.equalsIgnoreCase("string")) { //ERP-26934
                    /**
                      * For INDIA Compliance - Vendor  Import added Deductee If TDS is Applicable then Mandatory field  else none  Mandatory.
                      * 
                    */ 
                    if (isTDSapplicable) {
                        if (StringUtil.isNullOrEmpty(data)) {
                            throw new DataInvalidateException("Data Should not empty for column " + csvHeader + ".");
                        } else {
//                            String companyid = (String) requestParams.get("companyid");
                            String DeducteeType = importDao.getMasterTypeID(companyid, data, "34");
                            if (DeducteeType.equalsIgnoreCase("-1")) {
                                throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". It should be 'Individual' ,'HUF', 'Association of Persons', 'Body of Individuals', 'Artificial Juridical Person', 'Firm', 'Co-operative Society', 'Local authority', 'Company', 'Unknown'.");
                            } else {
                                dataMap.put(column, DeducteeType);
                            }
                        }
                    } else {
                        dataMap.put(column, "");
                    }
                }else if  ((mode.equalsIgnoreCase("Vendor") || mode.equalsIgnoreCase("Customer")) && (columnHeader.equals("Vendor Residential Status") || columnHeader.equals("Customer Residential Status")) && validatetype.equalsIgnoreCase("string")) {
                    /**
                      * For INDIA Compliance - Vendor  Import added Vendor Residential Status field
                      * ERP-33711
                    */ 
                    int ResidentStatus = 0;
                    if (isTDSapplicable) { // Company Level + Customer/ Vendor Level TDS chck, For more details please check start of this Customer
                        if (StringUtil.isNullOrEmpty(data)) {
                            throw new DataInvalidateException("Data Should not empty for column " + csvHeader + ".");
                        } else {
                            if (data.trim().equalsIgnoreCase("Non-Resident")) {
                                ResidentStatus = 1;
                            }else if(data.trim().equalsIgnoreCase("Resident")){
                                ResidentStatus = 0;
                            }else{
                                throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". It should be 'Non-Resident' ,'Resident'.");
                            }
                        }
                    }
                    dataMap.put(column, ResidentStatus);
                }else if  ((mode.equalsIgnoreCase("Vendor") || mode.equalsIgnoreCase("Customer")) && (columnHeader.equals("Vat Dealer Type")) && validatetype.equalsIgnoreCase("string")) {
                    /**
                      * For INDIA Compliance - Vendor  Import added Vat Dealer Type field
                      * 
                    */ 
                    if(isEnableVatCst){
                        String dealertype = "-1";
                        String validationMsg="It should be 'Unregistered Dealer' or 'Registered Dealer' or data will be empty.";
                        if (data.equalsIgnoreCase("Registered Dealer")) {
                            dealertype = "1";
                        } else if (data.equalsIgnoreCase("Unregistered Dealer")) {
                            dealertype = "2";
                        } else if (stateCheckMaharashtra && data.equalsIgnoreCase("Composition Dealer u/s 42(3) ,(3A) & (4)")){
                            dealertype = "3";
                        }else if (stateCheckMaharashtra && data.equalsIgnoreCase("Composition Dealer u/s 42(1) ,(2)")){
                            dealertype = "4";
                        }
                        if(stateCheckMaharashtra){
                            validationMsg="It should be 'Unregistered Dealer' or 'Registered Dealer' or 'Composition Dealer u/s 42(3) ,(3A) & (4)' or 'Composition Dealer u/s 42(1) ,(2)' or data will be empty.";
                        }
                        if (dealertype.equalsIgnoreCase("-1")) {
                            throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". "+validationMsg);
                        } else {
                            dataMap.put(column, dealertype);  
                        }
                    }else{
                        dataMap.put(column, null); // Default value of 'Vat Dealer Type' is null
                    }
                } else if  ((mode.equalsIgnoreCase("Vendor") || mode.equalsIgnoreCase("Customer")) && (columnHeader.equals("Type of Sales") && !StringUtil.isNullOrEmpty(data)) && validatetype.equalsIgnoreCase("string")) {
                    /**
                      * For INDIA Compliance - Vendor  Import added Type of Sales field
                      * 
                    */ 
//                            String companyid= (String) requestParams.get("companyid");
                            String typOFSales = importDao.getMasterTypeID(companyid, data,"47");
                            if (typOFSales.equalsIgnoreCase("-1")) {
                                throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". It should be 'First Stage Dealer', 'Importer', 'Manufacturer', 'Purchase from Importer', 'Second stage Dealer'.");
                            } else {
                                dataMap.put(column, typOFSales);
                            } 
                    
                } else if (mode.equalsIgnoreCase("Vendor") && columnHeader.equals("Default nature of Payment") && validatetype.equalsIgnoreCase("string")) {//ERP-28976
                    if (isTDSapplicable) {
                        if (StringUtil.isNullOrEmpty(data)) {
                            throw new DataInvalidateException("Data Should not empty for column " + csvHeader + ".");
                        } else {
//                            String companyid = (String) requestParams.get("companyid");
                            String[] dataAry = data.split("-");
                            String code = dataAry.length > 0 ? dataAry[0].trim() : "";
                            data = dataAry.length > 1 ? dataAry[1].trim() : "";
                            String nop = importDao.getMasterTypeID(companyid, data, "33", code);
                            if (nop.equalsIgnoreCase("-1")) {
                                throw new DataInvalidateException("Value is not valid for column " + csvHeader + ". ");
                            } else {
                                dataMap.put(column, nop);
                            }
                        }
                    } else {
                        dataMap.put(column, "");
                    }
                }else if (mode.equalsIgnoreCase("Vendor Invoice") && validatetype.equalsIgnoreCase("ref") && columnHeader.equals("Consignment Number")) {
                    if (!StringUtil.isNullOrEmpty(data) && !StringUtil.isNullOrEmpty(dataMap.get("Product").toString())) {
                        KwlReturnObject productTypeObj = importDao.getProductTypeOfProduct(dataMap.get("Product").toString(), (String) requestParams.get("companyid"));
                        if (productTypeObj.getEntityList() != null && !productTypeObj.getEntityList().isEmpty()) {
                            String productType = productTypeObj.getEntityList().get(0).toString();

                            if (productType.equalsIgnoreCase(Constants.ASSEMBLY) || productType.equalsIgnoreCase(Constants.INVENTORY_PART)) {
                                throw new DataInvalidateException("Inventory and Assembly Type Product are not allowed in Consignment link case.");
                            }
                        }
                    }
                } else if (mode.equalsIgnoreCase("Vendor Invoice") && validatetype.equalsIgnoreCase("text")) {
                    if (columnHeader.equals("Goods Receipt No")) {
                        if (StringUtil.isNullOrEmpty(data) && !StringUtil.isNullOrEmpty(dataMap.get("GenerateGoodsReceipt").toString())) {
                            if (dataMap.get("GenerateGoodsReceipt").toString().equalsIgnoreCase("true")) {
                                throw new DataInvalidateException("Goods Receipt No is not available.");
                            }
                        } else if (!StringUtil.isNullOrEmpty(data) && !StringUtil.isNullOrEmpty(dataMap.get("GenerateGoodsReceipt").toString()) && dataMap.get("GenerateGoodsReceipt").toString().equalsIgnoreCase("true")) {
//                            String companyid = (String) requestParams.get("companyid");
                            KwlReturnObject groResult = importDao.getGoodsReceiptOrderCount(data, companyid);
                            if (groResult.getRecordTotalCount() > 0) {
                                throw new DataInvalidateException("Goods Receipt No " + data + " is already exist. ");
                            }
                        }
                    }
                } else if ((mode.equalsIgnoreCase("Customer Invoices") || mode.equalsIgnoreCase("Cash Sales")) && validatetype.equalsIgnoreCase("text")) {
                    if (columnHeader.equals("Delivery Order Number")) {
                        if (StringUtil.isNullOrEmpty(data) && dataMap.containsKey("GenerateDeliveryOrder") && !StringUtil.isNullOrEmpty(dataMap.get("GenerateDeliveryOrder").toString())) {
                            if (dataMap.get("GenerateDeliveryOrder").toString().equalsIgnoreCase("true")) {
                                throw new DataInvalidateException("Delivery Order Number is not available.");
                            }
                        } else if (!StringUtil.isNullOrEmpty(data) && dataMap.containsKey("GenerateDeliveryOrder") && !StringUtil.isNullOrEmpty(dataMap.get("GenerateDeliveryOrder").toString()) && dataMap.get("GenerateDeliveryOrder").toString().equalsIgnoreCase("true")) {
//                            String companyid = (String) requestParams.get("companyid");
                            KwlReturnObject groResult = importDao.getDeliveryOrderCount(data, companyid);
                            if (groResult.getRecordTotalCount() > 0) {
                                throw new DataInvalidateException("Delivery Order Number " + data + " is already exist. ");
                            }
                        }
                    }
                } else if (openingModuleSet.contains(mode)) { // for opening transactions
                    validateOpeningSpecificValidation(requestParams,dataMap,columnHeader,vDataValue,data,mode);
                    dataMap.put(column, vDataValue);
                } else if ((mode.equalsIgnoreCase("Customer Invoices") || mode.equalsIgnoreCase("Vendor Invoice") || mode.equalsIgnoreCase("Sales Order")) && validatetype.equalsIgnoreCase("boolean")) {
                    if (columnHeader.equals("Include Product Tax")) {
                        if (!StringUtil.isNullOrEmpty(dataMap.get("GstIncluded").toString()) && dataMap.get("GstIncluded").toString().equalsIgnoreCase("true") && (StringUtil.isNullOrEmpty(data) || data.equalsIgnoreCase("No") || data.equalsIgnoreCase("false"))) {
                            throw new DataInvalidateException("If value Including GST is \"TRUE\" then value of Include Product Tax should be \"Yes\".");
                        }
                    } else if (columnHeader.equals("Include Total Tax")) {
                        if (!StringUtil.isNullOrEmpty(dataMap.get("Includeprotax").toString()) && (dataMap.get("Includeprotax").toString().equalsIgnoreCase("true") || dataMap.get("Includeprotax").toString().equalsIgnoreCase("Yes")) && !StringUtil.isNullOrEmpty(data) && (data.equalsIgnoreCase("Yes") || data.equalsIgnoreCase("true"))) {
                            throw new DataInvalidateException("If value of Include Product Tax is \"Yes\" then value of Include Total Tax should be \"No\".");
                        }
                    }
                } else if ((mode.equalsIgnoreCase("Customer Invoices") || mode.equalsIgnoreCase("Vendor Invoice") || mode.equalsIgnoreCase("Sales Order") || mode.equalsIgnoreCase("Cash Purchase") || mode.equalsIgnoreCase("Cash Sales")) && validatetype.equalsIgnoreCase("ref")) {
                    if (columnHeader.equals("Tax Name")) {
                        if (!StringUtil.isNullOrEmpty(dataMap.get("GstIncluded").toString()) && dataMap.get("GstIncluded").toString().equalsIgnoreCase("false") && !StringUtil.isNullOrEmpty(dataMap.get("Includeprotax").toString()) && (dataMap.get("Includeprotax").toString().equalsIgnoreCase("false") || dataMap.get("Includeprotax").toString().equalsIgnoreCase("No")) && !StringUtil.isNullOrEmpty(dataMap.get("Taxincluded").toString()) && (dataMap.get("Taxincluded").toString().equalsIgnoreCase("Yes") || dataMap.get("Taxincluded").toString().equalsIgnoreCase("true")) && StringUtil.isNullOrEmpty(data)) {
                            throw new DataInvalidateException("Tax Name is not available.");
                        }
                    } else if (columnHeader.equals("Tax Code")) {
                        if (!StringUtil.isNullOrEmpty(dataMap.get("GstIncluded").toString()) && dataMap.get("GstIncluded").toString().equalsIgnoreCase("false") && !StringUtil.isNullOrEmpty(dataMap.get("Includeprotax").toString()) && (dataMap.get("Includeprotax").toString().equalsIgnoreCase("false") || dataMap.get("Includeprotax").toString().equalsIgnoreCase("No")) && !StringUtil.isNullOrEmpty(dataMap.get("Taxincluded").toString()) && (dataMap.get("Taxincluded").toString().equalsIgnoreCase("Yes") || dataMap.get("Taxincluded").toString().equalsIgnoreCase("true"))) {
                            if(StringUtil.isNullOrEmpty(data)){
                                throw new DataInvalidateException("Tax Code is not available.");
                            } else {
                                boolean isSales = true;
                                if (mode.equalsIgnoreCase("Vendor Invoice") || mode.equalsIgnoreCase("Cash Purchase")) {
                                    isSales = false;
                                }
                                KwlReturnObject result = importDao.getTaxByCode(companyid, data, isSales);
                                if (result.getEntityList().isEmpty() || result.getEntityList().get(0) == null) {
                                    throw new DataInvalidateException("Tax Code is not "+ (isSales ? "Sales" : "Purchase") +" Type TAX for code "+ data);
                                }
                                if (!importDao.isTaxActivated(companyid, data)) {
                                    throw new DataInvalidateException("Tax Code is deactivated " + data);
                                }
                            }
                        }
                    } else if (columnHeader.equals("Product Tax")) {
                        if (!StringUtil.isNullOrEmpty(dataMap.get("Includeprotax").toString()) && (dataMap.get("Includeprotax").toString().equalsIgnoreCase("Yes") || dataMap.get("Includeprotax").toString().equalsIgnoreCase("true")) ) {
                            if(StringUtil.isNullOrEmpty(data)) {
                                throw new DataInvalidateException("Product Tax is not available.");
                            } else {
                                boolean isSales = true;
                                if (mode.equalsIgnoreCase("Vendor Invoice") || mode.equalsIgnoreCase("Cash Purchase")) {
                                    isSales = false;
                                }
                                KwlReturnObject result = importDao.getTaxByCode(companyid, data, isSales);
                                if (result.getEntityList().isEmpty() || result.getEntityList().get(0) == null) {
                                    throw new DataInvalidateException("Tax Code is not "+ (isSales ? "Sales" : "Purchase") +" Type TAX for code "+ data);
                                }
                                if (!importDao.isTaxActivated(companyid, data)) {
                                    throw new DataInvalidateException("Tax Code is deactivated " + data);
                                }
                            }
                        }
                    }
                } else if ((mode.equalsIgnoreCase("Customer Invoices") || mode.equalsIgnoreCase("Vendor Invoice") || mode.equalsIgnoreCase("Sales Order")) && validatetype.equalsIgnoreCase("double")) {
                    if (columnHeader.equals("Unit Price Including GST")) {
                        if (!StringUtil.isNullOrEmpty(dataMap.get("GstIncluded").toString()) && dataMap.get("GstIncluded").toString().equalsIgnoreCase("true") && StringUtil.isNullOrEmpty(data)) {
                            throw new DataInvalidateException("Unit Price Including GST is not available.");
                        }
                    }
                } else if (mode.equalsIgnoreCase("Product Category") && validatetype.equalsIgnoreCase("ref")) {
                    if (columnHeader.equals("Product Category")) {
                        String ProductCategory= (String) dataMap.get("ProductCategory");
                        String ProductID= (String) dataMap.get("ProductID");
                        if (!StringUtil.isNullOrEmpty(data) && !StringUtil.isNullOrEmpty(ProductCategory) && !StringUtil.isNullOrEmpty(ProductID)) {
                            KwlReturnObject categoryResult = importDao.getProductCategoryMappingCount(ProductID, ProductCategory);
                            int nocount = categoryResult.getRecordTotalCount();
                            if (nocount > 0) {
                                throw new DataInvalidateException("Product Category '" + ProductCategory + "' for Product ID '" + ProductID + "' is already exists.");
                            }
                        } 
                    }
                } else if (mode.equalsIgnoreCase("Customer Category") && validatetype.equalsIgnoreCase("ref")) {
                    if (columnHeader.equals("Customer Category")) {
                        String customerCategory = (String) dataMap.get("CustomerCategory");
                        String customerID = (String) dataMap.get("CustomerID");
                        if (!StringUtil.isNullOrEmpty(data) && !StringUtil.isNullOrEmpty(customerCategory) && !StringUtil.isNullOrEmpty(customerID)) {
                            KwlReturnObject categoryResult = importDao.getCustomerCategoryMappingCount(customerID, customerCategory);
                            int nocount = categoryResult.getRecordTotalCount();
                            if (nocount > 0) {
                                throw new DataInvalidateException("Customer Category '" + customerCategory + "' for Customer Code '" + customerID + "' is already exists.");
                            }
                        }
                    }
                    dataMap.put(column, vDataValue);
                } else if (mode.equalsIgnoreCase("Vendor Category") && validatetype.equalsIgnoreCase("ref")) {
                    if (columnHeader.equals("Vendor Category")) {
                        String vendorCategory = (String) dataMap.get("VendorCategory");
                        String vendorID = (String) dataMap.get("VendorID");
                        if (!StringUtil.isNullOrEmpty(data) && !StringUtil.isNullOrEmpty(vendorCategory) && !StringUtil.isNullOrEmpty(vendorID)) {
                            KwlReturnObject categoryResult = importDao.getVendorCategoryMappingCount(vendorID, vendorCategory);
                            int nocount = categoryResult.getRecordTotalCount();
                            if (nocount > 0) {
                                throw new DataInvalidateException("Vendor Category '" + vendorCategory + "' for Vendor Code '" + vendorID + "' is already exists.");
                            }
                        }
                    }
                    dataMap.put(column, vDataValue);
                } else if (columnHeader.equals("Cheque Date") && dataMap.containsKey("ChequeDate")) {
                    int paymentDetailType = -1;
                    if (dataMap.containsKey("PaymentMethod") && dataMap.get("PaymentMethod") != null) {
                        String paymentMethodID = dataMap.get("PaymentMethod").toString();
                        if (!StringUtil.isNullOrEmpty(paymentMethodID)) {
                            KwlReturnObject returnObject = importDao.getPaymentMethodDetail(dataMap.get("PaymentMethod").toString(), companyid);
                                    if (returnObject != null && !returnObject.getEntityList().isEmpty() && returnObject.getEntityList().get(0) != null) {
                                        List listPaymentMethod = returnObject.getEntityList();
                                        Object[] dateValuesobj = (Object[]) listPaymentMethod.get(0);
                                        if (dateValuesobj != null) {
                                            if (dateValuesobj[0] != null) {
                                                paymentDetailType = (int) dateValuesobj[0];
                                    }
                                }
                            }
                        }
                    }
                    if (StringUtil.isNullOrEmpty(data) && paymentDetailType == Constants.bank_detail_type) {
                        throw new DataInvalidateException("Cheque Date is not available.");
                    }
                } else if ((mode.equalsIgnoreCase("Customer Invoices") && columnHeader.equals("Invoice Date")) || (mode.equalsIgnoreCase("Cash Sales") && columnHeader.equals("Cash Sales Date")) || (mode.equalsIgnoreCase("Vendor Invoice") && columnHeader.equals("Vendor Invoice Date")) || ((mode.equalsIgnoreCase("Payment") || mode.equalsIgnoreCase("Receipt")) && columnHeader.equals("Date")) || (mode.equalsIgnoreCase("Credit Note") && columnHeader.equals("Credit Note Date "))) {
                    DateFormat df = new SimpleDateFormat(dateFormat);
                    df.setLenient(false);

                    Date finanDate = null, bookdate = null, billDate = null;
                    if(dataMap.containsKey(column)){
                        billDate = df.parse(dataMap.get(column).toString());
                    }

                    boolean isOpeningBalanceOrder = (requestParams.containsKey("isOpeningBalanceOrder") && requestParams.get("isOpeningBalanceOrder") != null) ? Boolean.parseBoolean(requestParams.get("isOpeningBalanceOrder").toString()) : false;
                    Date transactiondate = null;
                    if (billDate != null) {
                        transactiondate = billDate;
                    }
                    Map<String, Object> filterParams = new HashMap<String, Object>();
                    filterParams.put("id", (String) requestParams.get("companyid"));
                    KwlReturnObject kresult = importDao.getFinancialYearDates(filterParams);
                    List cplist = kresult.getEntityList();
                    Object[] dateValuesobj = (Object[]) cplist.get(0);
                    if (dateValuesobj != null) {
                        if (dateValuesobj[0] != null) {
                            finanDate = (Date) dateValuesobj[0];
                        }
                        if (dateValuesobj[1] != null) {
                            bookdate = (Date) dateValuesobj[1];
                        }

                    }

                    finanDate = removeTimefromDate(finanDate);
                    transactiondate = removeTimefromDate(transactiondate);
                    bookdate = removeTimefromDate(bookdate);
                    if (finanDate.after(transactiondate) && !isOpeningBalanceOrder) {//if date is less than first financial year date
                        throw new DataInvalidateException("Transaction before First Financial Year Date are not allowed.You can update the First Financial Year Date and then proceed.");
                    } else if (bookdate.after(transactiondate) && !isOpeningBalanceOrder) {//if date lies between first financial year date & book beginning date
                        throw new DataInvalidateException("Transaction before First Book Beginning Date are not allowed. You can update the First Book Beginning Date and then proceed.");
                    }

                } else if ((mode.equalsIgnoreCase("Journal Entry") && (columnHeader.equals("Account Code")))|| (mode.equalsIgnoreCase("Fixed Asset Group") && (columnHeader.equals("Controlling Account") || columnHeader.equals("Depreciation GL(Profit Loss)")|| columnHeader.equals("Provision For Depreciation GL(Balance Sheet)") || columnHeader.equals("Profit/Loss on Sale of Assets") || columnHeader.equals("Write-Off Account")))) {
                    if (vDataValue != null) {
                        KwlReturnObject returnObject = importDao.getAccountDetailsFromAccountId((String)vDataValue);
                        List listactive = returnObject.getEntityList();
                        if (listactive.size() > 0) {
                            Object[] dateValuesobj = (Object[]) listactive.get(0);
                            char activeAccount = 0;
                            String usedIn = "";
                            char isWantToPostJE = 0;
                            if (dateValuesobj != null) {
                                if (dateValuesobj[1] != null) {
                                    activeAccount = (char) dateValuesobj[1];
                                }
                                if (dateValuesobj[2] != null) {
                                    usedIn = (String) dateValuesobj[2];
                                }
                                if (dateValuesobj[3] != null) {
                                    isWantToPostJE = (char) dateValuesobj[3];
                                }
                                
                            }
                            if (activeAccount == 'F') {
                                if (mode.equalsIgnoreCase("Fixed Asset Group")) {
                                    throw new DataInvalidateException("Account '" + data + "' are already deactivated. Record cannot be imported.");
                                } else {
                                    throw new DataInvalidateException("Account Code '" + dataMap.get("Acccode") + "' are already deactivated. Record cannot be imported.");
                                }

                            }
                            if (!StringUtil.isNullOrEmpty(usedIn) && isWantToPostJE == 'F' && !(mode.equalsIgnoreCase("Fixed Asset Group"))) {
                                throw new DataInvalidateException("The account code (" + data + ") is a control account. " + messageSource.getMessage("acc.JournalEntry.import.ControlAccount", null, locale));
                            }
                        }
                    }
                } else {
                    if (validatetype.equalsIgnoreCase("string") && maxLength > 0 && data != null && data.length() > maxLength) {
                        if (pref.equalsIgnoreCase("0")) {// most restricted case and data is empty
                            throw new DataInvalidateException("Data length greater than " + maxLength + " for column " + csvHeader + ".");
                        } else {//truncating data for other two cases
                            data = data.substring(0, maxLength);
                            vDataValue=data;
                        }
                    }
                    dataMap.put(column, vDataValue);
                }
            }
        } else { // If no validation type then check allow null property[SK]

            if ((vDataValue == null || (vDataValue != null && StringUtil.isNullOrEmpty(vDataValue.toString()))) && isMandatory) {
                throw new DataInvalidateException(messageSource.getMessage("acc.export.Emptydatafoundin", null, locale)+" " + csvHeader + ","+messageSource.getMessage("acc.export.cannotsetemptydatafor", null, locale)+" " + columnHeader + ".");
            }
        }
    }
    
    public void validateCustomFieldColumnData(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, JSONObject columnConfig, String column, JSONArray customfield, HashMap<String, Object> columnHeaderMap, String dateFormat, Map rowRefDataMap) throws JSONException, DataInvalidateException, ParseException, ServiceException {
        boolean isMandatory = columnConfig.optBoolean("isMandatory", false);
        String csvHeader = (String) columnHeaderMap.get(column);
        csvHeader = (csvHeader == null ? csvHeader : csvHeader.replaceAll("\\.", " "));//remove '.' from csv Header
        String columnHeader = columnConfig.getString("columnName");
        String moduleId="";        
        String data = dataMap.get(column) == null ? null : dataMap.get(column).toString().replaceAll("\n", "");
        String pref = (String) requestParams.get("masterPreference"); //0:Skip Record, 1:Skip Column, 2:Add new
        Object vDataValue = data;
        Locale locale = null;       
        boolean isManadatory = columnConfig.optBoolean("isMandatory", false);
        if(columnConfig.has("moduleid") && columnConfig.get("moduleid")!=null){
            moduleId=String.valueOf(columnConfig.get("moduleid"));
        }        
        if (requestParams.containsKey("locale")) {
            locale = (Locale) requestParams.get("locale");
        }

        if ((vDataValue == null || (vDataValue != null && StringUtil.isNullOrEmpty(vDataValue.toString()))) && isMandatory) {
            throw new DataInvalidateException(messageSource.getMessage("acc.export.Emptydatafoundin", null, locale) + " " + csvHeader + ", " + messageSource.getMessage("acc.export.cannotsetemptydatafor", null, locale) + " " + columnHeader + ".");
        } else {
            if (columnConfig.getString("xtype").equals("2")) {
                try {
                    if (!StringUtil.isNullOrEmpty((String)vDataValue)) {
                        Double.parseDouble(vDataValue.toString());
                    } 
                } catch (NumberFormatException ex) {
                    if (pref.equals("1") && !isMandatory) {//add empty case when field is not mandatory
                        vDataValue = 0.0;
                    } else {
                        throw new DataInvalidateException("Incorrect numeric value for " + csvHeader + " Please ensure that value type of " + csvHeader + " matches with the " + columnHeader);
                    }
                }
            } else if (columnConfig.getString("xtype").equals("3")) {
                vDataValue = validateDataForDate(requestParams, columnConfig, csvHeader, vDataValue, data, dateFormat);
            } else if(moduleId.equals(String.valueOf(Constants.GSTModule))){  
                /*
                 * method will be invoked in import case of Terms
                 */
              vDataValue=validateComboData(requestParams, columnConfig, csvHeader, vDataValue, data, dateFormat, dataMap);                   
              /**
               * Validate GST rule.
               * If GST Rule Added In Applied date 1 July 2017 with term name : CGST and SGST (Already Rule Added or Import And Used In Transaction)
               * After this using same applied date we are adding rules with term name : CGST , SGST and CESS then. (CESS tax Added Extra)
               * 1) Validation messages should be shown before import (validation window).
               * Messages : CGST , SGST rule can't update as it is used in transaction but CESS rule will be added in system. (New CESS Rule entry should not be restricted)
               */
              if (csvHeader.equalsIgnoreCase(Constants.ENTITY)) {
                    JSONObject returnObject = validateGSTRulesUsedInTransaction(requestParams, columnConfig, csvHeader, vDataValue, data, dateFormat, dataMap, columnHeaderMap);
                    String failureMsg = returnObject.optString("failureMsg", "");
                    if (!StringUtil.isNullOrEmpty(failureMsg)) {
                        throw new DataInvalidateException(failureMsg);
                    }
               }
            }else if (columnConfig.getString("xtype").equals("4")) {
                JSONObject paramJobj = new JSONObject();
                paramJobj.put("fieldlabel", csvHeader);
                paramJobj.put("fieldvalue", vDataValue);
                paramJobj.put(Constants.companyKey, requestParams.get("companyid"));
                if (isManadatory && StringUtil.isNullOrEmpty((String) vDataValue)) {
                    throw new DataInvalidateException("Empty data found in " + csvHeader + ", cannot set empty data for " + csvHeader);
                } else {
                    KwlReturnObject result = importDao.isCustomDataPresent(paramJobj);
                    BigInteger valueCount = !result.getEntityList().isEmpty() ? (BigInteger) result.getEntityList().get(0) : BigInteger.valueOf(0);
                    String dataValue=(String) vDataValue;
                    if (valueCount.intValue() <= 0 && !StringUtil.isNullOrEmpty(dataValue) && !dataValue.equalsIgnoreCase("none")) {
                        /**
                         * If GST Rule import module and value contains other then no validation for this value.
                         * For INDIA GST Rule Other means add IGST Rule for all state except Entity dimension's state value.
                         */
                        if(!(moduleId.equals(String.valueOf(Constants.GSTModule)) && dataValue.toLowerCase().contains("Other".toLowerCase()))){
		           throw new DataInvalidateException(vDataValue + " not found in drop down of " + csvHeader);
	                }//
                    }
                }
            }
        }
    }
    public boolean isMandatoryConditionForColumn(Map<String, Object> requestParams,HashMap<String, Object> dataMap,String column){
        boolean isMandatoryCondition = false;
        String mode = (String) requestParams.get("modName");
        if(mode.equalsIgnoreCase(Constants.Acc_Product_modulename) || mode.equalsIgnoreCase(Constants.ASSEMBLY_PRODUCT_MODULE_NAME)){
            // Matching with Pojomethod name is better way than matching with defaultHeader. Because default header can be modified but there is very rate chance of pojomethod name. So here I have matched with pojomethodname 
            boolean isServiceTypeProduct = dataMap.get("Producttype")!=null ? (dataMap.get("Producttype").toString().equalsIgnoreCase("Service") || dataMap.get("Producttype").toString().equalsIgnoreCase("4efb0286-5627-102d-8de6-001cc0794cfa")):false;
            boolean isNonInventoryPartTypeProduct = dataMap.get("Producttype")!=null ? (dataMap.get("Producttype").toString().equalsIgnoreCase("Non-Inventory Part") || dataMap.get("Producttype").toString().equalsIgnoreCase("f071cf84-515c-102d-8de6-001cc0794cfa")):false;
            if (column.equals("UnitOfMeasure")) {// Stock UOM Header. This column is mandatory when product is non service type and that is for below if case
                if(!isServiceTypeProduct){
                    isMandatoryCondition= true;
                }
            } else if(column.equals("CostOfGoodsSoldAccount") || column.equals("InventoryAccount") || column.equals("StockAdjustmentAccount")){// File header Cost of Goods Sold Account || Inventory Account || Stock Adjustment Account
                if(!isServiceTypeProduct && !isNonInventoryPartTypeProduct){// these are non mandatory for Service/Non-Inventory type of product. 
                    boolean isMRPFlag = false;
                    boolean isPerpetualInventoryValuation = false;

                    if (requestParams.containsKey("isActivateMRPModule") && requestParams.get("isActivateMRPModule") != null) {
                        isMRPFlag = StringUtil.isNullOrEmpty(requestParams.get("isActivateMRPModule").toString()) ? false : Boolean.parseBoolean(requestParams.get("isActivateMRPModule").toString());
                    }
                    if (requestParams.containsKey("inventoryValuationType") && requestParams.get("inventoryValuationType") != null) {
                        try{
                          isPerpetualInventoryValuation = (Integer.parseInt(requestParams.get("inventoryValuationType").toString())==1); // 1 for perpetual inventory valuation  
                        } catch(NumberFormatException px){
                            isPerpetualInventoryValuation=false;
                        }
                    }
                    if(isMRPFlag || isPerpetualInventoryValuation){// If mrp module is active or inventory valuation is perpetual then these columns are mandatory while importing
                        isMandatoryCondition = true;
                    }
                }
                    }
                boolean isEnableVatCst = (requestParams.containsKey("isEnableVatCst") && Boolean.parseBoolean(requestParams.get("isEnableVatCst").toString()));
                boolean countryCheck = (requestParams.containsKey("countryid") && Integer.parseInt(requestParams.get("countryid").toString()) == Constants.indian_country_id);
                    if (countryCheck && isEnableVatCst) {
                        if (column.equals("Vatonmrp")) {
                            isMandatoryCondition = true;
                        }
                        
                        boolean isVatonmrp=false;
                        if(dataMap.get("Vatonmrp")!=null){
                         isVatonmrp= Boolean.valueOf(dataMap.get("Vatonmrp").toString().equals("T") ||dataMap.get("Vatonmrp").toString().equals("true")) ;
                        }
                        
                        if (column.equals("VatMethodType") && isVatonmrp) {
                            isMandatoryCondition = true;
                        }
                        if ((column.equals("TariffName") || column.equals("Reportinguom") || column.equals("Excisemethodmain") || column.equals("NatureofStockItem")) && !isServiceTypeProduct){                             
                           isMandatoryCondition = true;
                         }
                }
               if (column.equals("HSNCode") && countryCheck) {
                isMandatoryCondition = true;
                }  
        }else if (mode.equalsIgnoreCase("Customer") || mode.equalsIgnoreCase("Vendor")) { //ERP-26934
            
            boolean countryCheck = (requestParams.containsKey("countryid") && Integer.parseInt(requestParams.get("countryid").toString()) == Constants.indian_country_id);
            boolean isTDSapplicable = (requestParams.containsKey("isTDSapplicable") && Boolean.parseBoolean(requestParams.get("isTDSapplicable").toString()));
            boolean isExciseApplicable = (requestParams.containsKey("isExciseApplicable") && Boolean.parseBoolean(requestParams.get("isExciseApplicable").toString()));

            /*
             * Country check is to check Indian Company and TDS Applicable then DeducteeType and Pan Status is Mandatory to vendor and customer Add in import file. 
             */            
            if (countryCheck && isTDSapplicable) {
                if ((column.equals("IsTDSapplicableonvendor") && mode.equalsIgnoreCase("Vendor"))) {
                    isMandatoryCondition = true;
                }
                String isTDSapplicableonvendor_Value = dataMap.get("IsTDSapplicableonvendor") != null ? dataMap.get("IsTDSapplicableonvendor").toString() : null;
                boolean TDSapplicableOnParticularVendor = true;
                if (!StringUtil.isNullOrEmpty(isTDSapplicableonvendor_Value)) {
                    if (isTDSapplicableonvendor_Value.equalsIgnoreCase("true") || isTDSapplicableonvendor_Value.equalsIgnoreCase("1") || isTDSapplicableonvendor_Value.equalsIgnoreCase("T") || isTDSapplicableonvendor_Value.equalsIgnoreCase("YES")) {
                        TDSapplicableOnParticularVendor = true;
                    } else if (isTDSapplicableonvendor_Value.equalsIgnoreCase("false") || isTDSapplicableonvendor_Value.equalsIgnoreCase("0") || isTDSapplicableonvendor_Value.equalsIgnoreCase("F") || isTDSapplicableonvendor_Value.equalsIgnoreCase("NO")) {
                        TDSapplicableOnParticularVendor = false;
                    }
                }
                
                if ((column.equals("IsTDSapplicableoncust") && mode.equalsIgnoreCase("Customer"))) {
                    isMandatoryCondition = true;
                }
                String isTDSapplicableonCustomer_Value = dataMap.get("IsTDSapplicableoncust") != null ? dataMap.get("IsTDSapplicableoncust").toString() : null;
                boolean TDSapplicableOnParticularCustomer = true;
                if (!StringUtil.isNullOrEmpty(isTDSapplicableonCustomer_Value)) {
                    if (isTDSapplicableonCustomer_Value.equalsIgnoreCase("true") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("1") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("T") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("YES")) {
                        TDSapplicableOnParticularCustomer = true;
                    } else if (isTDSapplicableonCustomer_Value.equalsIgnoreCase("false") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("0") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("F") || isTDSapplicableonCustomer_Value.equalsIgnoreCase("NO")) {
                        TDSapplicableOnParticularCustomer = false;
                    }
                }
                if (TDSapplicableOnParticularVendor && TDSapplicableOnParticularCustomer) {
                    if ((column.equals("DeducteeType") && mode.equalsIgnoreCase("Vendor")) || column.equals("PanStatus")) {
                        isMandatoryCondition = true;
                    }
                    if ((column.equals("NatureOfPayment") && mode.equalsIgnoreCase("Vendor"))) {//ERP-28976
                        isMandatoryCondition = true;
                    }
                }
            }else if (countryCheck && isExciseApplicable && mode.equalsIgnoreCase("Vendor")) {
                if (column.equals("ManufacturerType")) {
                    isMandatoryCondition = true;
                }
            }
          }else if (mode.equalsIgnoreCase(Constants.IMPORT_PRODUCT_PRICE)) {
              /*
               check if at lease 1 price column is present or not also if both column is not present then make it as 'Conditional Mandatory'
              */
            if (column.equals("PurchasePrice") ||column.equals("SalesPrice")) {
                if(!((dataMap.containsKey("PurchasePrice") && dataMap.get("PurchasePrice")!=null) || (dataMap.containsKey("SalesPrice") && dataMap.get("SalesPrice")!=null))){
                  isMandatoryCondition = true;  
          }
            }
        }
        return isMandatoryCondition;
    }
    
    public Object validateDataForDate(Map<String, Object> requestParams, JSONObject columnConfig, String csvHeader, Object vDataValue, String data, String dateFormat) throws DataInvalidateException, ParseException, JSONException, ServiceException {
        //below commented code is already present just moved it into method for reusability purpose

        String ldf = dateFormat != null ? dateFormat : (data == null ? df : (data.length() > 10 ? df_full : df));
        boolean isMandatory = columnConfig.optBoolean("isNotNull", false);
        boolean customflag = columnConfig.optBoolean("customflag", false);
        String pref = (String) requestParams.get("masterPreference");
        try {
            /* ERP-31401 - Check Length of date from file and date format matches or not*/
            if(data.length() != ldf.length()){
                throw new DataInvalidateException();
            }
            DateFormat sdf = new SimpleDateFormat(ldf);
            sdf.setLenient(false);//it is set false so that exact match get parsed
            vDataValue = StringUtil.isNullOrEmpty(data) ? null : sdf.parse(data);
            if (customflag && vDataValue != null) {
                vDataValue = new SimpleDateFormat(df_customfield).format(vDataValue);
            }
        } catch (Exception ex) { //if date not parsable or not valid the
            if (pref.equals("1") && !isMandatory) {//add empty case when field is not mandatory
                vDataValue = getDefaultValue(columnConfig, requestParams);
            } else {
                throw new DataInvalidateException("Incorrect date format for " + csvHeader + ", Please specify values in " + ldf + " format.");
            }
        }
        return vDataValue;
    }
    
    public Object validateComboData(Map<String, Object> requestParams, JSONObject columnConfig, String csvHeader, Object vDataValue, String data, String dateFormat, HashMap<String, Object> dataMap) throws DataInvalidateException, ParseException, JSONException, ServiceException {

        JSONObject paramJobj = new JSONObject();
        String fieldName = "", companyid = "", dbValue = "",stateValidator="",countryid="";
        String entityId = "", fieldId = "", errorMsg = "";
        int colNum = 0, xType = 0, moduleId = 0, gstConfigType = 0;

        Locale locale = null;
        if (requestParams.containsKey("locale")) {
            locale = (Locale) requestParams.get("locale");
        }
        if (requestParams.containsKey("companyid")) {
            companyid = (String) requestParams.get("companyid");
        }
        if (columnConfig.has("colnum") && !StringUtil.isNullOrEmpty(String.valueOf(columnConfig.get("colnum")))) {
            colNum = Integer.parseInt(String.valueOf(columnConfig.get("colnum")));
        }
        if (columnConfig.has("id") && !StringUtil.isNullOrEmpty(String.valueOf(columnConfig.get("id")))) {
            fieldId = (String) columnConfig.get("id");
        }
        if (columnConfig.has("dataindex") && !StringUtil.isNullOrEmpty(String.valueOf(columnConfig.get("dataindex")))) {
            fieldName = (String) columnConfig.get("dataindex");
        }
        if (columnConfig.has("xtype") && !StringUtil.isNullOrEmpty(String.valueOf(columnConfig.get("xtype")))) {
            xType = Integer.parseInt(String.valueOf(columnConfig.get("xtype")));
        }
        if (columnConfig.has("moduleid") && !StringUtil.isNullOrEmpty(String.valueOf(columnConfig.get("moduleid")))) {
            moduleId = Integer.parseInt(String.valueOf(columnConfig.get("moduleid")));
        }
        if (columnConfig.has("gstconfigtype") && columnConfig.get("gstconfigtype") != null) {
            gstConfigType = Integer.parseInt(String.valueOf(columnConfig.get("gstconfigtype")));
        }
        if(dataMap.containsKey("Output CGST") && dataMap.get("Output CGST") != null){
            stateValidator=String.valueOf(dataMap.get("Output CGST"));
        }
        if(dataMap.containsKey("Input CGST") && dataMap.get("Input CGST") != null){
            stateValidator=String.valueOf(dataMap.get("Input CGST"));
        }
        if (!StringUtil.isNullOrEmpty(companyid)) {
            KwlReturnObject companyResult = null;
            companyResult = KwlCommonTablesDAOObj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            countryid = company.getCountry().getID();

        }
        paramJobj.put("companyId", companyid);
        paramJobj.put("fieldValue", data);
        paramJobj.put("fieldId", fieldId);
        paramJobj.put("fieldName", "Custom_" + csvHeader);
        paramJobj.put("xType", xType);
        paramJobj.put("moduleId", moduleId);
//        if(csvHeader.equals("State") && moduleId==Constants.GSTModule){
        if (countryid.equalsIgnoreCase(Constants.INDIA_COUNTRYID) && (gstConfigType == Constants.GST_CONFIG_ISFORGST || gstConfigType == Constants.GST_CONFIG_CUSTOM_TO_ENTITY)) {
            if (!data.equals("Other") || !stateValidator.equalsIgnoreCase("NA")) {
                /*
                 * Here retriving Entity data first to validate its custom data i.e from MultiEntityDimensionCustomData
                 */
                String entityFileValue = String.valueOf(dataMap.get("Entity"));
                paramJobj.put("fieldName", "Custom_Entity");
                paramJobj.put("fieldValue", entityFileValue);
                paramJobj.put("moduleId", Constants.GSTModule);
                entityId = importDao.getValuesForLinkedRecords(paramJobj);
                if (!StringUtil.isNullOrEmpty(entityId)) {
                    paramJobj.put("colNum", colNum);
                    paramJobj.put("comboId", entityId);
                    dbValue = importDao.getEntityCustomData(paramJobj);
                    if (StringUtil.isNullOrEmpty(dbValue) || !dbValue.equalsIgnoreCase(data)) {
                        throw new DataInvalidateException("Incorrect value " + data + " for " + csvHeader);
                    }
                } else {
                    errorMsg += data + " " + messageSource.getMessage("acc.gst.entity.cannot.validated", null, locale) + " " + entityFileValue;
                    throw new DataInvalidateException(errorMsg);
                }
            }
        } else {
            /*
             * In else case all values validated except State.
             */
            entityId = importDao.getValuesForLinkedRecords(paramJobj);
            if (StringUtil.isNullOrEmpty(entityId)) {
                throw new DataInvalidateException("Dimension value " + data + " for " + csvHeader + " is not avaibale in master configuration.");
            }
        }

        return vDataValue;
    }
    /**
     * Validate GST Rule if Already present and Used in Transaction then Not
     * update rule. ERP-38317
     *
     * @param requestParams
     * @param columnConfig
     * @param csvHeader
     * @param vDataValue
     * @param data
     * @param dateFormat
     * @param dataMap
     * @throws DataInvalidateException
     * @throws ParseException
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject validateGSTRulesUsedInTransaction(Map<String, Object> requestParams, JSONObject columnConfig, String csvHeader, Object vDataValue, String data, String dateFormat, HashMap<String, Object> dataMap, HashMap<String, Object> columnHeaderMap) throws DataInvalidateException, ParseException, JSONException, ServiceException {
        String companyid = "", countryid = "";
        JSONObject returnObject = new JSONObject();
        Locale locale = null;
        String failureMsg = "";
        if (requestParams.containsKey("locale")) {
            locale = (Locale) requestParams.get("locale");
        }
        if (requestParams.containsKey("companyid")) {
            companyid = (String) requestParams.get("companyid");
        }
        if (requestParams.containsKey("countryid")) {
            countryid = (String) requestParams.get("countryid");
        }
        /**
         * Entity Dimension local State column number
         */
        int entityColnumforState = importDao.getColumnFromFieldParams(Constants.STATE, companyid, Constants.GSTModule, 0);
        DateFormat df = new SimpleDateFormat(dateFormat);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.yyyyMMdd);
        JSONObject requestParamsObj = new JSONObject();
        requestParamsObj.put(Constants.locale, locale);
        requestParamsObj.put(Constants.df, df);
        requestParamsObj.put("countryid", countryid);
        requestParamsObj.put("companyid", companyid);
        /**
         * Get GST terms details from DB to 
         *  check each terms with given in file
         */
        HashMap<String, Object> params = new HashMap<String, Object>();
        //params.putAll(requestParams);
        params.put("termType", 7);
        params.put("companyid", companyid);
        if (requestParams.containsKey("salesOrPurchase")) {
            params.put("subModuleFlag", Integer.parseInt(requestParams.get("salesOrPurchase").toString()));
        }
        List<Object[]> listGstTerm = importDao.getGSTTermDetails(params);
        Set<String> GSTRuleUsedModulesSet = new HashSet<String>();
        Set<String> GSTRulesUsed = new HashSet<String>();
        for (Object[] listGstTermObj : listGstTerm) {
            String termID = (String) listGstTermObj[0];
            String termName = (String) listGstTermObj[1];
            /**
             * Check each terms column from 
             * columnHeaderMap(Terms header from file)
             */
            for (Map.Entry<String, Object> columnHeaderMapEntrySet : columnHeaderMap.entrySet()) {
                String columnName = columnHeaderMapEntrySet.getKey();
                Object value = columnHeaderMapEntrySet.getValue();
                String termPercentageValue = dataMap.containsKey(termName) ? (String) dataMap.get(termName) : "";
                if (termName.equalsIgnoreCase(columnName) && !StringUtil.isNullOrEmpty(termPercentageValue) && !termPercentageValue.equalsIgnoreCase("NA")) {
                    String productTaxClassName = dataMap.containsKey(Constants.GSTProdCategory) ? (String) dataMap.get(Constants.GSTProdCategory) : "";
                    JSONObject paramJobj = new JSONObject();
                    paramJobj.put("fieldName", "Custom_" + Constants.GSTProdCategory);
                    paramJobj.put("fieldValue", productTaxClassName);
                    paramJobj.put("companyId", companyid);
                    paramJobj.put("moduleId", Constants.Acc_Product_Master_ModuleId);
                    String productTaxClassID = importDao.getValuesForLinkedRecords(paramJobj);

                    String entityName = dataMap.containsKey(Constants.ENTITY) ? (String) dataMap.get(Constants.ENTITY) : "";
                    paramJobj = new JSONObject();
                    paramJobj.put("fieldName", "Custom_" + Constants.ENTITY);
                    paramJobj.put("fieldValue", entityName);
                    paramJobj.put("companyId", companyid);
                    paramJobj.put("moduleId", Constants.GSTModule);
                    String entityID = importDao.getValuesForLinkedRecords(paramJobj);

                    Date applieddate = dataMap.containsKey(Constants.appliedDate) ? (Date) dataMap.get(Constants.appliedDate) : null;
                    String stateName = dataMap.containsKey(Constants.STATE) ? (String) dataMap.get(Constants.STATE) : "";
                    paramJobj = new JSONObject();
                    paramJobj.put("fieldName", "Custom_" + Constants.STATE);
                    paramJobj.put("fieldValue", stateName);
                    paramJobj.put("companyId", companyid);
                    paramJobj.put("moduleId", Constants.GSTModule);
                    String stateID = importDao.getValuesForLinkedRecords(paramJobj);
                    String cityID = "", countyID = "", countyName = "", cityName = "";
                    boolean isMerchantExporter = dataMap.containsKey(Constants.isMerchantExporter) && dataMap.get(Constants.isMerchantExporter)!=null ? (Boolean) dataMap.get(Constants.isMerchantExporter) : false;
                    /**
                     * County and City is only for US country
                     * so get County and City details
                     */
                    if (!StringUtil.isNullOrEmpty(countryid) && (Integer.parseInt(countryid) == Constants.USA_country_id)) {
                        cityName = dataMap.containsKey(Constants.CITY) ? (String) dataMap.get(Constants.CITY) : "";
                        paramJobj = new JSONObject();
                        paramJobj.put("fieldName", "Custom_" + Constants.CITY);
                        paramJobj.put("fieldValue", cityName);
                        paramJobj.put("companyId", companyid);
                        paramJobj.put("moduleId", Constants.GSTModule);
                        cityID = importDao.getValuesForLinkedRecords(paramJobj);

                        countyName = dataMap.containsKey(Constants.COUNTY) ? (String) dataMap.get(Constants.COUNTY) : "";
                        paramJobj = new JSONObject();
                        paramJobj.put("fieldName", "Custom_" + Constants.COUNTY);
                        paramJobj.put("fieldValue", countyName);
                        paramJobj.put("companyId", companyid);
                        paramJobj.put("moduleId", Constants.GSTModule);
                        countyID = importDao.getValuesForLinkedRecords(paramJobj);
                    }
                    if (!StringUtil.isNullOrEmpty(countryid) && !StringUtil.isNullOrEmpty(productTaxClassName) && !StringUtil.isNullOrEmpty(entityName)
                            && applieddate != null) {
                        requestParamsObj.put("termName", termName);
                        requestParamsObj.put("productTaxClassName", productTaxClassName);
                        requestParamsObj.put("entityName", entityName);
                        requestParamsObj.put("stateName", stateName);
                        JSONObject validateParams = new JSONObject();
                        validateParams.put(Constants.term, termID);
                        validateParams.put(Constants.entity, entityID);
                        validateParams.put(Constants.percentage, termPercentageValue);
                        validateParams.put(Constants.type, 1);
                        validateParams.put(Constants.appliedDate, applieddate);
                        validateParams.put(Constants.prodCategory, productTaxClassID);
                        validateParams.put(Constants.isMerchantExporter, isMerchantExporter);

                        if ((Integer.parseInt(countryid) == Constants.indian_country_id) && stateName.toLowerCase().contains("other".toLowerCase())) {
                            paramJobj.put("colNum", entityColnumforState);
                            paramJobj.put("comboId", entityID);
                            paramJobj.put("xType", 4);
                            paramJobj.put("companyId", companyid);
                            String entityLocalStateID = importDao.getEntityCustomData(paramJobj);
                            paramJobj.put(Constants.fieldlabel, Constants.STATE);
                            paramJobj.put(Constants.moduleid, Constants.GSTModule);
                            paramJobj.put(Constants.companyid, companyid);
                            paramJobj.put("entityStateid", entityLocalStateID);
                            /**
                             * For INDIA GST "Other" state name
                             * means All states except the state of entity.
                             */
                            List<Object[]> stateList = importDao.getStatesFromFieldCombodata(paramJobj);
                            for (Object[] state : stateList) {
                                stateID = state.length>0 && state[0]!=null ? state[0].toString() :"";
                                stateName = state.length>1 && state[1]!=null ? state[1].toString() :"";
                                if (!StringUtil.isNullOrEmpty(stateID)) {
                                    validateParams.put(Constants.shippedLocation1, stateID);
                                    requestParamsObj.put("stateName", stateName);
                                    JSONObject resultObj = getGSTRuleDetails(requestParamsObj, validateParams);
                                    if (resultObj.optBoolean("isUsed", false)) {
                                        GSTRulesUsed.add(stateName + " :" + sdf.format(applieddate));
                                        String GSTRuleUsedModules[] = resultObj.optString("GSTRuleUsedModules", "").split(",");
                                        for (String GSTRuleUsedModule : GSTRuleUsedModules) {
                                            if(!StringUtil.isNullOrEmpty(GSTRuleUsedModule)){
                                                GSTRuleUsedModulesSet.add(GSTRuleUsedModule);
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            /*
                            * This condition for All except INDIA GST State column value not "Other"
                            */
                            validateParams.put(Constants.shippedLocation1, stateID);
                            String GSTRulesUsedstr = " " + stateName;
                            if (!StringUtil.isNullOrEmpty(cityID)) {
                                validateParams.put(Constants.shippedLocation2, cityID);
                                requestParamsObj.put("cityName", cityName);
                                GSTRulesUsedstr += " :" + cityName;
                            }
                            if (!StringUtil.isNullOrEmpty(countyID)) {
                                validateParams.put(Constants.shippedLocation3, countyID);
                                requestParamsObj.put("countyName", countyName);
                                GSTRulesUsedstr += " :" + countyName;
                            }
                            JSONObject resultObj = getGSTRuleDetails(requestParamsObj, validateParams);
                            if (resultObj.optBoolean("isUsed", false)) {
                                GSTRulesUsedstr += " :" + sdf.format(applieddate) + ",";
                                GSTRulesUsed.add(GSTRulesUsedstr);
                                String GSTRuleUsedModules[] = resultObj.optString("GSTRuleUsedModules", "").split(",");
                                for (String GSTRuleUsedModule : GSTRuleUsedModules) {
                                    if (!StringUtil.isNullOrEmpty(GSTRuleUsedModule)) {
                                        GSTRuleUsedModulesSet.add(GSTRuleUsedModule);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        /**
         * Check if GST rule used in transaction or not
         * if used then create message to show user 
         */
        if(!GSTRulesUsed.isEmpty() && !GSTRuleUsedModulesSet.isEmpty()){
            failureMsg += messageSource.getMessage("acc.gstrr.cannotEditRuleImportLog", new Object[]{StringUtils.join(GSTRulesUsed,","), StringUtils.join(GSTRuleUsedModulesSet, ",")}, locale);
        }
        returnObject.put("failureMsg", failureMsg);
        return returnObject;
    }
    
    /**
     * validate Billing and Shipping Address for Customer/Vendor import
     * @param requestParams
     * @param dataMap
     * @param columnConfig
     * @param column
     * @param columnHeaderMap
     * @throws DataInvalidateException
     * @throws ParseException
     * @throws JSONException
     * @throws ServiceException 
     */
    public void validateAddressFieldsForNewGST(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, JSONObject columnConfig, String column, HashMap<String, Object> columnHeaderMap) throws DataInvalidateException, ParseException, JSONException, ServiceException {
        String companyid = "";
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            companyid = (String) requestParams.get("companyid");
        }
        Company company = (Company) KwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
        String countryid = company != null ? company.getCountry().getID() : String.valueOf(0);
        if (!StringUtil.isNullOrEmpty(countryid)) {
            String csvHeader = (String) columnHeaderMap.get(column);
            csvHeader = (csvHeader == null ? csvHeader : csvHeader.replaceAll("\\.", " "));
            String pojoName = columnConfig.getString("pojoName");
            /**
             * If Current column is address related column then check it
             * respective dimension value present or not Check mapping -
             * Constants.addressToDimensionMap
             */
            if (!StringUtil.isNullOrEmpty(csvHeader) && Constants.addressToDimensionMap.containsKey(pojoName) && Constants.addressToDimensionMap.get(pojoName) != null) {
                String vDataValue = dataMap.get(column) == null ? null : dataMap.get(column).toString().replaceAll("\n", "");
                if(StringUtil.isNullOrEmpty(vDataValue)){
                    throw new DataInvalidateException("Empty data found in " + csvHeader + ", cannot set empty data for " + csvHeader + ".");
                }
                Locale locale = new Locale("en", "US");   //Default value English
                if (requestParams.containsKey("locale") && requestParams.get("locale") != null) {
                    locale = (Locale) requestParams.get("locale");
                }
                /**
                 * Get Custom dimension value from databases if value present
                 * then continue the validation process otherwise mark this
                 * column invalid
                 */
                String fieldLabel = Constants.addressToDimensionMap.get(pojoName);
                JSONObject paramJobj = new JSONObject();
                paramJobj.put("fieldlabel", fieldLabel);
                paramJobj.put("fieldvalue", vDataValue);
                paramJobj.put(Constants.companyKey, companyid);
                KwlReturnObject result = importDao.isCustomDataPresent(paramJobj);
                BigInteger valueCount = !result.getEntityList().isEmpty() ? (BigInteger) result.getEntityList().get(0) : BigInteger.valueOf(0);
                if (valueCount.intValue() <= 0) {
                    String errorMessages = messageSource.getMessage("acc.import.customerVendor.invalid.address.error", new Object[]{vDataValue, fieldLabel, csvHeader}, locale);
                    throw new DataInvalidateException(errorMessages);
                }
            }
        }
    }
    /**
     *
     * @param requestParams
     * @param validateParams
     * @return
     * @throws JSONException
     * @throws ParseException
     * @throws ServiceException
     */
    public JSONObject getGSTRuleDetails(JSONObject requestParams, JSONObject validateParams) throws JSONException, ParseException, ServiceException {
        JSONObject returnObj = new JSONObject();
        Map<String, Object> reqMap = new HashMap();
        DateFormat df = (DateFormat) requestParams.get(Constants.df);
        reqMap.put(Constants.term, validateParams.optString(Constants.term));
        reqMap.put(Constants.entity, validateParams.optString(Constants.entity));
        reqMap.put(Constants.percentage, validateParams.optDouble(Constants.percentage));
        reqMap.put(Constants.type, validateParams.optInt(Constants.type, 1));
        reqMap.put(Constants.appliedDate, (Date)validateParams.opt(Constants.appliedDate));
        reqMap.put(Constants.shippedLocation1, validateParams.optString(Constants.shippedLocation1));
        reqMap.put(Constants.shippedLocation2, validateParams.optString(Constants.shippedLocation2));
        reqMap.put(Constants.shippedLocation3, validateParams.optString(Constants.shippedLocation3));
        reqMap.put(Constants.shippedLocation4, validateParams.optString(Constants.shippedLocation4));
        reqMap.put(Constants.shippedLocation5, validateParams.optString(Constants.shippedLocation5));
        reqMap.put(Constants.prodCategory, validateParams.optString(Constants.prodCategory));
        reqMap.put(Constants.isMerchantExporter, validateParams.optBoolean(Constants.isMerchantExporter,false));
        reqMap.put(Constants.df, df);
        
        List returnList = importDao.getGSTRuleDetails(reqMap);
        if (returnList != null && !returnList.isEmpty() && returnList.get(0) != null) {
            String id = (String) returnList.get(0);
            requestParams.remove(Constants.id1);
            requestParams.put(Constants.id1, id);
            JSONObject res = isGSTRuleusedInTransaction(requestParams);
            if (res.optBoolean("isUsed", false)) {
                returnObj.put("isUsed", res.optBoolean("isUsed", false));
                returnObj.put("GSTRuleUsedModules", res.optString("GSTRuleUsedModules", ""));
            }
        }
        return returnObj;
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     * @throws ParseException
     * @throws com.krawler.utils.json.base.JSONException
     */
    public JSONObject isGSTRuleusedInTransaction(JSONObject requestParams) throws ServiceException, ParseException, com.krawler.utils.json.base.JSONException {
        JSONObject jobj = new JSONObject();
        Map<String, Object> fieldParamsRequestMap;
        List<String> listTables = new ArrayList<>(Arrays.asList(Constants.DODetailTermMap, Constants.InvoiceDetailTermMap, Constants.PODetailTermMap, Constants.PRDetailTermMap, Constants.QuotationDetailTermMap, Constants.RADetailTermMap, Constants.ReceiptDetailTermMap, Constants.RODetailTermMap, Constants.SODetailTermMap, Constants.SRDetailTermMap, Constants.VQDetailTermMap));
        /**
         * HashMap for module names to table name Mapping
         */
        Map<String, String> forModuleNames = new HashMap<>();
        forModuleNames.put(Constants.DODetailTermMap, Constants.Delivery_Order);
        forModuleNames.put(Constants.InvoiceDetailTermMap, Constants.CUSTOMER_INVOICE + " or " + Constants.CASH_SALE); //CUSTOMER OR VENDOR INVOICE
        forModuleNames.put(Constants.PODetailTermMap, Constants.ACC_PURCHASE_ORDER);
        forModuleNames.put(Constants.PRDetailTermMap, Constants.PURCHASE_RETURN);
        forModuleNames.put(Constants.QuotationDetailTermMap, Constants.CUSTOMER_QUOTATION);
        forModuleNames.put(Constants.RADetailTermMap, Constants.RECEIVE_PAYMENT);
        forModuleNames.put(Constants.ReceiptDetailTermMap, Constants.VENDOR_INVOICE + " or " + Constants.CASH_PURCHASE);
        forModuleNames.put(Constants.RODetailTermMap, Constants.Goods_Receipt);
        forModuleNames.put(Constants.SODetailTermMap, Constants.SALESORDER);
        forModuleNames.put(Constants.SRDetailTermMap, Constants.SALES_RETURN);
        forModuleNames.put(Constants.VQDetailTermMap, Constants.VENDOR_QUOTATION);

        KwlReturnObject result = null;
        fieldParamsRequestMap = new HashMap();
        if (!StringUtil.isNullOrEmpty(requestParams.optString(Constants.id1))) {
            fieldParamsRequestMap.put(Constants.id1, requestParams.optString(Constants.id1));
        }
        fieldParamsRequestMap.put(Constants.tableName, listTables);
        fieldParamsRequestMap.put(Constants.modulename, forModuleNames);

        result = importDao.isGSTRuleusedInTransaction(fieldParamsRequestMap);
        if (!result.isSuccessFlag()) {
            jobj.put("isUsed", true);
            jobj.put("GSTRuleUsedModules", result.getMsg());
        }
        return jobj;
    }
    public Object validateOpeningPaymentSpecificCustomValidation(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, JSONObject columnConfig,String csvHeader, String data, String dateFormat,Object vDataValue) throws DataInvalidateException, ServiceException, ParseException, JSONException {
        String mode = (String) requestParams.get("modName");
        String columnHeader = columnConfig.optString("columnName","");
        String companyid = (String) requestParams.get("companyid");
        
        if (columnHeader.equals("Payment Status") && dataMap.containsKey("PaymentStatus")) {
            if (!StringUtil.isNullOrEmpty(data)) {
                int paymentDetailType = -1;
                if (dataMap.containsKey("PaymentMethod") && dataMap.get("PaymentMethod") != null) {
                    String paymentMethodID = dataMap.get("PaymentMethod").toString();
                    if (!StringUtil.isNullOrEmpty(paymentMethodID)) {
                        KwlReturnObject returnObject = importDao.getPaymentMethodDetail(dataMap.get("PaymentMethod").toString(), companyid);
                        if (returnObject != null && !returnObject.getEntityList().isEmpty() && returnObject.getEntityList().get(0) != null) {
                            List listPaymentMethod = returnObject.getEntityList();
                            Object[] dateValuesobj = (Object[]) listPaymentMethod.get(0);
                            if (dateValuesobj != null) {
                                if (dateValuesobj[0] != null) {
                                    paymentDetailType = (int) dateValuesobj[0];
                                }
                            }
                        }
                    }
                }
                //There should be payment method and it should not be cash then payment status can only be cleared or uncleared
                if ((paymentDetailType == -1 || paymentDetailType != 0) && !(data.equalsIgnoreCase("Uncleared") || data.equalsIgnoreCase("Cleared"))) {
                    throw new DataInvalidateException("Incorrect Payment Status type value for " + csvHeader + ". It should be either Cleared or Uncleared.");
                } else{
                    vDataValue = null;
                }
            } else {
                int paymentDetailType = -1;
                if (dataMap.containsKey("PaymentMethod") && dataMap.get("PaymentMethod") != null) {
                    String paymentMethodID = dataMap.get("PaymentMethod").toString();
                    if (!StringUtil.isNullOrEmpty(paymentMethodID)) {
                        KwlReturnObject returnObject = importDao.getPaymentMethodDetail(dataMap.get("PaymentMethod").toString(), companyid);
                        if (returnObject != null && !returnObject.getEntityList().isEmpty() && returnObject.getEntityList().get(0) != null) {
                                    List listPaymentMethod = returnObject.getEntityList();
                                    Object[] dateValuesobj = (Object[]) listPaymentMethod.get(0);
                                    if (dateValuesobj != null) {
                                        if (dateValuesobj[0] != null) {
                                            paymentDetailType = (int) dateValuesobj[0];
                                }
                            }
                        }
                    }
                }
                if (StringUtil.isNullOrEmpty(data) && paymentDetailType == Constants.bank_detail_type) {
                    throw new DataInvalidateException("Payment Status is not available.");
                }
            }
        } else if (columnHeader.equals("Clearance Date") && dataMap.containsKey("ClearenceDate")) {
            String status = "Cleared";
            if (dataMap.containsKey("PaymentStatus") && dataMap.get("PaymentStatus") != null) {
                status = dataMap.get("PaymentStatus").toString();
            }
            if (status.equalsIgnoreCase("Uncleared")) {//No need to give clearance date if status is uncleared. So no validation for this field and returnded as null
                vDataValue = null;
            } else {// when status is clear and paymenth method detail type is cash then need to check validation for clearance date
                int paymentDetailType = -1;
                if (dataMap.containsKey("PaymentMethod") && dataMap.get("PaymentMethod") != null) {
                    String paymentMethodID = dataMap.get("PaymentMethod").toString();
                    if (!StringUtil.isNullOrEmpty(paymentMethodID)) {
                        KwlReturnObject returnObject = importDao.getPaymentMethodDetail(dataMap.get("PaymentMethod").toString(), companyid);
                        if (returnObject != null && !returnObject.getEntityList().isEmpty() && returnObject.getEntityList().get(0) != null) {
                            List listPaymentMethod = returnObject.getEntityList();
                            Object[] dateValuesobj = (Object[]) listPaymentMethod.get(0);
                            if (dateValuesobj != null) {
                                if (dateValuesobj[0] != null) {
                                    paymentDetailType = (int) dateValuesobj[0];
                                }
                            }
                        }
                    }
                }
                /*
                 * paymentDetailType==-1 then payment method is not present in
                 * file or given payment method is not valid. so oon this case
                 * need to validate. because we are not sure for payment method.
                 * it can be cash type paymentDetailType!=0 then it is not cash type
                 * payment method. In this case we have to validate
                 */
                if (paymentDetailType == -1 || paymentDetailType != 0) {
                    if (StringUtil.isNullOrEmpty(data)) {//if date value is balnk then giving exeception messagre
                        throw new DataInvalidateException("Empty data found in " + csvHeader + ", cannot set empty data for " + columnHeader + ".");
                    } else {
                        vDataValue = validateDataForDate(requestParams, columnConfig, csvHeader, vDataValue, data, dateFormat);
                        //Once clerance date is validated need to check for it is greater that check date or not. nad this is only possible if valid check date is given
                        if (dataMap.containsKey("ChequeDate") && dataMap.get("ChequeDate") != null && !StringUtil.isNullOrEmpty(dataMap.get("ChequeDate").toString())) {
                            Date ChequeDate=null;
                            Date ClearenceDate = null;
                            try {
                                String ldf = dateFormat != null ? dateFormat : (data == null ? df : (data.length() > 10 ? df_full : df));
                                DateFormat sdf = new SimpleDateFormat(ldf);
                                String ChequeDateStr = (String) dataMap.get("ChequeDate");
                                String ClearenceDateStr = (String) dataMap.get("ClearenceDate");
                                ChequeDate = sdf.parse(ChequeDateStr);
                                ClearenceDate = sdf.parse(ClearenceDateStr);
                                if (ChequeDate.compareTo(ClearenceDate) > 0) {
                                    throw new DataInvalidateException("Clearence date should be greter than Cheque date.");
                                }
                            } catch (ParseException ex) {
                            }
                        }
                    }
                }
            }
        }else if (mode.equalsIgnoreCase(Constants.Acc_Opening_Receipt_modulename)) {//above columns are common for both opening make payment and Receive payment so there is no check of module
            if (columnHeader.equals("Bank Name") && dataMap.containsKey("BankName") && !StringUtil.isNullObject(dataMap.get("BankName")) && !StringUtil.isNullOrEmpty(dataMap.get("BankName").toString())) {
                if (!StringUtil.isNullOrEmpty(data)) {
                    KwlReturnObject returnObject = importDao.getBankNameMasterItemName(companyid, data);
                    if (returnObject.getEntityList().isEmpty()) {
                        throw new DataInvalidateException("Incorrect Bank Name type value for " + csvHeader + ". Please add new Bank Name as \"" + data + "\" with other details.");
                    } 
                } else {
                    vDataValue = null;
                }
            }
        } 
        return vDataValue;
    }
    
    public Object validateAccountSpecificCustomValidation(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, String columnHeader, String csvHeader, Object vDataValue, String data, int maxLength) throws DataInvalidateException {
        //DBS Related columns
        Set DBSBankHeaderSet = new HashSet();
        DBSBankHeaderSet.add("Sender's Company Id");
        DBSBankHeaderSet.add("Originating Bank Number/Code");
        DBSBankHeaderSet.add("Originating Account Name/Originator's Name");
        DBSBankHeaderSet.add("Originating Branch Number/Code");
        DBSBankHeaderSet.add("Originating Account Number");
        DBSBankHeaderSet.add("Bank Daily Limit");
        //CIBM Related columns
        Set CIBMBankHeaderSet = new HashSet();
        CIBMBankHeaderSet.add("Service Code");
        CIBMBankHeaderSet.add("Bank Account Number");
        CIBMBankHeaderSet.add("Orderer Name");
        CIBMBankHeaderSet.add("CIBM Currency Code");
        CIBMBankHeaderSet.add("Settlement Mode");
        CIBMBankHeaderSet.add("Posting Indicator");

        boolean isActivateIBG = (requestParams.containsKey("isActivateIBG") && requestParams.get("isActivateIBG") != null) ? Boolean.parseBoolean(requestParams.get("isActivateIBG").toString()) : false;

        boolean isIBGDetails = false;
        if (dataMap.containsKey("IBGBank") && dataMap.get("IBGBank") != null) {
            String isIBGDetailsValue = "";
            isIBGDetailsValue = dataMap.get("IBGBank").toString();
            if (isIBGDetailsValue.equalsIgnoreCase("true") || isIBGDetailsValue.equalsIgnoreCase("1") || isIBGDetailsValue.equalsIgnoreCase("T") || isIBGDetailsValue.equalsIgnoreCase("YES")) {
                isIBGDetails = true;
            }
        }

        /*
         * isIBGDetails = false : user have given value false in file or column
         * is missing in file or not mapped . In this case system will not saved
         * IBG details So no need to validate data
         *
         * isIBGDetails = true : user have given value trye in file. In this
         * case system will save IBG details. So need to validate data
         */

        if ((DBSBankHeaderSet.contains(columnHeader) || CIBMBankHeaderSet.contains(columnHeader)) && isActivateIBG && isIBGDetails) {//If IBG Activated in company preferences and IBG Details is true, only then we need to validate IBG Related Data
            if (dataMap.containsKey("IbgbankName") && dataMap.get("IbgbankName") != null) {
                String IbgbankName = dataMap.get("IbgbankName").toString();
                if (DBSBankHeaderSet.contains(columnHeader) && (IbgbankName.equalsIgnoreCase(Constants.DBS_BANK_NAME) || IbgbankName.equalsIgnoreCase(String.valueOf(Constants.DBS_BANK_Type)))) {//validate only DBC related columns
                    if (StringUtil.isNullOrEmpty(data)) {
                        throw new DataInvalidateException("Empty data found in " + csvHeader + ", cannot set empty data for " + columnHeader + ".");
                    } else {
                        if (columnHeader.equals("Sender's Company Id")) {
                            if (!Pattern.matches("^[a-zA-Z]+$", data)) {
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It should contains only alphabets.");
                            }
                            if (data.length() > 8) {
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It can have max 8 characters.");
                            }
                        } else if (columnHeader.equals("Originating Bank Number/Code")) {
                            if (!Pattern.matches("^[0-9]+$", data)) {
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It should contains only numbers.");
                            }
                            if (data.length() > 4) {
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It can have max 4 digit numbers.");
                            }
                        } else if (columnHeader.equals("Originating Branch Number/Code")) {
                            if (!Pattern.matches("^[0-9]+$", data)) {
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It should contains only numbers.");
                            }
                            if (data.length() > 3) {
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It can have max 3 digit numbers.");
                            }
                        } else if (columnHeader.equals("Originating Account Number")) {
                            if (!Pattern.matches("^[0-9]+$", data)) {
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It should contains only numbers.");
                            }
                            if (data.length() > 11) {
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It can have max 11 digit numbers.");
                            }
                        } else if (columnHeader.equals("Bank Daily Limit")) {
                            data = data.replaceAll(",", "");
                            try {
                                vDataValue = Double.parseDouble(data);
                            } catch (NumberFormatException ex) {
                                throw new DataInvalidateException("Incorrect numeric value for " + csvHeader + ", Please ensure that value type of " + csvHeader + " matches with the " + columnHeader + ".");
                            }
                        }
                    }
                } else if (CIBMBankHeaderSet.contains(columnHeader) && (IbgbankName.equalsIgnoreCase(Constants.CIBM_BANK_NAME) || IbgbankName.equalsIgnoreCase(String.valueOf(Constants.CIMB_BANK_Type)))) {//validate only CIMB related columns
                    if (StringUtil.isNullOrEmpty(data)) {
                        throw new DataInvalidateException("Empty data found in " + csvHeader + ", cannot set empty data for " + columnHeader + ".");
                    } else {
                        if (columnHeader.equals("Service Code")) {
                            if(!(data.equals(String.valueOf(1)) || data.equals(String.valueOf(2)) || data.equals(String.valueOf(3)))){//Service code can only be 1,2,3 . Other than this given exception
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It can only be 1 or 2 or 3.");
                            }
                        } else if (columnHeader.equals("Bank Account Number")) {
                            if (!Pattern.matches("^[0-9]+$", data)) {
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It should contains only numbers.");
                            }
                            if (data.length() > 10) {
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It can have max 10 digit numbers.");
                            }
                        } else if (columnHeader.equals("CIBM Currency Code")) {
                            if(!data.equals(Constants.SGD_CURRENCY_CODE)){
                                throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It can only be "+Constants.SGD_CURRENCY_CODE+".");
                            }
                        } else if (columnHeader.equals("Settlement Mode")) {
                           int settlementMode= getSettlementModeValue(data);
                           if(settlementMode==-1){
                               throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It can only be "+Constants.Settlement_Mode_Batch_Name+" or "+Constants.Settlement_Mode_Real_Time_Name+".");
                           }
                           vDataValue = settlementMode;
                        } else if (columnHeader.equals("Posting Indicator")) {
                           int postingIndicator= getPostingIndicatorValue(data);
                           if(postingIndicator==-1){
                               throw new DataInvalidateException("Invalid data found in " + csvHeader + ",It can only be "+Constants.Posting_Indicator_Consolidated_Name+" or "+Constants.Posting_Indicator_Individual_Name+".");
                           }
                           vDataValue = postingIndicator;
                        }
                    }
                }
            }
        }
        return vDataValue;
    }

    public boolean validateDBSBankNumber(String data) throws DataInvalidateException{
        boolean validData = true;
        if(!StringUtil.isNullOrEmpty(data)){
            if(data.length()!=4){
                validData=false;
                throw new DataInvalidateException("Length of Receiving Bank Number/Code should be 4.");
            } 
        } else {
            validData = false;
            throw new DataInvalidateException("Receiving Bank Number/Code can not be empty.");
        }
        return validData ;
    }

    public boolean validateDBSBranchNumber(String data) throws DataInvalidateException{
        boolean validData = true;
        if(!StringUtil.isNullOrEmpty(data)){
            try{
                int branchNumber = Integer.parseInt(data);
            }catch(Exception e){
                throw new DataInvalidateException("Receiving Branch Number/Code should be integer.");
            }     
            if(data.length()>3){
                throw new DataInvalidateException("Length of Receiving Branch Number/Code can not be greater than 3");
            }
        } else {
            validData = false;
            throw new DataInvalidateException("Receiving Branch Number/Code can not be empty.");
        }
        return validData ;
    }
    public boolean validateDBSAccountNumber(String data) throws DataInvalidateException{
        boolean validData = true;
        if(!StringUtil.isNullOrEmpty(data)){
            if(!data.matches(".*\\d+.*")){
                throw new DataInvalidateException("Receiving Account Number should contain atleast one number.");
            }
            if(data.length()>10){
                throw new DataInvalidateException("Length of Receiving Account Number can not be greater than 10.");
            }
            
        } else {
            validData = false;
            throw new DataInvalidateException("Receiving Account Number can not be empty.");
        }
        return validData ;
    }
    public boolean validateDBSVendorCode(String companyId,String data) throws DataInvalidateException, ServiceException{
        boolean validData = true;
        if(!StringUtil.isNullOrEmpty(data)){
            KwlReturnObject returnObject = importDao.getVendorFromVendorCode(companyId,data);
            List list = returnObject.getEntityList();
            if(list == null || list.isEmpty()){
                throw new DataInvalidateException("No Vendor exists with Vendor Code "+data+".");
            }
        } else {
            validData = false;
            throw new DataInvalidateException("Vendor Code can not be empty.");
        }
        return validData ;
    }
    public boolean validatePayee(String companyid, String data) throws DataInvalidateException, ServiceException {
        Object vDataValue;
        boolean validData = true;
        if (!StringUtil.isNullOrEmpty(data)) {
            KwlReturnObject returnObject = importDao.getCustomerFromName(companyid, data);
            if (returnObject.getEntityList().isEmpty()) {
                validData = false;
            } else {
                vDataValue = returnObject.getEntityList().get(0).toString();
                validData = true;
            }

            if(!validData){
                returnObject = importDao.getVendorFromName(companyid, data);
                if (returnObject.getEntityList().isEmpty()) {
                    validData = false;
                } else {
                    vDataValue = returnObject.getEntityList().get(0).toString();
                    validData = true;
                }
            }
            
            if (!validData) {
                returnObject = importDao.getAccountFromName(companyid, data);
                if (returnObject.getEntityList().isEmpty()) {
                    validData = false;
                } else {
                    List list = returnObject.getEntityList();
                    Object[] details = (Object[]) list.get(0);
                    vDataValue = (String) details[0];;
                    validData = true;
                }
            }
        } else {
            throw new DataInvalidateException("Empty data found in Payee, cannot set empty data for Payee. ");
        }
        if (!validData) {
            throw new DataInvalidateException("Customer/Vendor/Account '" + data + "' is not found, Please provide correct Customer/Vendor/Account. ");
        }
        return validData;
    }
    
    public void validateOpeningSpecificValidation(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, String columnHeader, Object vDataValue, String data, String mode) throws DataInvalidateException, ServiceException {
        
        String pref = (String) requestParams.get("masterPreference");
        String companyid = (String) requestParams.get("companyid");
        String moduleName = (String) requestParams.get("moduleName");
        boolean isOpeningOrder=false;
        if(requestParams.containsKey("isOpeningOrder") && requestParams.get("isOpeningOrder")!=null && (columnHeader.equals("Sales Order Date") ||columnHeader.equals("Purchase Order Date"))){
            isOpeningOrder=(Boolean)requestParams.get("isOpeningOrder");
        }
        if ((columnHeader.equals("Transaction Date") || isOpeningOrder) && vDataValue != null) {
            Date transactionDate = (Date) vDataValue;
            transactionDate = removeTimefromDate(transactionDate);
            Date bookBeginningDate = (Date) requestParams.get("bookbeginning");
            if (transactionDate.after(bookBeginningDate)) {
                throw new DataInvalidateException("Transaction Date must be before Book Beginning Date.");
            }
        } else if (columnHeader.equals("Amount") && vDataValue != null) {
            double amount = (double) vDataValue;
            if (amount <= 0) {
                throw new DataInvalidateException("Amount cannot be zero or negative.");
            }
        } else if (columnHeader.equals("Exchange Rate") && !StringUtil.isNullOrEmpty(data) && vDataValue != null) {
            double exchangeRate = (double) vDataValue;
            if (exchangeRate <= 0) {
                throw new DataInvalidateException("Exchange Rate cannot be zero or negative.");
            }
        } 
        else if (moduleName.equalsIgnoreCase("Opening Purchase Invoice") && columnHeader.equals("Agent (Salesman)") && !StringUtil.isNullOrEmpty(data) && vDataValue != null) {
            if ((pref != null && pref.equalsIgnoreCase("2") || pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("1")) && !StringUtil.isNullOrEmpty(data)) {
                String masterGroupID = String.valueOf(20); //20 is groupid for Agent in mastergroup table 
                String srID = "";
                KwlReturnObject returnObject = importDao.getMasterItem(companyid, data, masterGroupID);
                if (!returnObject.getEntityList().isEmpty() && returnObject.getEntityList().get(0) != null) {
                    srID = returnObject.getEntityList().get(0).toString();
                }
                if (pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("2")) {
                    if (srID.equals("")) {
                        throw new DataInvalidateException("Agent not found in master list for Agent. ");
                    } else {
                        vDataValue = new String(srID);
                    }
                } else if (pref.equalsIgnoreCase("1")) {
                    vDataValue = srID;
                }
            }
        }

    }
     
    public Date removeTimefromDate(Date sampledate) { //removing time from date-Neeraj D
        Calendar c = Calendar.getInstance();
        c.setTime(sampledate);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        sampledate = c.getTime();
        return sampledate;
    }
     
    public Object validateCustomerSpecificCustomValidation(HashMap<String, Object> requestParams,HashMap<String, Object> dataMap,String columnHeader,Object vDataValue,String data, int maxLength) throws DataInvalidateException, ServiceException {
        /*
         * pref=2 <=> Add missing master records automatically (Most lenient)
         * pref=1 <=> Import the record except invalid/missing values pref=0 <=>
         * Don't import entire record (Most strict)
         */
        String pref = (String) requestParams.get("masterPreference");
        String companyid = (String) requestParams.get("companyid");
        boolean updateExistingRecordFlag=false;
        if(requestParams.containsKey("updateExistingRecordFlag") && requestParams.get("updateExistingRecordFlag")!=null && !StringUtil.isNullOrEmpty(requestParams.get("updateExistingRecordFlag").toString())){   //SDP-6182
            updateExistingRecordFlag = (Boolean)requestParams.get("updateExistingRecordFlag");
        }
        if (columnHeader.equals("Create As Vendor")) {
            boolean createAsVendor = false;
            if (!StringUtil.isNullOrEmpty(data)) {
                if (data.equalsIgnoreCase("true") || data.equalsIgnoreCase("1") || data.equalsIgnoreCase("T") || data.equalsIgnoreCase("YES")) {
                    createAsVendor = true;
                }
                if (createAsVendor) {//if option given to creare as Vendor but columns are missing.
                    if (!dataMap.containsKey("VendorAcccode") && !dataMap.containsKey("VendorAccount")) {
                        throw new DataInvalidateException("Column Vendor Code and Vendor Account not found to Create As Vendor, please provide these missing columns.");
                    } else if (!dataMap.containsKey("VendorAcccode")) {
                        throw new DataInvalidateException("Column Vendor Code not found to Create As Vendor, please provide this missing columns.");
                    } else if (!dataMap.containsKey("VendorAccount")) {
                        throw new DataInvalidateException("Column Vendor Account not found to Create As Vendor, please provide this missing columns.");
                    } else if(!dataMap.containsKey("ManufacturerType")){ 
                        Company company = (Company) KwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
                        int countryid = (company != null && company.getCountry() != null) ? Integer.parseInt(company.getCountry().getID()) : 0;
                        if (countryid == Constants.indian_country_id) {
                            throw new DataInvalidateException("Column Type Of Manufacturer not found to Create As Vendor, please provide this missing columns.");
                        }
                    }
                }
            }
            vDataValue=createAsVendor;
        } else if (columnHeader.equals("Vendor Code")) {
            if (dataMap.containsKey("Mapcustomervendor") && !StringUtil.isNullObject(dataMap.get("Mapcustomervendor"))) {
                String columnValue = dataMap.get("Mapcustomervendor").toString();
                boolean createAsVendor = false;
                if (columnValue.equalsIgnoreCase("true") || columnValue.equalsIgnoreCase("1") || columnValue.equalsIgnoreCase("T") || columnValue.equalsIgnoreCase("YES")) {
                    createAsVendor = true;
                }
                if (createAsVendor) {//if user going to create customer as vendor in that case vendor code and vendor account is mandatory
                    if (!StringUtil.isNullOrEmpty(data)) {
                        if (maxLength > 0 && data.length() > maxLength) {
                            if (pref.equalsIgnoreCase("0")) {// most restricted case and data is empty
                                throw new DataInvalidateException("Data length greater than " + maxLength + " for column Vendor Code.");
                            } else {//truncating data for other two cases
                                data = data.substring(0, maxLength);
                                vDataValue = data;
                            }
                        }
                        List list = getRefData(requestParams, "Vendor", "acccode", "ID", "", data);
                        if (!list.isEmpty()) {
                            /*
                            * ERP-36641 - Put vendor ID in update case of customer import
                            * ERP-38020
                            */
                            if(updateExistingRecordFlag){
                                // Get Vendor ID of existing record
                                dataMap.put("VendorID", list.get(0));
                            } else {
                                throw new DataInvalidateException("Vendor Code " + data + " is already exist, please provide unique value for Vendor Code. ");
                            }
                        }
                    } else {
                        throw new DataInvalidateException("Vendor Code not found to create customer as vendor,please provide unique value for Vendor Code. ");
                    }
                }
            }
        } else if (columnHeader.equals("Vendor Account")) {
            if (dataMap.containsKey("Mapcustomervendor") && !StringUtil.isNullObject(dataMap.get("Mapcustomervendor"))) {
                String columnValue = dataMap.get("Mapcustomervendor").toString();
                boolean createAsVendor = false;
                if (columnValue.equalsIgnoreCase("true") || columnValue.equalsIgnoreCase("1") || columnValue.equalsIgnoreCase("T") || columnValue.equalsIgnoreCase("YES")) {
                    createAsVendor = true;
                }
                if (createAsVendor) {//if user going to create customer as vendor in that case vendor code and vendor account is mandatory
                    if (!StringUtil.isNullOrEmpty(data)) {
                        int grpNature = 0;//liability
                        KwlReturnObject returnObject = importDao.getAccountByName(companyid, data, grpNature);
                        if (returnObject.getEntityList().isEmpty()) {
                            throw new DataInvalidateException("Vendor Account " + data + " is not found, please provide vendor account which have group nature liability. ");
                        } else {
                            vDataValue = returnObject.getEntityList().get(0).toString();
                        }
                    } else {
                        throw new DataInvalidateException("Vendor Account not found to create customer as vendor,please provide Vendor Account. ");
                    }
                }
            }
        } else if (columnHeader.equals("Shipping Route")) {//Customer Shipping Route
            if ((pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("1")) && !StringUtil.isNullOrEmpty(data)) {
                String masterGroupID = String.valueOf(28); //28 is groupid for Shipping Route in mastergroup table 
                String srID = "";
                KwlReturnObject returnObject = importDao.getMasterItem(companyid, data, masterGroupID);
                if (!returnObject.getEntityList().isEmpty() && returnObject.getEntityList().get(0) != null) {
                    srID = returnObject.getEntityList().get(0).toString();
                }
                if (pref.equalsIgnoreCase("0")) {
                    if (srID.equals("")) {
                        throw new DataInvalidateException("Shipping Route not found in master list for Shipping Route dropdown. ");
                    } else {
                        vDataValue = new String(srID);
                    }
                } else if (pref.equalsIgnoreCase("1")) {
                    vDataValue = srID;
                }
            }
            }
        return vDataValue;
    }
    
    public Object validateVendorSpecificCustomValidation(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, String columnHeader,Object vDataValue, String data, int maxLength) throws DataInvalidateException, ServiceException {
        /*
         * pref=2 <=> Add missing master records automatically (Most lenient)
         * pref=1 <=> Import the record except invalid/missing values pref=0 <=>
         * Don't import entire record (Most strict)
         */
        String pref = (String) requestParams.get("masterPreference");
        String companyid = (String) requestParams.get("companyid");
        boolean updateExistingRecordFlag=false;
        if(requestParams.containsKey("updateExistingRecordFlag") && requestParams.get("updateExistingRecordFlag")!=null && !StringUtil.isNullOrEmpty(requestParams.get("updateExistingRecordFlag").toString())){   //SDP-6182
            updateExistingRecordFlag = (Boolean)requestParams.get("updateExistingRecordFlag");
        }
        if (columnHeader.equals("Create As Customer")) {
            boolean createAsVendor = false;
            if (!StringUtil.isNullOrEmpty(data)) {
                if (data.equalsIgnoreCase("true") || data.equalsIgnoreCase("1") || data.equalsIgnoreCase("T") || data.equalsIgnoreCase("YES")) {
                    createAsVendor = true;
                }
                if (createAsVendor) {//if option given to creare as Vendor but columns are missing.
                    if (!dataMap.containsKey("CustomerAcccode") && !dataMap.containsKey("CustomerAccount")) {
                        throw new DataInvalidateException("Column Customer Code and Customer Account not found to Create As Customer, please provide these missing columns.");
                    } else if (!dataMap.containsKey("CustomerAcccode")) {
                        throw new DataInvalidateException("Column Customer Code not found to Create As Customer, please provide this missing columns.");
                    } else if (!dataMap.containsKey("CustomerAccount")) {
                        throw new DataInvalidateException("Column Customer Account not found to Create As Customer, please provide this missing columns.");
                    }
                }
            }
            vDataValue = createAsVendor;
        }else if (columnHeader.equals("Customer Code")) {
            if (dataMap.containsKey("Mapcustomervendor") && !StringUtil.isNullObject(dataMap.get("Mapcustomervendor"))) {
                String columnValue = dataMap.get("Mapcustomervendor").toString();
                boolean createAsVendor = false;
                if (columnValue.equalsIgnoreCase("true") || columnValue.equalsIgnoreCase("1") || columnValue.equalsIgnoreCase("T") || columnValue.equalsIgnoreCase("YES")) {
                    createAsVendor = true;
                }
                if (createAsVendor) {//if user going to create customer as vendor in that case vendor code and vendor account is mandatory
                    if (!StringUtil.isNullOrEmpty(data)) {
                        if (maxLength > 0 && data.length() > maxLength) {
                            if (pref.equalsIgnoreCase("0")) {// most restricted case and data is empty
                                throw new DataInvalidateException("Data length greater than " + maxLength + " for column Customer Code.");
                            } else {//truncating data for other two cases
                                data = data.substring(0, maxLength);
                                vDataValue = data;
                            }
                        }
                        List list = getRefData(requestParams, "Customer", "acccode", "ID", "", data);
                        if (!list.isEmpty()) {
                            /*
                            * ERP-36626 - Put customer ID in update case of vendor import
                            * ERP-37984
                            */
                            if(updateExistingRecordFlag){
                                // Get Customer ID of existing record
                                dataMap.put("CustomerID", list.get(0));
                            } else {
                                throw new DataInvalidateException("Customer Code " + data + " is already exist, please provide unique value for Customer Code. ");
                            }
                        }
                    } else {
                        throw new DataInvalidateException("Customer Code not found to create customer as vendor,please provide unique value for Customer Code. ");
                    }
                }
            }
        } else if (columnHeader.equals("Customer Account")) {
            if (dataMap.containsKey("Mapcustomervendor") && !StringUtil.isNullObject(dataMap.get("Mapcustomervendor"))) {
                String columnValue = dataMap.get("Mapcustomervendor").toString();
                boolean createAsVendor = false;
                if (columnValue.equalsIgnoreCase("true") || columnValue.equalsIgnoreCase("1") || columnValue.equalsIgnoreCase("T") || columnValue.equalsIgnoreCase("YES")) {
                    createAsVendor = true;
                }
                if (createAsVendor) {//if user going to create customer as vendor in that case vendor code and vendor account is mandatory
                    if (!StringUtil.isNullOrEmpty(data)) {
                        int grpNature = 1;//asset
                        KwlReturnObject returnObject = importDao.getAccountByName(companyid, data, grpNature);
                        if (returnObject.getEntityList().isEmpty()) {
                            throw new DataInvalidateException("Customer Account " + data + " is not found, please provide vendor account which have group nature asset. ");
                        } else {
                            vDataValue = returnObject.getEntityList().get(0).toString();
                        }
                    } else {
                        throw new DataInvalidateException("Customer Account not found to create customer as vendor,please provide Customer Account. ");
                    }
                }
            }
        }

        return vDataValue;
    }
    
    public Map<String,Object> validateCustomerVendorCommonCustomValidation(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, String columnHeader, Object vDataValue, String data, String mode) throws DataInvalidateException, ServiceException {
        /*
         * pref=2 <=> Add missing master records automatically (Most lenient)
         * pref=1 <=> Import the record except invalid/missing values 
         * pref=0 <=> Don't import entire record (Most strict)
         */
        String pref = (String) requestParams.get("masterPreference");
        String companyid = (String) requestParams.get("companyid");
        Map<String,Object> map=new HashMap<String,Object>();
        boolean columnFound=false;
        if (columnHeader.equals("Account")) { // tax for customer or vendor
            columnFound=true;
            if (!StringUtil.isNullOrEmpty(data)) {
                int grpNature = 0;//for vendor account, group nature is liabilty which value is 0
                if(mode.equalsIgnoreCase("Customer")){
                    grpNature=1;//for customer account, group nature is asset which value is 1
                }
                KwlReturnObject returnObject = importDao.getAccountByName(companyid, data, grpNature);
                if (returnObject.getEntityList().isEmpty()) {
                    if(mode.equalsIgnoreCase("Customer")){
                        throw new DataInvalidateException("Account " + data + " of group nature Asset is not found, Please provide correct Account. ");
                    } else {
                        throw new DataInvalidateException("Account " + data + " of group nature Liability is not found, Please provide correct Account. ");
                    }
                } else {
                    vDataValue = returnObject.getEntityList().get(0).toString();
                }
            } else {
                throw new DataInvalidateException("Empty data found in Account, cannot set empty data for Account. ");
            }
        }else if (columnHeader.equalsIgnoreCase("Tax Code")) { // tax for customer or vendor
            columnFound=true;
            if (!StringUtil.isNullOrEmpty(data)) {
                boolean isSales = true;
                if (mode.equalsIgnoreCase("Vendor")) {
                    isSales = false;
                }
//                KwlReturnObject result = importDao.getTaxByCode(companyid, data, isSales);
                String taxID = "";
                Map taxMap = new HashMap<>();
                taxMap.put(Constants.companyKey, companyid);
                taxMap.put(Constants.TAXCODE, data);
                taxMap.put(Constants.TAXTYPE, isSales ? Constants.SALES_TYPE_TAX : Constants.PURCHASE_TYPE_TAX);
                ArrayList taxList = getTax(taxMap);
                if (!StringUtil.isNullOrEmpty((String) taxList.get(1))) {
                    taxID = (String) taxList.get(1);
                } else if (!StringUtil.isNullOrEmpty((String) taxList.get(2))) {
                    String failureMsg = (String) taxList.get(2) + data;
                    throw new DataInvalidateException(failureMsg);
                }
                if (pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("2")) {
                    if (taxID.equals("")) {
                        if (mode.equalsIgnoreCase("Vendor")) {
                            throw new DataInvalidateException("Tax Code " + data + " of Purchase type not found in tax master list for tax dropdown.");
                        } else {
                            throw new DataInvalidateException("Tax Code " + data + " of Sales type not found in tax master list for tax dropdown.");
                        }
                    } else {
                        //dataMap.put("Taxid", taxID);
                        vDataValue=taxID;
                    }
                } else if (pref.equalsIgnoreCase("1")) {
                    //dataMap.put("Taxid", taxID);
                    vDataValue=taxID;
                }
            }
        } else if (columnHeader.equals("Preferred Product(s)") || columnHeader.equals("Preferred Product Code")) {//Preferred Product(s) Preferred Product Code
            columnFound=true;
            if (!StringUtil.isNullOrEmpty(data)) {
                String[] productMapping = data.split(",");
                String notExistProducts = "";
                String existedProductsIDs = "";
                for (int j = 0; j < productMapping.length; j++) {
                    String productCode = productMapping[j];
                    KwlReturnObject returnObject = importDao.getProductByProductCode(companyid, productCode);
                    if (returnObject.getEntityList().isEmpty()) {
                        notExistProducts += notExistProducts.equals("") ? productCode : ", " + productCode;
                    } else {
                        Object[] obj = (Object[]) returnObject.getEntityList().get(0);
                        String productid = (String) obj[0];
                        existedProductsIDs += existedProductsIDs.equals("") ? productid : "," + productid;
                    }
                }
                if (pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("2")) {
                    if (!notExistProducts.equals("")) {//if any preferred product not present in system
                        throw new DataInvalidateException("Preferred Product " + notExistProducts + " not found in master list for Product dropdown.");
                    } else {
                        vDataValue = existedProductsIDs;
                    }
                } else if (pref.equalsIgnoreCase("1")) {
                    vDataValue = existedProductsIDs;
                }
            }
        } else if (columnHeader.equals("Category")) {//Customer or Vendor Category
            columnFound=true;
            //7 is groupid for Customer Category in mastergroup table    
            //8 is groupid for Vendor Category in mastergroup table  
            if ((pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("1")) && !StringUtil.isNullOrEmpty(data)) {// For pref==2 we need to create data if it is not exist so this case handled in aftersave method
                String masterGroupID = mode.equalsIgnoreCase("Customer") ? String.valueOf(7) : String.valueOf(8);
                String[] categoryNames = data.split(",");
                String notExistCategories = "";
                String existCategoryIds = "";
                for (int j = 0; j < categoryNames.length; j++) {
                    String categoryName = categoryNames[j];
                    KwlReturnObject returnObject = importDao.getMasterItem(companyid, categoryName, masterGroupID);
                    if (returnObject.getEntityList().isEmpty()) {
                        notExistCategories += notExistCategories.equals("") ? categoryName : ", " + categoryName;
                    } else {
                        String categoryID = returnObject.getEntityList().get(0).toString();
                        existCategoryIds += existCategoryIds.equals("") ? categoryID : "," + categoryID;
                    }
                }
                if (pref.equalsIgnoreCase("0")) {//most restricted case
                    if (!notExistCategories.equals("")) { //if any category not present in system
                        if (mode.equalsIgnoreCase("Customer")) {
                            throw new DataInvalidateException("Customer Category " + notExistCategories + " not found in master list for Category dropdown. ");
                        } else {
                            throw new DataInvalidateException("Vendor Category " + notExistCategories + " not found in master list for Category dropdown. ");
                        }
                    }
                } else if (pref.equalsIgnoreCase("1")) {
                    vDataValue = existCategoryIds;
                }
            }
        } else if (columnHeader.equals("Sales Person") || columnHeader.equals("Default Sales Person") || columnHeader.equals("Agent")) {
            columnFound=true;
            //7 is groupid for Customer Category in mastergroup table    
            //8 is groupid for Vendor Category in mastergroup table  
            if (!StringUtil.isNullOrEmpty(data)) {
                String masterGroupID = mode.equalsIgnoreCase("Customer") ? String.valueOf(15) : String.valueOf(20);
                String[] spName = data.split(",");
                String notExistSP = "";
                String existedSPIds = "";
                for (int j = 0; j < spName.length; j++) {
                    String salesPerson = spName[j];
                    KwlReturnObject returnObject = importDao.getMasterItem(companyid, salesPerson, masterGroupID);
                    if (returnObject.getEntityList().isEmpty()) {
                        notExistSP += notExistSP.equals("") ? salesPerson : ", " + salesPerson;
                    } else if (returnObject.getEntityList().get(0) != null) {
                        String recId = returnObject.getEntityList().get(0).toString();
                        existedSPIds += existedSPIds.equals("") ? recId : "," + recId;
                    }
                }
                if (pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("2")) {
                    if (!notExistSP.equals("")) {
                        if (mode.equalsIgnoreCase("Customer")) {
                            throw new DataInvalidateException(columnHeader +" "+ notExistSP + " not found in master list for Sales Person dropdown. ");
                        } else {
                            throw new DataInvalidateException("Agent " + notExistSP + " not found in master list for Agent dropdown. ");
                        }
                    } else {
                        if (mode.equalsIgnoreCase("Customer") && columnHeader.equals("Default Sales Person")) {
                            String salesPerson = (String) dataMap.get("SalesPerson");
                            String[] spNameArray = salesPerson.split(",");
                            List list = Arrays.asList(spNameArray);
                            String defaultSalesPerson = (String) dataMap.get("MappingSalesPerson");
                            String[] defaultSPNameArray = defaultSalesPerson.split(",");
                            boolean notPresentInSalesPerson = false;
                            
                            if(defaultSPNameArray.length > 1) {
                                throw new DataInvalidateException("Default Sales Person should not be more than one. ");
                            } else if (!list.contains(defaultSalesPerson)) {
                                notPresentInSalesPerson = true;
                            }
                            if (notPresentInSalesPerson) {
                                throw new DataInvalidateException("Default Sales Person "+ defaultSalesPerson +" not present in Sales person column.");
                            } else {
                                vDataValue = existedSPIds;
                            }
                        }
                        if (mode.equalsIgnoreCase("Customer") && columnHeader.equals("Sales Person")) {
                            vDataValue = existedSPIds;
                        }
                    }
                } else if (pref.equalsIgnoreCase("1")) {
                    vDataValue = existedSPIds;
                }
            }
        } else if (columnHeader.equals("Title")) {//Customer or Vendor Title
            columnFound=true;
            if ((pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("1")) && !StringUtil.isNullOrEmpty(data)) {
                String masterGroupID = String.valueOf(6); //6 is groupid for Title in mastergroup table   
                KwlReturnObject returnObject = importDao.getMasterItem(companyid, data, masterGroupID);
                String titleid = "";
                if (!returnObject.getEntityList().isEmpty() && returnObject.getEntityList().get(0) != null) {
                    titleid = returnObject.getEntityList().get(0).toString();
                }
                if (pref.equalsIgnoreCase("0")) {
                    if (titleid.equals("")) {
                        throw new DataInvalidateException("Title " + data + " not found in master list for Title dropdown. ");
                    } else {
                        vDataValue = titleid;
                    }
                } else if (pref.equalsIgnoreCase("1")) {
                    vDataValue = titleid;
                }
            }
        } 
        map.put("validatedDataValue", vDataValue);
        map.put("columnFound", columnFound);
        return map;
    }
    
    private void updateManualJePostSettingForAccount(String accountID, String usedIn) {
        try {
            if (!StringUtil.isNullOrEmpty(accountID) && !StringUtil.isNullOrEmpty(usedIn)) {
                int res = importDao.updateManualJePostSettingForAccount(accountID, usedIn);
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    public int getMasterValue(String masterString) {
        int masterValue = -1;
        if (masterString.equalsIgnoreCase(Constants.MASTER_TYPE_GENERAL_LEDGER)) {
            masterValue = 1;
        } else if (masterString.equalsIgnoreCase(Constants.MASTER_TYPE_CASH)) {
            masterValue = 2;
        } else if (masterString.equalsIgnoreCase(Constants.MASTER_TYPE_BANK)) {
            masterValue = 3;
        } else if (masterString.equalsIgnoreCase(Constants.MASTER_TYPE_GST) || masterString.equalsIgnoreCase(Constants.MASTER_TYPE_DUTIES_AND_TAXES)) {
            masterValue = 4;
        }
        return masterValue;
    }
    
    public int getIBGBankValue(String ibgBankString) {
        int ibgBankVal = -1;
        if (ibgBankString.equalsIgnoreCase(Constants.DBS_BANK_NAME)) {
            ibgBankVal = Constants.DBS_BANK_Type;
        } else if (ibgBankString.equalsIgnoreCase(Constants.CIBM_BANK_NAME)) {
            ibgBankVal = Constants.CIMB_BANK_Type;
        }
        return ibgBankVal;
    }
    public int getSettlementModeValue(String settlementModeString) {
        int settlementVal = -1;
        if (settlementModeString.equalsIgnoreCase(Constants.Settlement_Mode_Batch_Name)) {
            settlementVal = Constants.Settlement_Mode_Batch;
        } else if (settlementModeString.equalsIgnoreCase(Constants.Settlement_Mode_Real_Time_Name)) {
            settlementVal = Constants.Settlement_Mode_Real_Time;
        }
        return settlementVal;
    }
    public int getPostingIndicatorValue(String postingIndicatorString) {
        int ibgBankVal = -1;
        if (postingIndicatorString.equalsIgnoreCase(Constants.Posting_Indicator_Consolidated_Name)) {
            ibgBankVal = Constants.Posting_Indicator_Consolidated;
        } else if (postingIndicatorString.equalsIgnoreCase(Constants.Posting_Indicator_Individual_Name)) {
            ibgBankVal = Constants.Posting_Indicator_Individual;
        }
        return ibgBankVal;
    }

    public int getAccountTypeValue(String masterString) {
        int masterValue = -1;
        if (masterString.equalsIgnoreCase("Balance Sheet")) {
            masterValue = 1;
        } else if (masterString.equalsIgnoreCase("Profit & Loss")) {
            masterValue = 0;
        }
        return masterValue;
    }

    public Object getDefaultValue(JSONObject columnConfig,Map<String, Object> requestParams) throws ParseException, JSONException, DataInvalidateException, ServiceException {
        Object defaultValue = columnConfig.get("defaultValue");
        if (columnConfig.has("validatetype")) {
            String validatetype = columnConfig.getString("validatetype");
            if (validatetype.equalsIgnoreCase("integer")) {
                defaultValue = StringUtil.isNullOrEmpty(defaultValue.toString()) ? 0 : Integer.parseInt(defaultValue.toString());
            } else if (validatetype.equalsIgnoreCase("double")) {
                defaultValue = StringUtil.isNullOrEmpty(defaultValue.toString()) ? 0.0 : Double.parseDouble(defaultValue.toString());
            } else if (validatetype.equalsIgnoreCase("date")) {
                String ddateStr = defaultValue.toString();
                DateFormat sdf = new SimpleDateFormat(ddateStr.length() > 10 ? df_full : df);
                if (ddateStr.equals("now")) {
                    defaultValue = new Date();
                } else if(ddateStr.equals("bookbeginning")){
                    if(requestParams.containsKey("bookbeginning")){
                        defaultValue=(Date)requestParams.get("bookbeginning");
                    }
                    if(requestParams.containsKey("bookBeginningDate")){
                        defaultValue=(Date)requestParams.get("bookBeginningDate");
                    }
                } else {
                    defaultValue = StringUtil.isNullOrEmpty(ddateStr) ? null : sdf.parse(ddateStr);
                }
            } else if (validatetype.equalsIgnoreCase("boolean")) {
                String data = defaultValue.toString();
                if (data.equalsIgnoreCase("true") || data.equalsIgnoreCase("1") || data.equalsIgnoreCase("T")) {
                    defaultValue = true;
                } else if (data.equalsIgnoreCase("false") || data.equalsIgnoreCase("0") || data.equalsIgnoreCase("F")) {
                    defaultValue = false;
                } else {
                    throw new DataInvalidateException("Incorrect default boolean value for " + columnConfig.getString("columnName") + ".");
                }
            }
        }
        if (defaultValue == null && columnConfig.has("isNotNull") && columnConfig.getBoolean("isNotNull")) {
            throw new DataInvalidateException("cannot set default empty data for " + columnConfig.getString("columnName") + ".");
        }
        return defaultValue;
    }

    public List getRefData(HashMap<String, Object> requestParams, String table, String dataColumn, String fetchColumn, String comboConfigid, Object token) throws ServiceException, DataInvalidateException {
        ArrayList<String> filterNames = new ArrayList<String>();
        ArrayList<Object> filterValues = new ArrayList<Object>();
        filterNames.add(dataColumn);
        filterValues.add(token);
        
        return importDao.getRefModuleData(requestParams, table, fetchColumn, comboConfigid, filterNames, filterValues);
    }
    
    
    public KwlReturnObject getTaxbyIDorName(String companyId,String fetchColumnName,String conditionColumnName, String  data) throws ServiceException {
        return importDao.getTaxbyIDorName( companyId, fetchColumnName, conditionColumnName,data);
    }    
    public List getRefDataWithPrimaryKey(HashMap<String, Object> requestParams, String table, String dataColumn, String fetchColumn, String comboConfigid, Object token) throws ServiceException, DataInvalidateException {
        ArrayList<String> filterNames = new ArrayList<String>();
        ArrayList<Object> filterValues = new ArrayList<Object>();
        filterNames.add(dataColumn);
        filterValues.add(token);
        String moduleName="";
        if(requestParams.containsKey("moduleName")){
            moduleName=(String)requestParams.get("moduleName");
        }

        Set<String> modules = new HashSet<>();
        modules.add("Product");
        modules.add("Assembly Product");
        modules.add("Unit of Measure");
        modules.add(Constants.Acc_Vendor_modulename);
        modules.add(Constants.Acc_Customer_modulename);
        modules.add("Accounts");
        modules.add("Group");
        modules.add("Opening Sales Invoice");
        modules.add("Opening Purchase Invoice");
        modules.add("Opening Receipt");
        modules.add("Opening Payment");
        modules.add("Opening Customer Credit Note");
        modules.add("Opening Vendor Credit Note");
        modules.add("Opening Customer Debit Note");
        modules.add("Opening Vendor Debit Note");
        modules.add("Quotation");
        modules.add("Customer Invoices");
        modules.add("Cash Sales");
        modules.add("Vendor Invoice");
        modules.add("Sales Order");
        modules.add("Credit Note");
        modules.add("Payment");
        modules.add("Receipt");
        modules.add("Purchase Order");
        modules.add("Cash Purchase");
        modules.add("Delivery Order");
        
        if (modules.contains(moduleName)) {
            filterNames.add("company.companyID");
            filterValues.add(requestParams.get("companyid").toString());
        }

        if (requestParams.get("moduleName").equals("Product") && dataColumn.equalsIgnoreCase("productid")) { // this check added due to while checking unique productid it should not check deleted entry and asset entry
            filterNames.add("deleted");
            filterValues.add(false);
            if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null) {// Since moduleName for Asset group came as Product so checking moduleid for differention
                int moduleID = (int) requestParams.get("moduleid");
                if (moduleID == Constants.Acc_Product_Master_ModuleId) {
                    filterNames.add("asset");
                    filterValues.add(false);
                } else if (moduleID == Constants.Acc_FixedAssets_AssetsGroups_ModuleId) {
                    filterNames.add("asset");
                    filterValues.add(true);
                }
            }
        }

        return importDao.getRefModuleDataWithPrimaryKeyValue(requestParams, table, fetchColumn, comboConfigid, filterNames, filterValues);
    }

    public List getCustomComboID(String combovalue, String fieldid, String fetchColumn) throws ServiceException, DataInvalidateException {
        ArrayList filterNames = new ArrayList<String>();
        ArrayList filterValues = new ArrayList<Object>();
        filterNames.add("value");
        filterValues.add(combovalue);
        filterNames.add("fieldid");
        filterValues.add(fieldid);
        return importDao.getCustomComboID(fetchColumn, filterNames, filterValues);
    }

      public List getCustomComboValue(String combovalue, String fieldid, String fetchColumn) throws ServiceException, DataInvalidateException {
        ArrayList filterNames = new ArrayList<String>();
        ArrayList filterValues = new ArrayList<Object>();
        filterNames.add("id");
        filterValues.add(combovalue);
        filterNames.add("fieldid");
        filterValues.add(fieldid);
        return importDao.getCustomComboID(fetchColumn, filterNames, filterValues);
    }
      
    public String chkNullorEmptywithDatatruncation(String cc, int dataTruncation) {
        String ret = "";
        if (!StringUtil.isNullOrEmpty(cc)) {
            if (cc.length() > dataTruncation && dataTruncation != 0) {
                cc = cc.substring(0, dataTruncation);
            }
            ret = cc.trim();
        }
        return ret;
    }

    //Function used for XLS preview grid
    public JSONObject parseXLS(String filename, int sheetNo) throws FileNotFoundException, IOException, JSONException {
        JSONObject jobj = new JSONObject();
        FileInputStream fs = new FileInputStream(filename);
        
        int startRow = 0;
        int maxRow = 0;
        int maxCol = 0;
        int noOfRowsDisplayforSample = 20;
        JSONArray jArr = new JSONArray();
        try {
            DateFormat sdf = new SimpleDateFormat(df);
            Workbook wb = WorkbookFactory.create(fs); // It will create Workbook according to given excel file version like xls/xlsx
    //        HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
            Sheet sheet = wb.getSheetAt(sheetNo);
            DecimalFormat dfmt = new DecimalFormat("#.#####");
            startRow = 0;
            maxRow = sheet.getLastRowNum();
            maxCol = 0;
            if (noOfRowsDisplayforSample > sheet.getLastRowNum()) {
                noOfRowsDisplayforSample = sheet.getLastRowNum();
            }
            for (int i = 0; i <= noOfRowsDisplayforSample; i++) {
                Row row = sheet.getRow(i);
                JSONObject obj = new JSONObject();
                JSONObject jtemp1 = new JSONObject();
                if (row == null) {
                    continue;
                }
                if (i == 0) {
                    maxCol = row.getLastCellNum();
                }
                for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                    Cell cell = row.getCell(cellcount);
                    CellReference cref = new CellReference(i, cellcount);
                    String colHeader = cref.getCellRefParts()[2];
                    String val = null;

                    if (cell != null) {
                        val = getCellValue(cell, sdf);
                    }

                    if (i == 0) { // List of Headers (Consider first row as Headers)
                        if (val != null) {
                            jtemp1 = new JSONObject();
                            jtemp1.put("header", val == null ? "" : val);
                            jtemp1.put("index", cellcount);
                            jobj.append("Header", jtemp1);
                        }
                    }
                    obj.put(colHeader, val);
                }
//                    if(obj.length()>0){ //Don't show blank row in preview grid[SK]
                jArr.put(obj);
//                    }
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        jobj.put("startrow", startRow);
        jobj.put("maxrow", maxRow);
        jobj.put("maxcol", maxCol);
        jobj.put("index", sheetNo);
        jobj.put("data", jArr);
        jobj.put("filename", filename);

        jobj.put("msg", "XLS has been successfully uploaded");
        jobj.put("lsuccess", true);
        jobj.put("valid", true);
        return jobj;
    }
    
    public JSONObject parseXLSX(String filename, int sheetNo) throws FileNotFoundException, IOException, JSONException {
        JSONObject jobj = new JSONObject();
        XSSFWorkbook wb = new XSSFWorkbook(filename);
//        XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(wb);
        XSSFSheet sheet = wb.getSheetAt(sheetNo);
        DecimalFormat dfmt = new DecimalFormat("#.#####");
        int startRow = 0;
        int maxRow = sheet.getLastRowNum();
        int maxCol = 0;
        int noOfRowsDisplayforSample = 20;
        if (noOfRowsDisplayforSample > sheet.getLastRowNum()) {
            noOfRowsDisplayforSample = sheet.getLastRowNum();
        }

        JSONArray jArr = new JSONArray();
        try {
            for (int i = 0; i <= noOfRowsDisplayforSample; i++) {
                XSSFRow row = sheet.getRow(i);
                JSONObject obj = new JSONObject();
                JSONObject jtemp1 = new JSONObject();
                if (row == null) {
                    continue;
                }
                if (i == 0) {
                    maxCol = row.getLastCellNum();
                }
                for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                    XSSFCell cell = row.getCell(cellcount);
                    CellReference cref = new CellReference(i, cellcount);
                    String colHeader = cref.getCellRefParts()[2];
                    String val = null;

                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case XSSFCell.CELL_TYPE_NUMERIC:
                                val = Double.toString(cell.getNumericCellValue());
                                val = dfmt.format(cell.getNumericCellValue());
                                break;
                            case XSSFCell.CELL_TYPE_STRING:
                                val = cell.getRichStringCellValue().getString();
                                break;
                    }
                    }

                    if (i == 0) { // List of Headers (Consider first row as Headers)
                        if (val != null) {
                            jtemp1 = new JSONObject();
                            jtemp1.put("header", val == null ? "" : val);
                            jtemp1.put("index", cellcount);
                            jobj.append("Header", jtemp1);
                        }
                    }
                    obj.put(colHeader, val);
                }
//                    if(obj.length()>0){ //Don't show blank row in preview grid[SK]
                jArr.put(obj);
//                    }
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        jobj.put("startrow", startRow);
        jobj.put("maxrow", maxRow);
        jobj.put("maxcol", maxCol);
        jobj.put("index", sheetNo);
        jobj.put("data", jArr);
        jobj.put("filename", filename);

        jobj.put("msg", "XLSX has been successfully uploaded");
        jobj.put("lsuccess", true);
        jobj.put("valid", true);
        return jobj;
    }

    public JSONObject parseXLS1(String filename, int sheetNo, int startindex) throws FileNotFoundException, IOException, JSONException {
        JSONObject jobj = new JSONObject();
        
        ArrayList<String> arr = new ArrayList<String>();
        int startRow = 0;
        int maxRow = 0;
        int maxCol = 0;
        DateFormat dateFormat = new SimpleDateFormat(df);
        JSONArray jArr = new JSONArray();
        try {
            FileInputStream fs = new FileInputStream(filename);
            Workbook wb = WorkbookFactory.create(fs);// It will create Workbook according to given excel file version like xls/xlsx
            Sheet sheet = wb.getSheetAt(sheetNo);
            maxRow = sheet.getLastRowNum();
            for (int i = startindex; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                JSONObject obj = new JSONObject();
                JSONObject jtemp1 = new JSONObject();
                if (row == null) {
                    continue;
                }
                if (i == startindex) {
                    maxCol = row.getLastCellNum();
                }
                for (int j = 0; j < maxCol; j++) {
                    Cell cell = row.getCell(j);
                    String val = null;
                    if (cell == null) {
                        arr.add(val);
                        continue;
                    }
                    String colHeader = new CellReference(i, j).getCellRefParts()[2];
                    val = getCellValue(cell, dateFormat);
                    if (i == startindex) { // List of Headers (consider startindex row as a headers)
                        if (val != null) {
                            jtemp1 = new JSONObject();
                            jtemp1.put("header", val);
                            jtemp1.put("index", j);
                            jobj.append("Header", jtemp1);
                            obj.put(colHeader, val);
                        }
                        arr.add(val);
                    } else {
                        if (arr.get(j) != null) {
                            obj.put(arr.get(j), val);
                        }
                    }

                }
                if (obj.length() > 0) {
                    jArr.put(obj);
                }

            }
        } catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        jobj.put("startrow", startRow);
        jobj.put("maxrow", maxRow);
        jobj.put("maxcol", maxCol);
        jobj.put("index", sheetNo);
        jobj.put("data", jArr);
        jobj.put("filename", filename);

        jobj.put("msg", "XLS has been successfully uploaded");
        jobj.put("lsuccess", true);
        jobj.put("valid", true);
        return jobj;
    }

    public JSONObject validateFileData(HashMap<String, Object> requestParams) {
        int importLimitTemp = importLimit;
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        boolean isAssetsImportFromDoc = true;
        boolean isRecordFailed = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;

        int total = 0, failed = 0, fileSize = 0;
        String fileName = "", extn = "";
        Modules module = null;
        String exceededLimit = "no", channelName = "";
        try {
            String mode = (String) requestParams.get("modName");
            if(!StringUtil.isNullOrEmpty(mode) && (Constants.IMPORT_TIME_10000_MOD_LIST.contains(mode))){
                importLimitTemp=importLimit_10000;
            }
             boolean withoutBOM = false;
            if(requestParams.containsKey("withoutBOM") && !StringUtil.isNullOrEmpty(requestParams.get("withoutBOM").toString())){   //SDP-6182
                withoutBOM = Boolean.parseBoolean(requestParams.get("withoutBOM").toString());
            }
            
            boolean isBomlessFile = false;
            if(requestParams.containsKey("isBomlessFile") && !StringUtil.isNullOrEmpty(requestParams.get("isBomlessFile").toString())){   //SDP-6182
                isBomlessFile = Boolean.parseBoolean(requestParams.get("isBomlessFile").toString());
            }
            int subModuleFlag = 0;
            if (requestParams.containsKey("subModuleFlag") && !StringUtil.isNullOrEmpty(requestParams.get("subModuleFlag").toString())) {
                subModuleFlag = Integer.parseInt((String) requestParams.get("subModuleFlag"));
            }
            boolean updateExistingRecordFlag=false;
            if(requestParams.containsKey("updateExistingRecordFlag") && requestParams.get("updateExistingRecordFlag")!=null && !StringUtil.isNullOrEmpty(requestParams.get("updateExistingRecordFlag").toString())){   //SDP-6182
                updateExistingRecordFlag = (Boolean)requestParams.get("updateExistingRecordFlag");
            }
            if(requestParams.containsKey("isAssetsImportFromDoc") && requestParams.get("isAssetsImportFromDoc")!=null && !StringUtil.isNullOrEmpty(requestParams.get("isAssetsImportFromDoc").toString())){  
                isAssetsImportFromDoc = (Boolean)requestParams.get("isAssetsImportFromDoc");
            }
            boolean isActivateMRPModule=false;
            if(requestParams.containsKey("isActivateMRPModule") && requestParams.get("isActivateMRPModule")!=null && !StringUtil.isNullOrEmpty(requestParams.get("isActivateMRPModule").toString())){
                isActivateMRPModule = (Boolean)requestParams.get("isActivateMRPModule");
            }
            fileName = (String) requestParams.get("filename");
            if (mode.equalsIgnoreCase("Cash Sales")) {
                mode = "Customer Invoices";
            } else if (mode.equalsIgnoreCase("Cash Purchase")) {
                mode = "Vendor Invoice";
            }
            boolean fetchCustomFields = false;
            String isdocumentimport = "F";
            if (!StringUtil.isNullOrEmpty((String) requestParams.get("fetchCustomFields"))) {
                fetchCustomFields = Boolean.parseBoolean((String) requestParams.get("fetchCustomFields"));
                if (mode.equalsIgnoreCase("Customer Invoices") || mode.equalsIgnoreCase("Sales Order") || mode.equalsIgnoreCase("Vendor Invoice") || mode.equalsIgnoreCase("Quotation") || mode.equalsIgnoreCase("Purchase Order") || mode.equalsIgnoreCase("Vendor Quotation") || mode.equalsIgnoreCase("Delivery Order") || mode.equalsIgnoreCase("GoodsReceiptOrder") || mode.equalsIgnoreCase("Convert Sales Invoice in to Cash Sales")||mode.equalsIgnoreCase("Credit Note")||mode.equalsIgnoreCase("Receipt")||mode.equalsIgnoreCase("Payment")||mode.equalsIgnoreCase("GSTTerm")) {
                    isdocumentimport = "T";
                }
            }
            if (mode.equalsIgnoreCase("Accounts")) {
                isdocumentimport = "F";
            }
            extn = fileName.substring(fileName.lastIndexOf(".") + 1);
            channelName = "/ValidateFile/" + fileName;

            Object extraObj = requestParams.get("extraObj");
            JSONObject extraParams = (JSONObject) requestParams.get("extraParams");

            String jsondata = (String) requestParams.get("resjson");
            jsondata = jsondata.replaceAll("\\n", "").trim();
            JSONObject rootcsvjobj = new JSONObject(jsondata);
            JSONArray mapping = rootcsvjobj.getJSONArray("root");

            String dateFormat = null, dateFormatId = (String) requestParams.get("dateFormat");
            if ((extn.equalsIgnoreCase("csv") || extn.equalsIgnoreCase("xls")) && !StringUtil.isNullOrEmpty(dateFormatId)) {
                KWLDateFormat kdf = (KWLDateFormat) KwlCommonTablesDAOObj.getClassObject(KWLDateFormat.class.getName(), dateFormatId);
                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }

            String classPath = "", primaryKey = "", uniqueKeyMethodName = "", uniqueKeyHbmName = "";
            try {
                List list = importDao.getModuleObject(mode);
                module = (Modules) list.get(0); //Will throw null pointer if no module entry found
            } catch (Exception ex) {
                throw new DataInvalidateException("Column config not available for module " + mode);
            }

            try {
                classPath = module.getPojoClassPathFull().toString();
                primaryKey = module.getPrimaryKey_MethodName().toString();
            } catch (Exception ex) {
                throw new DataInvalidateException("Please set proper properties for module " + mode);
            }
            uniqueKeyMethodName = module.getUniqueKey_MethodName();
            uniqueKeyHbmName = module.getUniqueKey_HbmName();
            HashMap<String, Object> params = new HashMap<String, Object> ();
            params.put("moduleId", module.getId());
            params.put("companyid", (String) requestParams.get("companyid"));
            params.put("isdocumentimport", isdocumentimport);
            params.put("subModuleFlag", new Integer(subModuleFlag));
            params.put("isBomlessFile", isBomlessFile);
            params.put("updateExistingRecordFlag", updateExistingRecordFlag);
            params.put("isExpenseInvoiceImport", requestParams.containsKey("isExpenseInvoiceImport") ? (Boolean) requestParams.get("isExpenseInvoiceImport") : false);
            params.put("incash", requestParams.containsKey("incash") ? (Boolean) requestParams.get("incash") : false);
            JSONArray columnConfig = getModuleColumnConfig(params);
            if (fetchCustomFields) {
                JSONArray CustomDataJArr = getCustomModuleColumnConfig(module.getId(), (String) requestParams.get("companyid"), false,params);
                for (int i = 0; i < CustomDataJArr.length(); i++) {
                    JSONObject jSONObject = CustomDataJArr.getJSONObject(i);
                    String clmName = jSONObject.getString("columnName");
                    for (int k = 0; k < mapping.length(); k++) {
                        JSONObject mappingJson = mapping.getJSONObject(k);
                        String datakey = mappingJson.getString("columnname");
                        boolean customflag = (!StringUtil.isNullOrEmpty(mappingJson.optString("customflag"))) ? Boolean.parseBoolean(mappingJson.optString("customflag")) : false;
                        if (customflag && clmName.equalsIgnoreCase(datakey)) {
                            columnConfig.put(jSONObject);
                        }
                    }
                }
            }
            Map rowRefDataMap = new HashMap();
            for(int i=0 ; i<columnConfig.length() ; i++){
                JSONObject ccObject = columnConfig.getJSONObject(i);
                if("ref".equalsIgnoreCase(ccObject.optString("validatetype", null))){
                    rowRefDataMap.put(ccObject.optString("pojoName", ""), new HashMap());
                    }
                }
            String tableName = importDao.getTableName(fileName);
            KwlReturnObject kresult = importDao.getFileData(tableName, new HashMap<String, Object>());
            List fileDataList = kresult.getEntityList();
            Iterator itr = fileDataList.iterator();

            importDao.markRecordValidation(tableName, -1, 1, "", ""); //reset all invalidation
            JSONArray recordJArr = new JSONArray(), columnsJArr = new JSONArray(), DataJArr = new JSONArray(),validFileDataArr=new JSONArray();
            if (itr.hasNext()) { //this if block create an array of column data of file
                Object[] fileData = (Object[]) itr.next();
                JSONObject jtemp = new JSONObject();
                jtemp.put("header", "Row No.");
                jtemp.put("dataIndex", "col0");
                jtemp.put("width", 50);
                columnsJArr.put(jtemp);

                for (int i = 1; i < fileData.length - 3; i++) {    //Discard columns, id at index 0 and isvalid,validationlog at last 2.
                    jtemp = new JSONObject();
                    jtemp.put("header", fileData[i] == null ? "" : fileData[i].toString());
                    jtemp.put("dataIndex", "col" + i);
                    columnsJArr.put(jtemp);
                }

                jtemp = new JSONObject();
                jtemp.put("header", "Validation Log");
//                jtemp.put("hidden", true);
                jtemp.put("dataIndex", "validateLog");
                columnsJArr.put(jtemp);


                //Create record Obj for grid's store
                for (int i = 0; i < fileData.length - 1; i++) {
                    jtemp = new JSONObject();
                    jtemp.put("name", "col" + i);
                    recordJArr.put(jtemp);
                }
                jtemp = new JSONObject();
                jtemp.put("name", "validateLog");
                recordJArr.put(jtemp);
            }

            try {
                jobj.put("record", recordJArr);
                jobj.put("columns", columnsJArr);
                jobj.put("data", DataJArr);
                jobj.put("validdatajson", validFileDataArr);
                jobj.put("count", failed);
                jobj.put("valid", 0);
                jobj.put("totalrecords", total);
                jobj.put("isHeader", true);
                jobj.put("finishedValidation", false);
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) requestParams.get("servletContext"));
            } catch (Exception ex) {
                throw ex;
            }

            fileSize = fileDataList.size() - 1;
            fileSize = fileSize >= importLimitTemp ? importLimitTemp : fileSize; // fileSize used for showing progress bar[Client Side]

            jobj.put("isHeader", false);
            int recIndex = 0;
            HashSet <String> assemblyset = new HashSet <String>();   //For Assembly Product Validation
            
            Set<String> modulePrimaryKeyValues = new HashSet<>();
            Set<String> checkNumberDuplicate = new HashSet<>();
            Set<String> paymentNumbersSet = new HashSet<>();
            Set<String> modulePrimaryKeyValueSet = new HashSet<>();
            Set<String> globalDatakeySet = new HashSet<>();
            while (itr.hasNext()) {
                Object[] fileData = (Object[]) itr.next();
                recIndex = (Integer) fileData[0];
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                HashMap<String, Object> columnHeaderMap = new HashMap<String, Object>();
                HashMap<String, Object> columnCSVindexMap = new HashMap<String, Object>();
                Map<Integer, Object> invalidColumn = new HashMap<>();
                JSONArray customfield = new JSONArray();
                isRecordFailed=false;
                for (int k = 0; k < mapping.length(); k++) {
                    JSONObject mappingJson = mapping.getJSONObject(k);
                    String datakey = mappingJson.getString("columnname");
                    Object dataValue = cleanHTML((String) fileData[mappingJson.getInt("csvindex") + 1]); //+1 for id column at index-0
                    dataMap.put(datakey, dataValue);
                    columnHeaderMap.put(datakey, mappingJson.getString("csvheader"));
                    columnCSVindexMap.put(datakey, mappingJson.getInt("csvindex") + 1);
                }
               /* Added for finding index of invalid column in checkUniqueRecord function*/
                columnCSVindexMap.put("uniqueKeyMethodName", uniqueKeyMethodName);
                columnCSVindexMap.put("uniqueKeyHbmName", uniqueKeyHbmName);
                
                if (mode.equalsIgnoreCase("Payment")) {
                    requestParams.put("checkNumberDuplicate", checkNumberDuplicate);
                    requestParams.put("paymentNumbersSet", paymentNumbersSet);
                }
                for (int j = 0; j < extraParams.length(); j++) {
                    String datakey = (String) extraParams.names().get(j);
                    Object dataValue = extraParams.get(datakey);
                    dataMap.put(datakey, dataValue);
                }

                try {
                    if (total >= importLimitTemp) {
                        exceededLimit = "yes";
                        break;
                    }
                    //Update processing status at client side
                    if (total > 0 && total % 10 == 0) {
                        try {
                            ServerEventManager.publish(channelName, "{parsedCount:" + total + ",invalidCount:" + failed + ", fileSize:" + fileSize + ", finishedValidation:false}", (ServletContext) requestParams.get("servletContext"));
                        } catch (Exception ex) {
                            throw ex;
                        }
                    }
                    if (mode.equalsIgnoreCase(Constants.ASSEMBLY_PRODUCT_MODULE_NAME)) {
                        Locale locale = new Locale("en", "US");   //Default value English
                        if (requestParams.containsKey("locale") && requestParams.get("locale") != null) {
                            locale = (Locale)requestParams.get("locale");
                        }
                        if(!withoutBOM){
                            validateAssemblyProduct(assemblyset, dataMap, locale, isActivateMRPModule); //This method used to validate Assembly Product related records
			}
                    }
                    
                    CheckUniqueRecord(requestParams, dataMap, invalidColumn,classPath, uniqueKeyMethodName, uniqueKeyHbmName, primaryKey, columnHeaderMap,columnCSVindexMap, modulePrimaryKeyValues);
                    if(!updateExistingRecordFlag){
                        CheckGlobalLevelData(requestParams, columnConfig, dataMap, uniqueKeyMethodName, globalDatakeySet, modulePrimaryKeyValueSet);
                    }
                    //related to mapping
                    validateDataMAP2(requestParams, dataMap,invalidColumn, columnConfig, customfield, columnHeaderMap, columnCSVindexMap, dateFormat, rowRefDataMap);   
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

                    importDao.markRecordValidation(tableName, recIndex, 0, errorMsg, invalidColumns);
                    JSONObject jtemp = new JSONObject();
                    for (int i = 0; i < fileData.length - 2; i++) {
                        jtemp.put("col" + i, fileData[i] == null ? "" : fileData[i].toString());
                    }
                    jtemp.put("invalidcolumns", invalidColumns);
                    jtemp.put("validateLog", errorMsg);
                    DataJArr.put(jtemp);

//                    try {
//                        jtemp.put("count", failed);
//                        jtemp.put("totalrecords", total+1);
//                        jtemp.put("fileSize", fileSize);
//                        jtemp.put("finishedValidation", false);
//                        ServerEventManager.publish(channelName, jtemp.toString(), (ServletContext) requestParams.get("servletContext"));
//                    } catch(Exception dex) {
//                        throw dex;
//                    }
                }
                total++;
                if (isAssetsImportFromDoc && !isRecordFailed) {
                    DateFormat df = authHandler.getDateOnlyFormat();
                    dataMap.put(Constants.df, df);
                    prepareAssetDetailsJsonData(validFileDataArr, dataMap);
                }
            }

            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
                msg = "All the records are invalid.";
            } else if (success == total) {
                msg = "All the records are valid.";
            } else {
                msg = "" + success + " valid record" + (success > 1 ? "s" : "") + "";
                msg += (failed == 0 ? "." : " and " + failed + " invalid record" + (failed > 1 ? "s" : "") + ".");
            }

            jobj.put("record", recordJArr);
            jobj.put("columns", columnsJArr);
            jobj.put("data", DataJArr);
            jobj.put("count", failed);
            jobj.put("valid", success);
            jobj.put("totalrecords", total);
            jobj.put("filename", fileName);
            if (isAssetsImportFromDoc) {
                JSONObject fobj = new JSONObject();
                fobj.put(Constants.RES_data, validFileDataArr);
                jobj.put("validdatajson", fobj);
            }

            try {
                ServerEventManager.publish(channelName, "{parsedCount:" + total + ",invalidCount:" + failed + ", fileSize:" + fileSize + ", finishedValidation:true}", (ServletContext) requestParams.get("servletContext"));
            } catch (Exception ex) {
                throw ex;
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception e) {
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + e.getMessage();
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("exceededLimit", exceededLimit);
            } catch (JSONException ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    private int getNatureValue(String datakey, Object dataValue) {
        int natureValue = -1;
        if (dataValue.toString().equalsIgnoreCase("Liability")) {
            natureValue = 0;
        } else if (dataValue.toString().equalsIgnoreCase("Asset")) {
            natureValue = 1;
        } else if (dataValue.toString().equalsIgnoreCase("Expenses")) {
            natureValue = 2;
        } else if (dataValue.toString().equalsIgnoreCase("Income")) {
            natureValue = 3;
        }
        return natureValue;
    }
    private String getPANStatus(String datakey, Object dataValue) {
        String PANStatus = "-1";
//        if (dataValue.toString().equalsIgnoreCase("Available")) {
//            PANStatus = "";
//        } else 
        if (dataValue.toString().equalsIgnoreCase(IndiaComplianceConstants.PAN_NOT_AVAILABLE)) {
            PANStatus = "2";
        } else if (dataValue.toString().equalsIgnoreCase(IndiaComplianceConstants.PAN_APPLIED_FOR)) {
            PANStatus = "3";
        }
        return PANStatus;
    }
    private String getManufacturerStatus(String datakey, Object dataValue) {
        String ManufacturerStatus = "-1";
        if (dataValue.toString().equalsIgnoreCase("Regular")) {
            ManufacturerStatus = "1";
        } else if (dataValue.toString().equalsIgnoreCase("Small Scale Industries(SSI)")) {
            ManufacturerStatus = "2";
        } 
        return ManufacturerStatus;
    }
    
    private int getValuationMethodValue(String datakey, Object dataValue) {
        int valmethodValue = -1;
        if (dataValue.toString().equalsIgnoreCase("LIFO")) {
            valmethodValue = 0;
        } else if (dataValue.toString().equalsIgnoreCase("FIFO")) {
            valmethodValue = 1;
        } else if (dataValue.toString().equalsIgnoreCase("Moving Average")) {
            valmethodValue = 2;
        } 
        return valmethodValue;
    }    
    private boolean isTINvalid(Object tinNumber) {
        boolean isTINmatch = false;
        if (tinNumber!=null) {
            Pattern p = Pattern.compile("(\\d){10}[a-z A-Z]|(\\d){11}");
            Matcher m = p.matcher(tinNumber.toString());
            isTINmatch = m.matches();
        }
        return isTINmatch;
    }    
    private boolean isPANvalid(Object tinNumber) {
        boolean isPANvalid = false;
        if (tinNumber!=null) { 
            Pattern p = Pattern.compile("[A-Z]{5}(\\d){4}[A-Z]");
            Matcher m = p.matcher(tinNumber.toString());
            isPANvalid = m.matches();
        }
        return isPANvalid;
    }    

    public void prepareAssetDetailsJsonData(JSONArray validFileDataArr, HashMap<String, Object> dataMap) throws JSONException, ParseException,IOException {

        
        
        DateFormat df = null;
        if (dataMap.containsKey(Constants.df) && dataMap.get(Constants.df) != null) {
            df = (DateFormat) dataMap.get(Constants.df);
        }

        /* 
         Put Asset Detail  information in JSON Object
         */
        JSONObject tempJSONObject = new JSONObject();
        if (dataMap.containsKey("AssetId") && dataMap.get("AssetId") != null) {
            tempJSONObject.put("assetId", dataMap.get("AssetId"));
        }
        if (dataMap.containsKey("Description") && dataMap.get("Description") != null) {
            tempJSONObject.put("assetdescription", dataMap.get("Description"));
        }
        if (dataMap.containsKey("Department") && dataMap.get("Department") != null) {
            tempJSONObject.put("department", dataMap.get("Department"));
        }
        if (dataMap.containsKey("PersonUsingtheAsset") && dataMap.get("PersonUsingtheAsset") != null) {
            tempJSONObject.put("assetUser", dataMap.get("PersonUsingtheAsset"));
        }
        if (dataMap.containsKey("Cost") && dataMap.get("Cost") != null) {
            tempJSONObject.put("cost", dataMap.get("Cost"));
            tempJSONObject.put("costInForeignCurrency", dataMap.get("Cost"));
        }
        if (dataMap.containsKey("purchaseDate") && dataMap.get("purchaseDate") != null) {
            tempJSONObject.put("purchaseDate", df.format((Date) dataMap.get("purchaseDate")));
        }
        if (dataMap.containsKey("DateOfInstallation") && dataMap.get("DateOfInstallation") != null) {
            tempJSONObject.put("installationDate", df.format((Date) dataMap.get("DateOfInstallation")));
        }
        if (dataMap.containsKey("AssetLifeYear") && dataMap.get("AssetLifeYear") != null) {
            tempJSONObject.put("assetLife", dataMap.get("AssetLifeYear"));
        }
        if (dataMap.containsKey("Salvage Rate") && dataMap.get("Salvage Rate") != null) {
            tempJSONObject.put("salvageRate", dataMap.get("Salvage Rate"));
        }
        if (dataMap.containsKey("SalvageValue") && dataMap.get("SalvageValue") != null) {
            tempJSONObject.put("salvageValue", dataMap.get("SalvageValue"));
            tempJSONObject.put("salvageValueInForeignCurrency", dataMap.get("SalvageValue"));
        }
        if (dataMap.containsKey("NominalValue") && dataMap.get("NominalValue") != null) {
            tempJSONObject.put("nominalValue", dataMap.get("NominalValue"));
        }
        if (dataMap.containsKey("ElapsedLifeYear") && dataMap.get("ElapsedLifeYear") != null) {
            tempJSONObject.put("elapsedLife", dataMap.get("ElapsedLifeYear"));
        }
        tempJSONObject.put("accumulatedDepreciation", "0");
        
        /*
        Put Warehouse,Location,Batch,Serial,Row,Rack,Bin related details
        */
        JSONObject obj = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        obj.put("id", "");
        if (dataMap.containsKey("Warehouse") && dataMap.get("Warehouse") != null) {
            obj.put("warehouse", dataMap.get("Warehouse"));
        }
        if (dataMap.containsKey("Locations") && dataMap.get("Locations") != null) {
            obj.put("location", dataMap.get("Locations"));
        }
        if (dataMap.containsKey("Batch") && dataMap.get("Batch") != null) {
            obj.put("batchname", dataMap.get("Batch"));
             obj.put("batch", dataMap.get("Batch"));
        }
        if (dataMap.containsKey("SerialNo") && dataMap.get("SerialNo") != null) {
            obj.put("serialno", dataMap.get("SerialNo"));
        }
        if (dataMap.containsKey("Row") && dataMap.get("Row") != null) {
            obj.put("row", dataMap.get("Row"));
        }
        if (dataMap.containsKey("Rack") && dataMap.get("Rack") != null) {
            obj.put("rack", dataMap.get("Rack"));
        }
        if (dataMap.containsKey("Bin") && dataMap.get("Bin") != null) {
            obj.put("bin", dataMap.get("Bin"));
        }
        if (dataMap.containsKey("MfgDate") && dataMap.get("MfgDate") != null) {
            obj.put("mfgdate", df.format((Date) dataMap.get("MfgDate")));
        }
        if (dataMap.containsKey("ExpDate") && dataMap.get("ExpDate") != null) {
            obj.put("expdate", df.format((Date) dataMap.get("ExpDate")));
        }
        if (dataMap.containsKey("WarrantyExpFromDate") && dataMap.get("WarrantyExpFromDate") != null) {
            obj.put("expstart", df.format((Date) dataMap.get("WarrantyExpFromDate")));
        }
        if (dataMap.containsKey("WarrantyExpToDate") && dataMap.get("WarrantyExpToDate") != null) {
            obj.put("expend", df.format((Date) dataMap.get("WarrantyExpToDate")));
        }
         obj.put("quantity", "1");
        jSONArray.put(obj);
        tempJSONObject.put("batchdetails", jSONArray.toString());
        validFileDataArr.put(tempJSONObject);
       
    }

    public JSONObject importFileData(HashMap<String, Object> requestParams) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        Locale locale = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        Boolean isBomlessFile = false;

        int total = 0, failed = 0;
        String fileName = "", tableName = "", extn = "",failureFileType="";
        Modules module = null;
        if(requestParams.containsKey("locale")){
        locale = (Locale)requestParams.get("locale");
        }
        if (requestParams.containsKey("isBomlessFile") && requestParams.get("isBomlessFile")!=null) {
            isBomlessFile = (Boolean) requestParams.get("isBomlessFile");
        }
        try {
            boolean isImportingGroup = false;
            String mode = (String) requestParams.get("modName");
            int groupdispOrder = 0;
            if (mode.equals("Group")) {
                isImportingGroup = true;

                KwlReturnObject dspresult = importDao.getMaxDisplayOrderOfGroup();
                List dispOrderlist = dspresult.getEntityList();
                if (!dispOrderlist.isEmpty() && dispOrderlist.get(0) != null) {
                    groupdispOrder = (Integer) dispOrderlist.get(0);
                }
            }
            fileName = (String) requestParams.get("filename");
            extn = fileName.substring(fileName.lastIndexOf(".") + 1);
            StringBuilder failedRecords = new StringBuilder();

            String dateFormat = null, dateFormatId = (String) requestParams.get("dateFormat");
            if (extn.equalsIgnoreCase("csv") && !StringUtil.isNullOrEmpty(dateFormatId)) {
                KWLDateFormat kdf = (KWLDateFormat) KwlCommonTablesDAOObj.getClassObject(KWLDateFormat.class.getName(), dateFormatId);
                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }

            boolean updateExistingRecordFlag = false;

            if (requestParams.containsKey("updateExistingRecordFlag") && requestParams.get("updateExistingRecordFlag") != null) {
                updateExistingRecordFlag = (Boolean) requestParams.get("updateExistingRecordFlag");
            }

            String moduleId = "";

            if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null) {
                moduleId = requestParams.get("moduleid").toString();
            }

            boolean fetchCustomFields = false;
            if (!StringUtil.isNullOrEmpty((String) requestParams.get("fetchCustomFields"))) {
                fetchCustomFields = Boolean.parseBoolean((String) requestParams.get("fetchCustomFields"));
            }

            List<String> nonEditableFormFields = new ArrayList<String>();

            if (updateExistingRecordFlag) {
                // get List of non editable fields of that module form

                List list = importDao.getModuleNonEditableFields(moduleId);
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    String dhid = (String) itr.next();
                    DefaultHeader dh = (DefaultHeader) KwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.DefaultHeader", dhid);
                    nonEditableFormFields.add(dh.getPojoMethodName());
                }
            }

            Object extraObj = requestParams.get("extraObj");
            JSONObject extraParams = (JSONObject) requestParams.get("extraParams");

            String jsondata = (String) requestParams.get("resjson");
            JSONObject rootcsvjobj = new JSONObject(jsondata);
            JSONArray mapping = rootcsvjobj.getJSONArray("root");

            String classPath = "", primaryKey = "", uniqueKeyMethodName = "", uniqueKeyHbmName = "";
            try {
                List list = importDao.getModuleObject(mode);
                module = (Modules) list.get(0); //Will throw null pointer if no module entry found
            } catch (Exception ex) {
                throw new DataInvalidateException("Column config not available for module " + mode);
            }

            try {
                classPath = module.getPojoClassPathFull().toString();
                primaryKey = module.getPrimaryKey_MethodName().toString();
            } catch (Exception ex) {
                throw new DataInvalidateException("Please set proper properties for module " + mode);
            }
            uniqueKeyMethodName = module.getUniqueKey_MethodName();
            uniqueKeyHbmName = module.getUniqueKey_HbmName();

            int subModuleFlag = 0;
            //Replaced the multiple arguments of getModuleColumnConfig() with single HashMap object
            HashMap<String, Object> params = new HashMap<String, Object> ();
            params.put("moduleId", module.getId());
            params.put("companyid", (String) requestParams.get("companyid"));
            params.put("isdocumentimport", "F");
            params.put("subModuleFlag", new Integer(subModuleFlag));
            params.put("isBomlessFile", isBomlessFile);
            params.put("updateExistingRecordFlag", updateExistingRecordFlag);
            JSONArray columnConfig = getModuleColumnConfig(params);
            if (fetchCustomFields) {
                JSONArray CustomDataJArr = getCustomModuleColumnConfig(module.getId(), (String) requestParams.get("companyid"), false,params);
                for (int i = 0; i < CustomDataJArr.length(); i++) {
                    JSONObject jSONObject = CustomDataJArr.getJSONObject(i);
                    String clmName = jSONObject.getString("columnName");
                    for (int k = 0; k < mapping.length(); k++) {
                        JSONObject mappingJson = mapping.getJSONObject(k);
                        String datakey = mappingJson.getString("columnname");
                        boolean customflag = (!StringUtil.isNullOrEmpty(mappingJson.getString("customflag"))) ? Boolean.parseBoolean(mappingJson.getString("customflag")) : false;
                        if (customflag && clmName.equalsIgnoreCase(datakey)) {
                            columnConfig.put(jSONObject);
                        }
                    }
                }
            }
            Map rowRefDataMap = new HashMap();
            for(int i=0 ; i<columnConfig.length() ; i++){
                JSONObject ccObject = columnConfig.getJSONObject(i);
                if("ref".equalsIgnoreCase(ccObject.optString("validatetype", null))){
                    rowRefDataMap.put(ccObject.optString("pojoName", ""), new HashMap());
                }
            }
            tableName = importDao.getTableName(fileName);
            HashMap<String, Object> filterParams = new HashMap<String, Object>();
//            filterParams.put("isvalid", 1); //To fetch valid records
            KwlReturnObject kresult = importDao.getFileData(tableName, filterParams); //Fetch all valid records
            List fileDataList = kresult.getEntityList();
            Iterator itr = fileDataList.iterator();
            List failureArr = new ArrayList();
            List failureColumnArr = new ArrayList();
            if (itr.hasNext()) {
                Object[] fileData = (Object[]) itr.next(); //Skip header row
                if (mode.equals("Vendor") || mode.equals("Customer")) {
                    Map<Integer, Object> invalidColumn = new HashMap<>();//This Map is used to hold index of column whose data is invalid. 
                    List headerArr = new ArrayList();
                    /*
                     * Since we are reading data from temporary table. In this
                     * table we add four columns to it 
                     * #1st is id of type integer 
                     * #2nd is isvalid 
                     * #3rd is invalidcolumns and 
                     * #4th validatelog 
                     * These are needed to remove while reading
                     * actual header of file
                     */
                    for (int header = 1; header < fileData.length - 3; header++) {
                        if (fileData[header] != null && !fileData[header].equals("")) {
                            headerArr.add(fileData[header]);
                        }
                    }
                    headerArr.add("Error Message");// adding one more header as Error Message
                    failureArr.add(headerArr);
                    failureColumnArr.add(invalidColumn);
                } else {
                    failedRecords.append(createCSVrecord(fileData) + "\"Error Message\"");//failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\"");
                }
            }
            int recIndex = 0;
            importDao.markRecordValidation(tableName, -1, 1, "", ""); //reset all invalidation
            Set modulePrimaryKeyValues = new HashSet();
            while (itr.hasNext()) {
                total++;
                Object[] fileData = (Object[]) itr.next();
                recIndex = (Integer) fileData[0];
                Map<Integer, Object> invalidColumn = new HashMap<>();//This Map is used to hold index of column whose data is invalid. this kept under while loop because it needs initialization for each record
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                HashMap<String, Object> columnHeaderMap = new HashMap<String, Object>();
                HashMap<String, Object> columnCSVindexMap = new HashMap<String, Object>();
                JSONArray customfield = new JSONArray();
                for (int k = 0; k < mapping.length(); k++) {
                    JSONObject mappingJson = mapping.getJSONObject(k);
                    String datakey = mappingJson.getString("columnname");
                    Object dataValue = cleanHTML((String) fileData[mappingJson.getInt("csvindex") + 1]); //+1 for id column at index-0
                    dataMap.put(datakey, dataValue);
                    if (datakey.contains("Name")) {
                        dataValue = cleanSpeacialChar((String) fileData[mappingJson.getInt("csvindex") + 1]); //+1 for id column at index-0
                        dataMap.put(datakey, dataValue);
                    }
                    columnHeaderMap.put(datakey, mappingJson.getString("csvheader"));
                    columnCSVindexMap.put(datakey, mappingJson.getInt("csvindex") + 1);
                }

                for (int j = 0; j < extraParams.length(); j++) {
                    String datakey = (String) extraParams.names().get(j);
                    Object dataValue = extraParams.get(datakey);
                    dataMap.put(datakey, dataValue);
                }


                if (isImportingGroup) {// in case importing accounts group
                    groupdispOrder++;
                    String datakey = "DisplayOrder";
                    dataMap.put(datakey, groupdispOrder);
                }

                String orignalOpeningBalance = "";
                if ((requestParams.get("moduleName").toString().equalsIgnoreCase("Accounts")) && (dataMap.containsKey("OpeningBalance"))) {
                    orignalOpeningBalance = dataMap.get("OpeningBalance")==null?"0":dataMap.get("OpeningBalance").toString();
                }

                if (requestParams.get("moduleName").toString().equalsIgnoreCase("Accounts")) {
                    if(dataMap.containsKey("bookBeginningDate")){
                        dataMap.remove("bookBeginningDate");
                    }
                }

                Object object = null;
                try {
                    CheckUniqueRecord(requestParams, dataMap, invalidColumn,classPath, uniqueKeyMethodName, uniqueKeyHbmName, primaryKey, columnHeaderMap,columnCSVindexMap,modulePrimaryKeyValues);
                    validateDataMAP2(requestParams, dataMap, invalidColumn, columnConfig, customfield, columnHeaderMap, columnCSVindexMap, dateFormat, rowRefDataMap);
//                    if (StringUtil.equal(balanceType, "Credit")) {    //In case of credit OpeningBalance will be negative 
//                        Object dataValue = "-" + dataMap.get("OpeningBalance");
//                        double openBalance = StringUtil.getDouble(dataValue.toString());
//                        dataMap.put("OpeningBalance", openBalance);
//                    }
                    if ((requestParams.get("moduleName").toString().equalsIgnoreCase("Accounts")) && (dataMap.containsKey("OpeningBalance"))) {
                        dataMap.put("orignalOpeningBalance", orignalOpeningBalance);
                    }

                    if (updateExistingRecordFlag && dataMap.containsKey(primaryKey) && dataMap.get(primaryKey) != null && !StringUtil.isNullOrEmpty(dataMap.get(primaryKey).toString())) {// if update Existing Record Checkbox is true and the record is exist already in db(only in this case dataMap will contain primary Key Value i.e "id" value).
                        String companyId = (String) requestParams.get("companyid");
                        if (requestParams.containsKey("moduleName") && requestParams.get("moduleName") != null) {
                            String moduleName = (String) requestParams.get("moduleName");
                            checkForNonEditableFields(companyId, moduleId, moduleName, primaryKey, dataMap, nonEditableFormFields);
                        }
                    }
                    boolean isIndiaCompany = (requestParams.containsKey("countryid") && Integer.parseInt(requestParams.get("countryid").toString()) == Constants.indian_country_id);
                    if (isIndiaCompany) {
                        if (dataMap.containsKey("gstapplicabledate")) {
                            requestParams.put("ApplyDate", (Date) dataMap.get("gstapplicabledate"));
                            dataMap.remove("gstapplicabledate");
                        }
                    }
                    object = importDao.saveRecord(requestParams, dataMap, null, mode, classPath, primaryKey, extraObj, customfield);
                    

                    /*
                    * Below block of code is used to insert audit trail entry.
                    * ModuleId check is also applied for module specific audit trail entries.
                    */
                    if (moduleId.equals(Constants.Account_ModuleId)) {
                        Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                        auditRequestParams.put(Constants.reqHeader, requestParams.get(Constants.reqHeader));
                        auditRequestParams.put(Constants.remoteIPAddress, requestParams.get(Constants.remoteIPAddress));
                        auditRequestParams.put(Constants.useridKey, requestParams.get(Constants.useridKey));
                        // Get Primary Key for new object saved - this recid is used to save audit trail entries.
                        Field field = object.getClass().getDeclaredField(primaryKey);
                        field.setAccessible(true);
                        String recid = (String) field.get(object);
                        
                        String auditMsg = "", auditID = "";
                        if(!updateExistingRecordFlag){
                            auditMsg = "added";
                            auditID = AuditAction.ACCOUNT_CREATED;
                        } else {
                            auditMsg = "updated";
                            auditID = AuditAction.ACCOUNT_UPDATED;
                        }
                        String accountCode = dataMap.get("Acccode").toString();
                        auditMsg = "User " + requestParams.get(Constants.userfullname).toString() + " has " + auditMsg + " account " + accountCode + Constants.auditMessageViaImport;
                        // Insert audit trail entry.
                        auditTrailObj.insertAuditLog(auditID, auditMsg, auditRequestParams, recid);
                    }
                } catch (Exception ex) {
                    failed++;
                    String errorMsg = ex.getMessage(), invalidColumns = "";
                    try {
                        JSONObject errorLog = new JSONObject(errorMsg);
                        errorMsg = errorLog.getString("errorMsg");
                        errorMsg = errorMsg.replaceAll("<br/>", "");//since we used same method (validateDataMAP2()) for validation while valoidating and import. 
                                                                    //At the time of validation we need to break line so appended <br/> where as while importing it does not require so replacing it with empty 
                        invalidColumns = errorLog.getString("invalidColumns");
                    } catch (JSONException jex) {
                    }
                    if (mode.equals("Vendor") || mode.equals("Customer")) {//Rejected file in XLS For customer and Vednor. TO DO other modules 
                        List failureRecArr = new ArrayList();
                        for (int cellData = 1; cellData < fileData.length - 3; cellData++) {
                            failureRecArr.add(fileData[cellData]);
                        }
                        failureRecArr.add(errorMsg.replaceAll("\"", ""));
                        failureArr.add(failureRecArr);

                        failureColumnArr.add(invalidColumn);
                    } else {
                        failedRecords.append("\n" + createCSVrecord(fileData) + "\"" + errorMsg + "\"");//failedRecords.append("\n"+(total)+","+createCSVrecord(fileData)+"\""+ex.getMessage()+"\"");
                        importDao.markRecordValidation(tableName, recIndex, 0, errorMsg, invalidColumns);
                    }
                }
            }

            if (failed > 0) {
                if (mode.equals("Vendor") || mode.equals("Customer")) {////Rejected file in XLS For customer and Vednor. TO DO other modules 
                    failureFileType="xls";
                    importDao.createFailureXlsFiles(fileName, failureArr, ".xls", failureColumnArr);
                } else {
                    createFailureFiles(fileName, failedRecords, ".csv");
                    failureFileType="csv";
                }
            }

            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
                msg = messageSource.getMessage("acc.rem.169", null, locale);
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
        } catch (Exception e) {
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + e.getMessage();
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);
            boolean exCommit = false;
            try {
                //Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                String logId = (String) requestParams.get("logId");
                if (!StringUtil.isNullOrEmpty(logId)) {
                    logDataMap.put("Id", logId);
                }
                logDataMap.put("FileName", ImportLog.getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", fileName.substring(fileName.lastIndexOf(".") + 1));
                logDataMap.put("FailureFileType", failureFileType);
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", issuccess ? failed : total);// if fail then rejected = total
                logDataMap.put("Module", module.getId());
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", (String) requestParams.get("userid"));
                logDataMap.put("Company", (String) requestParams.get("companyid"));
                importDao.saveImportLog(logDataMap);
                
                boolean allowToChildCompanies = requestParams.containsKey("allowropagatechildcompanies") ? Boolean.parseBoolean(requestParams.get("allowropagatechildcompanies").toString()) : false;
                if (allowToChildCompanies) {
                    if (requestParams.containsKey("totalChildCompanies") && requestParams.containsKey("currentChildCompanycount")) {
                        int totalChildCompanies = Integer.parseInt(requestParams.get("totalChildCompanies").toString());
                        int currentChildCompanycount = Integer.parseInt(requestParams.get("currentChildCompanycount").toString());
                        if (totalChildCompanies == currentChildCompanycount) {
                            importDao.removeFileTable(tableName);//Remove table after importing all records in all child companies
                        }
                    }
                } else {
                    importDao.removeFileTable(tableName);//Remove table after importing all records
                }
                try {
                    txnManager.commit(lstatus);
                } catch (Exception ex) {
                    exCommit = true;
                    throw ex;
                }
            } catch (Exception ex) {
                if (!exCommit) { //if exception occurs during commit then dont call rollback
                    txnManager.rollback(lstatus);
                }
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("totalrecords", total);
                jobj.put("successrecords", total - failed);
                jobj.put("failedrecords", failed);
                jobj.put("filename", ImportLog.getActualFileName(fileName));
                jobj.put("mailurl",requestParams.get("mailurl"));
            } catch (JSONException ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
     /*-------- Function to Import user permission role wise-------*/

    public JSONObject importPermissionsRoleWise(HashMap<String, Object> requestParams) throws ServiceException, IOException, JSONException {
        JSONObject jobj = new JSONObject();
        CsvReader csvReader = null;
        FileInputStream fileInputStream = null;
        String fileName = "", tableName = "", extn = "", failureFileType = "";
        fileName = (String) requestParams.get("filename");
        extn = fileName.substring(fileName.lastIndexOf(".") + 1);

        tableName = importDao.getTableName(fileName);
        HashMap<String, Object> filterParams = new HashMap<String, Object>();
        String delimiterType = requestParams.get("delimiterType").toString();
        KwlReturnObject kresult = importDao.getFileData(tableName, filterParams); //Fetch all valid records
        String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
        File filepath = new File(destinationDirectory + "/" + fileName);

        fileInputStream = new FileInputStream(filepath);
        csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);

        Long permissionCode = 0L;
        Long permissionCodeCalculator = 1L;
        String msg = "";
        String mode = (String) requestParams.get("modName");
        Modules module = null;

        boolean issuccess = true;
        int total = 0;
        int failed = 0;
        int recordCount = 0;     
        String featureID = "";
        String roleid = "";
        String activityID = "";
       
        boolean isPermissionGroupCountIncremented = false;
       
        ArrayList featureNameList = new ArrayList();
        ArrayList featureIDList = new ArrayList();

        ArrayList activityIDList = new ArrayList();
        ArrayList permissionType = new ArrayList();
        ArrayList activityNameList = new ArrayList();

        String  roleName = "";
        String previousFeatureId = "";
      
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        
     
       
        try {
            
            try {
                List list = importDao.getModuleObject(mode);
                module = (Modules) list.get(0); //Will throw null pointer if no module entry found
            } catch (Exception ex) {
                throw new DataInvalidateException("Column config not available for module " + mode);
            }
            
            
            while (csvReader.readRecord()) {
                total++;
            
                String[] recarr = csvReader.getValues();
                if (recordCount == 0) {
              
                    String[] roleNameArr = recarr[2].split("-");
                    roleName = roleNameArr[1].trim();
                    
                    
                    requestParams.put("roleName", roleName);

                    roleid = authHandlerDAOObj.getRoleIdByRoleName(requestParams);
                    
                    if (roleid.isEmpty()) {
                        throw new DataInvalidateException("Role Name provided in Import File is not present in the system");
                    }
                                                                         
                }
            
                if (recordCount != 0) {

                    /*---------Checking Permission Group-------- */
                    if (!recarr[0].isEmpty()) {

                        if (featureNameList.indexOf(recarr[0]) == -1) {
                            featureID = authHandlerDAOObj.gefeateatureIdByFeatureName(recarr[0]);
                            if (featureID.isEmpty()) {
                                throw new DataInvalidateException("Permission Group provided in Import file is not present in the system");
                            }
                            featureIDList.add(featureID);
                            featureNameList.add(recarr[0]);

                            if (recordCount > 1) {
                                isPermissionGroupCountIncremented = true;
                                previousFeatureId = featureIDList.get(featureIDList.size() - 2).toString();
                            }

                        }
                    } else {
                      throw new DataInvalidateException("Permission Group provided in Import file is empty");
                    }
                    
                    
                    /*--------------- If permission Group changed then calculate Permission code for that particular permission group(eg:-> Account Preferences , Sales Order etc.)-------------------*/
                    if (isPermissionGroupCountIncremented) {
                        isPermissionGroupCountIncremented = false;

                        for (int actitvityCount = 0; actitvityCount < activityIDList.size(); actitvityCount++) {
                            String booleanValue = permissionType.get(actitvityCount).toString();
                            if (booleanValue.equalsIgnoreCase("Yes")) {

                                /* -------------Calculation of Permission Code against Individual Permission Group for its all activity(eg:->View , Export , Edit etc.)-------------------*/
                                permissionCode = permissionCode + permissionCodeCalculator;

                            }
                            permissionCodeCalculator = 2 * permissionCodeCalculator;

                        }

                                              
                        /*---------------- Saving Permission for individual Role-----------------*/
                        requestParams.put("roleid", roleid);
                        requestParams.put("isEdit", true);
                        requestParams.put("rolename", roleName);

                        KwlReturnObject kmsg = authHandlerDAOObj.setRolePermissions(requestParams,   previousFeatureId, permissionCode);//String type parameter is enough here instead of List type.So pls do change

                           
                        activityIDList.clear();
                        permissionType.clear();
                        permissionCode = 0L;
                        permissionCodeCalculator = 1L;
                        activityNameList.clear();
                    }

                    /* Preparing ActitvityId List per permission Group*/
                    if (!recarr[1].isEmpty()) {
                                          
                 
                        activityID = authHandlerDAOObj.getActivityIdByActivityName(recarr[1], featureID);
                        if (activityID.isEmpty()) {
                            throw new DataInvalidateException("Permission Name provided in Import file is not matched against Permission Group provided in Import file");
                        }
                    } else {
                        throw new DataInvalidateException("Permission Name provided in Import file is empty");
                    }
                    activityIDList.add(activityID);
                    activityNameList.add(recarr[1]);
                
                    if (recarr[2].equalsIgnoreCase("Yes") || recarr[2].equalsIgnoreCase("No")) {
                        permissionType.add(recarr[2]);
                    } else {
                        recarr[2] = "No";
                        permissionType.add(recarr[2]);
                    }
                   
                }
                recordCount++;

            }

            /*-------------- Code executed ,If only one Permission Group or Last permission Group Present in imported file-------------*/
            if (recordCount > 1) {

                /*--------------- Iterating on Activity list(eg:->Create , View , Edit , Export etc.)--------------*/
                for (int actitvityCount = 0; actitvityCount < activityIDList.size(); actitvityCount++) {
                    String booleanValue = permissionType.get(actitvityCount).toString();
                    if (booleanValue.equalsIgnoreCase("Yes")) {

                        /* -------------Calculation of Permission Code against Individual Permission Group for its all activity-------------------*/
                        permissionCode = permissionCode + permissionCodeCalculator;

                    }
                    permissionCodeCalculator = 2 * permissionCodeCalculator;

                }


                /*---------------- Saving Permission for individual Role-----------------*/
                requestParams.put("roleid", roleid);
                requestParams.put("isEdit", true);
                requestParams.put("rolename", roleName);

                KwlReturnObject kmsg = authHandlerDAOObj.setRolePermissions(requestParams,  featureID, permissionCode);
            }
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }


            int success = total - failed;
            if (success == total) {
                msg = "All records are imported successfully.";
            } 
        } catch (Exception e) {
            txnManager.rollback(status);
            issuccess = false;
            msg = "" + e.getMessage();
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);
            boolean exCommit = false;
            try {
                //Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                String logId = (String) requestParams.get("logId");
                if (!StringUtil.isNullOrEmpty(logId)) {
                    logDataMap.put("Id", logId);
                }
                logDataMap.put("FileName", ImportLog.getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", fileName.substring(fileName.lastIndexOf(".") + 1));
                logDataMap.put("FailureFileType", failureFileType);
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", issuccess ? failed : total);// if fail then rejected = total
                logDataMap.put("Module", module.getId());
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", (String) requestParams.get("userid"));
                logDataMap.put("Company", (String) requestParams.get("companyid"));
                importDao.saveImportLog(logDataMap);

                boolean allowToChildCompanies = requestParams.containsKey("allowropagatechildcompanies") ? Boolean.parseBoolean(requestParams.get("allowropagatechildcompanies").toString()) : false;
                if (allowToChildCompanies) {
                    if (requestParams.containsKey("totalChildCompanies") && requestParams.containsKey("currentChildCompanycount")) {
                        int totalChildCompanies = Integer.parseInt(requestParams.get("totalChildCompanies").toString());
                        int currentChildCompanycount = Integer.parseInt(requestParams.get("currentChildCompanycount").toString());
                        if (totalChildCompanies == currentChildCompanycount) {
                            importDao.removeFileTable(tableName);//Remove table after importing all records in all child companies
                        }
                    }
                } else {
                    importDao.removeFileTable(tableName);//Remove table after importing all records
                }
                try {
                    txnManager.commit(lstatus);
                } catch (Exception ex) {
                    exCommit = true;
                    throw ex;
                }
            } catch (Exception ex) {
                if (!exCommit) { //if exception occurs during commit then dont call rollback
                    txnManager.rollback(lstatus);
                }
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("totalrecords", total);
                jobj.put("successrecords", total);
                jobj.put("filename", ImportLog.getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return jobj;
    }
    

    /*
     * This methods removes the noneditable fields value of existing record.
     *
     * Non Editable fields are those fields which cannot be edit once the
     * Master has been saved and that Master has been used in any transaction
     */
    public void checkForNonEditableFields(String companyId, String moduleId, String mouleName, String primaryKey, HashMap<String, Object> dataMap, List<String> nonEditableFormFields) throws ServiceException, DataInvalidateException {

        // Check weather this Master Record is used in other Transactions Or Not

        Set<String> objectKeys = dataMap.keySet();

        if (mouleName.equals(Constants.Acc_Customer_modulename) || mouleName.equals(Constants.Acc_Vendor_modulename)) {// in case of customer/vendor no need to check weather it is used in any transaction or not as its form has no such field which freezed after using it in any transaction. except Creation Date

            Iterator it = objectKeys.iterator();

            while (it.hasNext()) {
                String key = it.next().toString();
                if (nonEditableFormFields.contains(key)) {
                    if (dataMap.containsKey(key)) {
                        it.remove();
                    }
                }
            }
            return;
        }

        if (mouleName.equals("Accounts")) {
            String accountId = (String) dataMap.get(primaryKey);

            // Check whether this account is used in any transaction or not

            boolean isAccountHavingTransactions = importDao.isAccountHavingTransactions(accountId, companyId);

            if (isAccountHavingTransactions) {
                Iterator it = objectKeys.iterator();

                while (it.hasNext()) {
                    String key = it.next().toString();
                    if (nonEditableFormFields.contains(key)) {
                        if (dataMap.containsKey(key)) {
                            it.remove();
                        }
                    }
                }
            }

            dataMap.put("isAccountHavingTransactions", isAccountHavingTransactions);

        }

    }

    public void validateDataMAP2(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, Map<Integer,Object>invalidColumn,JSONArray columnConfigArray, JSONArray customfield, HashMap<String, Object> columnHeaderMap, HashMap<String, Object> columnCSVindexMap, String dateFormat, Map rowRefDataMap) throws DataInvalidateException, ServiceException, ParseException {
        String errorMsg = "", invalidColumns = "", fileCurrencyName="", fileCreationDate="";
        String mode = (String) requestParams.get("modName");
        boolean isCurrencyCode=false; 
        boolean fetchCustomFields = false;
        boolean isBatchQtyMandatory = false;
        boolean skipLineLevelValidation = false;
        boolean isBomlessFile = false;
        boolean updateExistingRecordFlag = false;
        String companyId = (String) requestParams.get("companyid");
        if (requestParams.containsKey("isBomlessFile") && requestParams.get("isBomlessFile")!=null) {
            isBomlessFile = (Boolean) requestParams.get("isBomlessFile");
        }
        if (requestParams.containsKey("isCurrencyCode")) {
            isCurrencyCode = Boolean.parseBoolean(requestParams.get("isCurrencyCode").toString());
        }
        if (!StringUtil.isNullOrEmpty((String) requestParams.get("fetchCustomFields"))) {
            fetchCustomFields = Boolean.parseBoolean((String) requestParams.get("fetchCustomFields"));
        }
        if (requestParams.containsKey("allowDuplcateRecord") && !StringUtil.isNullOrEmpty(requestParams.get("allowDuplcateRecord").toString())) {
            updateExistingRecordFlag = Boolean.parseBoolean(requestParams.get("allowDuplcateRecord").toString());
        }
        boolean isBookClosed = false;
        if (requestParams.containsKey("isBookClosed") && requestParams.get("isBookClosed") != null) {
            isBookClosed = (Boolean) requestParams.get("isBookClosed");
        }
        if (mode.equalsIgnoreCase("Accounts")) {
            if (dataMap.containsKey("OpeningBalance") && dataMap.containsKey("CreationDate") && dataMap.get("OpeningBalance")!= null && dataMap.get("CreationDate")!= null && !StringUtil.isNullOrEmpty(dataMap.get("CreationDate").toString())) {
                double orignalOpeningBalance = 0;
                // Check OpeningBalance has improper amount then skip this check and incorrect data error is thrown in further checks.
                try {
                    orignalOpeningBalance = Double.parseDouble(dataMap.get("OpeningBalance").toString());
                } catch(NumberFormatException ex){
                }
                if(orignalOpeningBalance!=0 ){
                    SimpleDateFormat creationDateDF = null;
                    String fileName = (String) requestParams.get("filename");
                    String extn = fileName.substring(fileName.lastIndexOf(".") + 1);
                    if(StringUtil.isNullOrEmpty(dateFormat) || extn.equalsIgnoreCase("xls")){
                        creationDateDF=new SimpleDateFormat("yyyy-MM-dd");
                    }else{
                        creationDateDF=new SimpleDateFormat(dateFormat);
                    } 
                    if (requestParams.containsKey("bookBeginningDate") && requestParams.get("bookBeginningDate") != null) {
                        Date bbDate = (Date) requestParams.get("bookBeginningDate");
                        Date creationDate = creationDateDF.parse(dataMap.get("CreationDate").toString());
                        if (creationDate.after(bbDate)) {
                            throw new DataInvalidateException("Creation date should not be after book begining date.");
                        }
                    }
                    if (isBookClosed) {
                        Locale locale = null;
                        if (requestParams.containsKey("locale")) {
                            locale = (Locale) requestParams.get("locale");
                        }
                        throw new DataInvalidateException(messageSource.getMessage("acc.account.openingbalance.cannotbeadded.if.fyclosed", null, locale));
                    }
                }
            }
            /**
             * ERP-41444:[Dot Com Smoke Testing] [Payment Method] Account Name
             * are not showing. Added import case.When account is mapped with
             * payment method or tax it will not allow to update exiting
             * account's master type
             */
            if (updateExistingRecordFlag) {
                KwlReturnObject result1 = null;
                Object[] masterTypeValueobj = null;
                String masterTypeValue = "";
                List list = null;
                int masterTypeInGrid = 0;
                if (columnHeaderMap.containsKey("Mastertypevalue") && dataMap.containsKey("Mastertypevalue")) {
                    masterTypeValue = !StringUtil.isNullOrEmpty((String) dataMap.get("Mastertypevalue")) ? (String) dataMap.get("Mastertypevalue") : "";
                    int val = 0;
                    if (masterTypeValue.equalsIgnoreCase("General Ledger")) {
                        val = Constants.ACCOUNT_MASTERTYPE_GL;
                    } else if (masterTypeValue.equalsIgnoreCase("Cash")) {
                        val = Constants.ACCOUNT_MASTERTYPE_CASH;
                    } else if (masterTypeValue.equalsIgnoreCase("Bank")) {
                        val = Constants.ACCOUNT_MASTERTYPE_BANK;
                    } else if (masterTypeValue.equalsIgnoreCase("GST")) {
                        val = Constants.ACCOUNT_MASTERTYPE_GST;
                    }
                    String id = dataMap.get("ID").toString();
                    boolean isAccountMappedWithPaymentMethodOrTax = false;
                    if (!StringUtil.isNullOrEmpty(companyId) && !StringUtil.isNullOrEmpty(id)) {
                        isAccountMappedWithPaymentMethodOrTax = importDao.isAccountMappedWithPaymentMethodOrTax(id, companyId);
                        result1 = importDao.getAccountDetailsFromAccountId(id);
                        list = result1.getEntityList();
                        if (!list.isEmpty() && list.size() > 0) {
                            masterTypeValueobj = (Object[]) list.get(0);
                            if (masterTypeValueobj != null) {
                                if (masterTypeValueobj[5] != null) {
                                    masterTypeInGrid = (Integer) masterTypeValueobj[5];
                                }
                            }
                        }
                    }
                    if (isAccountMappedWithPaymentMethodOrTax && val != masterTypeInGrid) {
                        Locale locale = null;
                        if (requestParams.containsKey("locale")) {
                            locale = (Locale) requestParams.get("locale");
                        }
                        throw new DataInvalidateException(messageSource.getMessage("acc.accounting.accountMappedWithPaymentMethodOrTax", null, locale));
                    }
                }
            }
        }
        /*
         * Check for Stock UOM, Casing UOM, Inner UOM.
         * Casing Uom, Inner Uom and Stock Uom must be different.
        */        
        if (mode.equalsIgnoreCase("Product")) {
            String stockUOM = "", casingUOM = "", innerUOM = "";
            if(dataMap.containsKey("UnitOfMeasure") && dataMap.get("UnitOfMeasure")!=null){
                stockUOM = dataMap.get("UnitOfMeasure").toString();
            }
            if(dataMap.containsKey("casingUoM") && dataMap.get("casingUoM")!=null){
                casingUOM = dataMap.get("casingUoM").toString();
            }
            if(dataMap.containsKey("innerUoM") && dataMap.get("innerUoM")!=null){
                innerUOM = dataMap.get("innerUoM").toString();
            }
            if((!StringUtil.isNullOrEmptyWithTrim(casingUOM) && casingUOM.equalsIgnoreCase(innerUOM)) || 
               (!StringUtil.isNullOrEmptyWithTrim(casingUOM) && casingUOM.equalsIgnoreCase(stockUOM)) ||
               (!StringUtil.isNullOrEmptyWithTrim(innerUOM) && innerUOM.equalsIgnoreCase(stockUOM))){
                throw new DataInvalidateException("Casing Uom, Inner Uom and Stock Uom cannot be same.");
            }
        }
        
        if (dataMap.containsKey("bompid") && isBomlessFile) {   //Import Assembly Product Without BOM call. Here import file should not contain BOM Details
            throw new DataInvalidateException("This is a Assembly Product without BOM. So import the file without BOM details.");
        }
        
        JSONObject productActivationProperties = new JSONObject();
        
         if (mode.equals("Customer Invoices")) { 
                //While Importing customer inovices, Line level item can be empty in certain cases for certain Records.
                // So below is the code to handle that case
                String uniqueKeyMethodName = "InvoiceNumber";
                Set uniqueValuesSet = null;
                if (requestParams.containsKey("uniqueValuesSet") && requestParams.get("uniqueValuesSet") != null) {
                    uniqueValuesSet = (Set) requestParams.get("uniqueValuesSet");
                } else {
                    uniqueValuesSet = new HashSet();
                }
                String invoiceNumber = dataMap.containsKey(uniqueKeyMethodName) && dataMap.get(uniqueKeyMethodName) != null ? dataMap.get(uniqueKeyMethodName).toString() : "";
                // While importing customer invoices we can repeat global level information. So for the first Global level data there must be Line level data
                // So if condition check, is it first global level data of the invoice or not
                boolean added = uniqueValuesSet.add(invoiceNumber);
                if (!added) {
                    boolean isInvoiceTermDataIsEmpty = checkAllInvoiceTermItemIsEmpty(dataMap);
                    if (!isInvoiceTermDataIsEmpty) {//invoice term details is non empty
                        boolean isLineLevelDataIsEmpty = checkAllLineLevelItemIsEmpty(dataMap, columnConfigArray);
                        if (isLineLevelDataIsEmpty) {//If Line Lele record is not first record and Invoice term details is not empty and Line Level information is empty then need to skip validation
                            skipLineLevelValidation = true;
                        }
                    }
                }
                requestParams.put("uniqueValuesSet", uniqueValuesSet);
            }
        
        
        
        if (mode.equalsIgnoreCase("product opening stock")) {
            if (dataMap.containsKey("Productid")) {
                String productCode = (String) dataMap.get("Productid");
                if (StringUtil.isNullOrEmpty(productCode)) {
                    throw new DataInvalidateException("Empty data found in Product ID, cannot set empty data for Product ID.");
                }
                try {
                    
                    List<Object[]> list = importDao.getProductLevelActiveItems(companyId, productCode);
                    if (!list.isEmpty()) {
                        Object[] obj = list.get(0);
                        String id = obj[0] == null ? "" : (String) obj[0];
                        boolean warehouse = obj[1] == null ? false : (Boolean) obj[1];
                        boolean location = obj[2] == null ? false : (Boolean) obj[2];
                        boolean row = obj[3] == null ? false : (Boolean) obj[3];
                        boolean rack = obj[4] == null ? false : (Boolean) obj[4];
                        boolean bin = obj[5] == null ? false : (Boolean) obj[5];
                        boolean batch = obj[6] == null ? false : (Boolean) obj[6];
                        boolean serial = obj[7] == null ? false : (Boolean) obj[7];
                        productActivationProperties.put("Warehouse", warehouse);
                        productActivationProperties.put("Location", location);
                        productActivationProperties.put("Row", row);
                        productActivationProperties.put("Rack", rack);
                        productActivationProperties.put("Bin", bin);
                        productActivationProperties.put("Batch", batch);
                        productActivationProperties.put("Serial", serial);
                        dataMap.put("isWarehouse", warehouse);
                        dataMap.put("isLocation", location);
                        dataMap.put("isBatch", batch);
                        dataMap.put("isSerial", serial);
                        dataMap.put("isRow", row);
                        dataMap.put("isRack", rack);
                        dataMap.put("isBin", bin);

                        if ((warehouse || location || batch || row || rack || bin) && !serial) {
                            isBatchQtyMandatory = true;
                        }
                        dataMap.put("isBatchQtyMandatory", isBatchQtyMandatory);
                        
                        boolean isProductUsedInTransaction = importDao.isProductUsedInTransaction(companyId, id);
                        if (isProductUsedInTransaction) {
                            throw new DataInvalidateException("Product is already used in transaction,so can't update its opening quantity.");
                        }
                        
                        // ERP-28052 Check Currency and Product Currency is same or not
                        if (dataMap.containsKey("Currency") && dataMap.get("Currency")!=null && !StringUtil.isNullOrEmpty(dataMap.get("Currency").toString())) {
                            String currency = (String) dataMap.get("Currency");
                            String productCurrencyName = importDao.getProductCurrencyName(companyId, id);
                            if(!StringUtil.isNullOrEmpty(currency) && !currency.equalsIgnoreCase(productCurrencyName)){
                                throw new DataInvalidateException("Currency '"+ currency +"' does not match with Product Currency '"+ productCurrencyName +"'.");
                            }
                        }
                        if (isBookClosed) {
                            if (dataMap.containsKey("Quantity")) {
                                double quantity = (!StringUtil.isNullObject(dataMap.get("Quantity")) && !StringUtil.isNullOrEmpty(dataMap.get("Quantity").toString())) ? Double.parseDouble(dataMap.get("Quantity").toString()) : 0;
                                KwlReturnObject result = importDao.getOpeningQuantityAndInitialPrice(companyId, id);
                                double oldQuantity = 0;
                                if (result != null && !result.getEntityList().isEmpty() && result.getEntityList().get(0) != null) {
                                    Object[] objArr = (Object[]) result.getEntityList().get(0);
                                    oldQuantity = (Double) objArr[0];
                                }
                                if (quantity != oldQuantity) {
                                    throw new DataInvalidateException("Cannot add/update initial quantity for the product, as book(s) for first financial year is already closed.");
                                }
                            }

                            if (dataMap.containsKey("price")) {
                                double price = (!StringUtil.isNullObject(dataMap.get("price")) && !StringUtil.isNullOrEmpty(dataMap.get("price").toString())) ? Double.parseDouble(dataMap.get("price").toString()) : 0;
                                KwlReturnObject result = importDao.getOpeningQuantityAndInitialPrice(companyId, id);
                                double oldPrice =0 ;
                                if (result != null && !result.getEntityList().isEmpty() && result.getEntityList().get(0) != null) {
                                    Object[] objArr = (Object[]) result.getEntityList().get(0);
                                    oldPrice = (Double) objArr[1];
                                }
                                if (price != oldPrice) {
                                    throw new DataInvalidateException("Cannot add/update initial price for the product, as book(s) for first financial year is already closed.");
                                }
                            }
                        }
//                        if(batch || serial){
//                            throw new DataInvalidateException("Batch or Serial is enable for this product.So,you can't give its opening quantity through import.");
//                        }
                        
                    } else {
                        throw new DataInvalidateException("Product ID '" + productCode + "' not exists.");
                    }

                } catch (JSONException ex) {
                    Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ServiceException ex) {
                    Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                throw new DataInvalidateException("Product ID column is not found.");
            }
        } else if ((mode.equals("Vendor") || mode.equals("Customer"))) {//need to preserve currency name it has use later
            if (dataMap.containsKey("Currency")) {
                fileCurrencyName = (String) dataMap.get("Currency");
            }
            if (isCurrencyCode) {
                if (dataMap.containsKey("currencyCode")) {
                    fileCurrencyName = (String) dataMap.get("currencyCode");
                }
            }
            if (dataMap.containsKey("CreatedOn")) {
                fileCreationDate = (String) dataMap.get("CreatedOn");
            }
        }
        if (dataMap.containsKey("Location") && dataMap.get("Location") != null) {  // Check Validation for Assembly product that if Location Active is true then Default Location is Mandatory 
            
            Boolean isIslocationforproduct = false;
            String ISlocationforproduct = dataMap.containsKey("isIslocationforproduct") ? dataMap.get("isIslocationforproduct").toString() : "F";
            if (ISlocationforproduct.equalsIgnoreCase("T") || ISlocationforproduct.equalsIgnoreCase("TRUE")) {
                isIslocationforproduct = true;
            } else if (ISlocationforproduct.equalsIgnoreCase("F") || ISlocationforproduct.equalsIgnoreCase("FALSE")) {
                isIslocationforproduct = false;
            }

            if (StringUtil.isNullOrEmpty(dataMap.get("Location").toString()) && isIslocationforproduct) {
                throw new DataInvalidateException("Default Location is not available.");
            }
        }
        if(dataMap.containsKey("Warehouse") && dataMap.get("Warehouse") != null){ // Check Validation for Assembly product that if Warehouse Active is true then Default Warehouse is Mandatory 
            Boolean isIswarehouseforproduct = false;
            String Iswarehouseforproduct = dataMap.containsKey("isIswarehouseforproduct") ? dataMap.get("isIswarehouseforproduct").toString() : "F";
            if (Iswarehouseforproduct.equalsIgnoreCase("T") || Iswarehouseforproduct.equalsIgnoreCase("TRUE")) {
                isIswarehouseforproduct = true;
            } else if (Iswarehouseforproduct.equalsIgnoreCase("F") || Iswarehouseforproduct.equalsIgnoreCase("FALSE")) {
                isIswarehouseforproduct = false;
            }

            if (StringUtil.isNullOrEmpty(dataMap.get("Warehouse").toString()) && isIswarehouseforproduct) {
                throw new DataInvalidateException("Default Warehouse is not available.");
            }
        }
        if ((mode.equals("Vendor Invoice") || mode.equals("GoodsReceiptOrder") || mode.equalsIgnoreCase("Customer Invoices") || mode.equalsIgnoreCase("Cash Sales") || mode.equalsIgnoreCase("Delivery Order")) && !skipLineLevelValidation) {
            String productCode = (String) dataMap.get("Product");
            List<Object[]> list = importDao.getProductLevelActiveItems(companyId, productCode);
            if (!list.isEmpty()) {
                Object[] obj = list.get(0);
                String id = obj[0] == null ? "" : (String) obj[0];
                boolean isWarehouse = obj[1] == null ? false : (Boolean) obj[1];
                boolean isLocation = obj[2] == null ? false : (Boolean) obj[2];
                boolean isRow = obj[3] == null ? false : (Boolean) obj[3];
                boolean isRack = obj[4] == null ? false : (Boolean) obj[4];
                boolean isBin = obj[5] == null ? false : (Boolean) obj[5];
                boolean isBatch = obj[6] == null ? false : (Boolean) obj[6];
                boolean isSerial = obj[7] == null ? false : (Boolean) obj[7];

                dataMap.put("isWarehouse", isWarehouse);
                dataMap.put("isLocation", isLocation);
                dataMap.put("isBatch", isBatch);
                dataMap.put("isSerial", isSerial);
                dataMap.put("isRow", isRow);
                dataMap.put("isRack", isRack); 
                dataMap.put("isBin", isBin);
                
                /*
                Changes done in ERP-38812
                */
                boolean validateBatchQty = true;
                if (dataMap.containsKey("GenerateGoodsReceipt") && dataMap.get("GenerateGoodsReceipt") != null) {
                    validateBatchQty = dataMap.get("GenerateGoodsReceipt").toString().equalsIgnoreCase("true");
                } else if (dataMap.containsKey("GenerateDeliveryOrder") && dataMap.get("GenerateDeliveryOrder") != null) {
                    validateBatchQty = dataMap.get("GenerateDeliveryOrder").toString().equalsIgnoreCase("true");
                } else if (mode.equalsIgnoreCase("Delivery Order")||(mode.equals("Vendor Invoice") && !dataMap.containsKey("GenerateGoodsReceipt")) ||(mode.equalsIgnoreCase("Customer Invoices") && !dataMap.containsKey("GenerateDeliveryOrder"))) {
                    validateBatchQty = false;
                }
                
                if((isWarehouse || isLocation || isBatch || isRow || isRack || isBin) && !isSerial && validateBatchQty){
                   isBatchQtyMandatory=true; 
                }
                dataMap.put("isBatchQtyMandatory", isBatchQtyMandatory);
                
                // Check batch or serial is activated for product then value for that must be present in file.
                if(isBatch && (!dataMap.containsKey("NewProductBatch") || dataMap.get("NewProductBatch")==null || StringUtil.isNullOrEmptyWithTrim(dataMap.get("NewProductBatch").toString()))){
                    throw new DataInvalidateException("Product ID '" + productCode + "' has batch activated. Please specify valid batch name.");
                }
                if(isSerial && (!dataMap.containsKey("NewBatchSerial") || dataMap.get("NewBatchSerial")==null || StringUtil.isNullOrEmptyWithTrim(dataMap.get("NewBatchSerial").toString()))){
                    throw new DataInvalidateException("Product ID '" + productCode + "' has serial activated. Please specify valid serial number.");
                }
                //(isWarehouse || isLocation || isBatch || isSerial)
                if(isBatchQtyMandatory && (!dataMap.containsKey("BatchQuantity") || dataMap.get("BatchQuantity")==null || StringUtil.isNullOrEmptyWithTrim(dataMap.get("BatchQuantity").toString()))){
                    throw new DataInvalidateException("Product ID '" + productCode + "' has Warehouse/Location/Batch activated. Please specify valid batch quantity.");
                }
            } else {
                throw new DataInvalidateException("Product ID '" + productCode + "' not exists.");
            }
           
        }
        for (int k = 0; k < columnConfigArray.length(); k++) {
            JSONObject columnConfig = null;
            String column = "";
            try {
                columnConfig = columnConfigArray.getJSONObject(k);
                if (columnConfig.has("pojoName")) {
                    column = columnConfig.getString("pojoName");
                    }
                if (productActivationProperties.has(column)) {
                    columnConfig.put("isNotNull", productActivationProperties.get(column));
                }

                /*IsMandatory */
                if (mode.equals("Role Management")) {
                    columnConfig.put("isNotNull", columnConfig.get("isMandatory"));
                }
                
                boolean customflag = false;
                if (columnConfig.has("customflag")) {
                    customflag = columnConfig.getBoolean("customflag");
                }
                boolean isLineLevelItem = false;
                if (columnConfig.optString("fieldtype").equals(Constants.DefaultHeader_DefaultLineItems) ||(columnConfig.optString("fieldtype").equals(Constants.DefaultHeader_BatchDetails) && column.equals("BatchQuantity"))) {
                    isLineLevelItem = true;
                }
                if (dataMap.containsKey(column)) {
                    if(!(isLineLevelItem && skipLineLevelValidation)){//If column is line level item and skipLineLevelValidation is coming true then no need to go for validation
                        validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat, rowRefDataMap);
                    } 
                }else if(customflag && fetchCustomFields){  //To validate custom field data
                    column= columnConfig.getString("columnName");
                    validateCustomFieldColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat, rowRefDataMap);
                }else {
                    if (columnConfig.has("defaultValue") && dataMap.containsKey(column)) {
                        dataMap.put(column, getDefaultValue(columnConfig,requestParams));
                    } else if (columnConfig.has("isNotNull") && columnConfig.getBoolean("isNotNull") && (!updateExistingRecordFlag && (mode.equals("Vendor") || mode.equals("Customer")))) {
                        throw new DataInvalidateException("Data required for field " + column);
                    } 
                    if (columnConfig.has("isConditionalMandetory") && columnConfig.getBoolean("isConditionalMandetory")) {
                        boolean isMandatoryConditionFound = false;
                        /*
                        ERP-34567- while importing Customer/Vendor Master record as update existing flag true the make Conditional Mandatory as a optional
                        */
                        if (!(updateExistingRecordFlag && (mode.equals("Vendor") || mode.equals("Customer") || mode.equals(Constants.Acc_Product_modulename) || mode.equals(Constants.IMPORT_PRODUCT_PRICE)))) {
                            isMandatoryConditionFound = isMandatoryConditionForColumn(requestParams, dataMap, column);
                        }
                        if (isMandatoryConditionFound) {
                            String columnHeader = columnConfig.getString("columnName");
                            if (mode.equalsIgnoreCase(Constants.IMPORT_PRODUCT_PRICE) && (column.equals("PurchasePrice") || column.equals("SalesPrice"))) {
                                throw new DataInvalidateException("Sales or Purchase price column is not found.Please insert Sales or Purchase Price column in File or insert both.");
                            } else {
                                throw new DataInvalidateException(columnHeader + " column is not found.");
                        }
                    }
                  }
                }
                /**
                 * validate Billing and Shipping Address for Customer/Vendor import
                 */
                if (mode.equals("Vendor") || mode.equals("Customer")) {
                    boolean isMandatory = columnConfig.optBoolean("isMandatory", false);
                    boolean isGSTAddressMapping = columnConfig.optBoolean(Constants.GSTAddressMapping, false);
                    List list = importDao.getExtraCompanyPref(companyId);
                    /**
                     * If updateExistingRecordFlag then validate mapped address fields
                     */
                    if ((isMandatory || updateExistingRecordFlag ) && isGSTAddressMapping && list != null && !list.isEmpty() && list.get(0) != null) {
                        Object[] extraCompanyPref = (Object[]) list.get(0);
                        boolean isNewGST = extraCompanyPref[0] != null ? (String.valueOf(extraCompanyPref[0]).equalsIgnoreCase("1") || String.valueOf(extraCompanyPref[0]).equalsIgnoreCase("T")) : false;
                        if (isNewGST) {// This function called only if isNewGST flag ON (US and INDIA)
                            validateAddressFieldsForNewGST(requestParams, dataMap, columnConfig, column, columnHeaderMap);
                        }
                    }
                }
            } catch (Exception ex) {
                errorMsg += ex.getMessage()+"<br/>";
                if(columnCSVindexMap.get(column)!=null){
                    invalidColumn.put((int)columnCSVindexMap.get(column)-1, "Invalid");
                    invalidColumns += ("col" + columnCSVindexMap.get(column) + ",");
                }
            }
        }
        // Below if case for customer or vendor when Currency and Creation date is valid
        // in this case we need to check wheather exchange rate avilable for currency or not for given creation date
            if ((mode.equalsIgnoreCase("Customer") || mode.equalsIgnoreCase("Vendor"))) {
            if ((isCurrencyCode?columnCSVindexMap.containsKey("currencyCode"):columnCSVindexMap.containsKey("Currency")) && columnCSVindexMap.containsKey("CreatedOn") && (isCurrencyCode?!invalidColumn.containsKey((int) columnCSVindexMap.get("currencyCode") - 1):!invalidColumn.containsKey((int) columnCSVindexMap.get("Currency") - 1)) && !invalidColumn.containsKey((int) columnCSVindexMap.get("CreatedOn") - 1)) {//both should be valid
                String currencyid =isCurrencyCode?(String) dataMap.get("currencyCode"):(String) dataMap.get("Currency");String gcurrencyid = StringUtil.isNullOrEmpty((String) requestParams.get("gcurrencyid")) ? "" : (String) requestParams.get("gcurrencyid");//company currency
                if (!currencyid.equalsIgnoreCase(gcurrencyid)) {//when currency for customer/vendor is not base currency then we need to check for exchange rate
                    Date creationDate = (Date) dataMap.get("CreatedOn"); //CreatedOn is pojomethod name in default_header
                    KwlReturnObject result = importDao.getExcDetailID(requestParams, currencyid, creationDate);

                    if (result == null || (result != null && result.getRecordTotalCount() == 0)) {
                        errorMsg += "Exchange Rate is not available for currency " + fileCurrencyName + " on creation date " + fileCreationDate + " .<br/>";
                        invalidColumn.put(isCurrencyCode?(int) columnCSVindexMap.get("currencyCode") - 1:(int) columnCSVindexMap.get("Currency") - 1, "Invalid");
                        invalidColumns += ("col" + (isCurrencyCode?columnCSVindexMap.get("currencyCode"):columnCSVindexMap.get("Currency")) + ",");
                    }
                }
            }
        }
        /**
         * Handle GST Rules import 'CESS Type/ Valuation Amount ' column validation for INDIA GST.
         */
        if (mode.equalsIgnoreCase("GSTTerm") && columnCSVindexMap.containsKey(IndiaComplianceConstants.CESS)
                && columnCSVindexMap.containsKey(IndiaComplianceConstants.GST_CESS_TYPE) && columnCSVindexMap.containsKey(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT)) {
            String CESSTermValue = dataMap.containsKey(IndiaComplianceConstants.CESS) && dataMap.get(IndiaComplianceConstants.CESS) != null ? dataMap.get(IndiaComplianceConstants.CESS).toString() : "";
            CESSTermValue = CESSTermValue.replaceAll("\"", "").trim();
            String CESSType = dataMap.containsKey(IndiaComplianceConstants.GST_CESS_TYPE) && dataMap.get(IndiaComplianceConstants.GST_CESS_TYPE) != null ? dataMap.get(IndiaComplianceConstants.GST_CESS_TYPE).toString() : "";
            CESSType = CESSType.replaceAll("\"", "").trim();
            String CESSValuationAmount = dataMap.containsKey(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT) && dataMap.get(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT) != null ? dataMap.get(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT).toString() : "";
            CESSValuationAmount = CESSValuationAmount.replaceAll("\"", "").trim();
            Locale locale = new Locale("en", "US");   //Default value English
            if (requestParams.containsKey("locale") && requestParams.get("locale") != null) {
                locale = (Locale) requestParams.get("locale");
            }
            if (!StringUtil.isNullOrEmpty(CESSType)) {
                if (!StringUtil.isNullOrEmpty(CESSTermValue) && !CESSTermValue.equalsIgnoreCase("NA")) {
                    if (!IndiaComplianceConstants.CESSTYPE.containsKey(CESSType)) {
                        errorMsg += messageSource.getMessage("acc.gstrr.gstrule.cesstype.error.txt1", new Object[]{}, locale);
                        invalidColumn.put((int) columnCSVindexMap.get(IndiaComplianceConstants.GST_CESS_TYPE) - 1, "Invalid");
                        invalidColumns += ("col" + columnCSVindexMap.get(IndiaComplianceConstants.GST_CESS_TYPE) + ",");
                    } else if (IndiaComplianceConstants.CESSTYPE.containsKey(CESSType) && StringUtil.isNullOrEmpty(CESSValuationAmount)
                            && !CESSType.equalsIgnoreCase(IndiaComplianceConstants.NOT_APPLICABLE) && !CESSType.equalsIgnoreCase(IndiaComplianceConstants.PERCENTAGES)) {
                        errorMsg += messageSource.getMessage("acc.gstrr.gstrule.cesstype.error.txt2", new Object[]{}, locale);
                        invalidColumn.put((int) columnCSVindexMap.get(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT) - 1, "Invalid");
                        invalidColumns += ("col" + columnCSVindexMap.get(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT) + ",");
                    }
                } else {
                    errorMsg += messageSource.getMessage("acc.gstrr.gstrule.cesstype.error.txt3", new Object[]{}, locale);
                    invalidColumn.put((int) columnCSVindexMap.get(IndiaComplianceConstants.GST_CESS_TYPE) - 1, "Invalid");
                    invalidColumns += ("col" + columnCSVindexMap.get(IndiaComplianceConstants.GST_CESS_TYPE) + ",");
                    invalidColumn.put((int) columnCSVindexMap.get(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT) - 1, "Invalid");
                    invalidColumns += ("col" + columnCSVindexMap.get(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT) + ",");
                }
            } else if (!StringUtil.isNullOrEmpty(CESSValuationAmount)) {
                errorMsg += messageSource.getMessage("acc.gstrr.gstrule.cesstype.error.txt4", new Object[]{}, locale);
                invalidColumn.put((int) columnCSVindexMap.get(IndiaComplianceConstants.GST_CESS_TYPE) - 1, "Invalid");
                invalidColumns += ("col" + columnCSVindexMap.get(IndiaComplianceConstants.GST_CESS_TYPE) + ",");
            }
        }
            
        if (mode.equalsIgnoreCase("Opening Sales Invoice") || mode.equalsIgnoreCase("Opening Purchase Invoice")) {
            //#1 Due date must be on or after creation date
            if (columnCSVindexMap.containsKey("CreationDate") && columnCSVindexMap.containsKey("DueDate") && (!invalidColumn.containsKey((int) columnCSVindexMap.get("CreationDate") - 1) && !invalidColumn.containsKey((int) columnCSVindexMap.get("DueDate") - 1))) {//both should be valid
                Date transactionDate = (Date) dataMap.get("CreationDate");
                transactionDate = removeTimefromDate(transactionDate);
                Date duedate = (Date) dataMap.get("DueDate");
                duedate = removeTimefromDate(duedate);
                if (duedate.before(transactionDate)) {
                    errorMsg += "Due Date must be on or after Transaction Date.<br/>";
                    invalidColumn.put((int) columnCSVindexMap.get("DueDate") - 1, "Invalid");
                    invalidColumns += ("col" + columnCSVindexMap.get("DueDate") + ",");
                }
            }
            
            //#2 Purchase order date must be on or after creation date
            if (columnCSVindexMap.containsKey("CreationDate") && columnCSVindexMap.containsKey("PoRefDate") && (!invalidColumn.containsKey((int) columnCSVindexMap.get("CreationDate") - 1) && !invalidColumn.containsKey((int) columnCSVindexMap.get("PoRefDate") - 1))) {//both should be valid
                Date transactionDate = (Date) dataMap.get("CreationDate");
                transactionDate = removeTimefromDate(transactionDate);
                Date poDate = (Date) dataMap.get("PoRefDate");
                poDate = removeTimefromDate(poDate);
                if (poDate.after(transactionDate)) {
                    errorMsg += "Purchase Order Date must be on or before Transaction Date.<br/>";
                    invalidColumn.put((int) columnCSVindexMap.get("PoRefDate") - 1, "Invalid");
                    invalidColumns += ("col" + columnCSVindexMap.get("PoRefDate") + ",");
                }
            }
        }
        
        if (mode.equalsIgnoreCase("Accounts") && errorMsg.length() == 0) { // here checking for mater type and currency combination. If already exception coming then no need to check for this.
                String currencyid = StringUtil.isNullOrEmpty((String) requestParams.get("gcurrencyid")) ? "" : (String) requestParams.get("gcurrencyid");
                int masterTypevalue = (Integer) dataMap.get("Mastertypevalue");
                String accountCurrencyID = StringUtil.isNullOrEmpty((String) dataMap.get("Currency")) ? "" : (String) dataMap.get("Currency");
                if (!StringUtil.isNullOrEmpty(currencyid) && !StringUtil.isNullOrEmpty(accountCurrencyID) && masterTypevalue != -1) { //when account 
                    if (!accountCurrencyID.equalsIgnoreCase(currencyid) && (masterTypevalue == 1 || masterTypevalue == 4)) { //accounting having non base currency and master type is general ledger or GST then in case we reject such accounts
                        errorMsg +="Master type General Ledger OR GST cannot have currency other than base currency.<br/>";
                    }
                }
        }
                
        if (mode.equalsIgnoreCase("Accounts") && errorMsg.length() == 0) { // here checking for Group and Type combination. If already exception coming then no need to check for this.
                String groupID = (String) dataMap.get("Group");
                int typeVal = (Integer) dataMap.get("Accounttype");
                int mastertypevalue = (Integer) dataMap.get("Mastertypevalue");

                if (!StringUtil.isNullOrEmpty(groupID)) {
                    try {
                        int natureValue = -1;

                        natureValue = importDao.getNatureByGroupID(groupID, companyId);
                        if ((natureValue == 0 || natureValue == 1) && typeVal == 0) { // Group Liability of Asset and Type Profit & Loss
                            errorMsg +="Group Should be of nature Income and Expenses for Profit and Loss type.<br/>";
                        }

                        if ((natureValue == 2 || natureValue == 3) && typeVal == 1) { // Group Expense or Income and Type Balance sheet
                            errorMsg +="Group Should be of nature Asset and Liability for Balance Sheet type.<br/>";
                        }
                    } catch (ServiceException ex) {
                        Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                /*
                * To Check Master Type and Type combination. For Account Type Profit & Loss, Master Type General Ledger is only allowed.
                */
                if(typeVal == 0 && mastertypevalue!=1){
                    errorMsg +="Wrong value for Master Type.<br/>"; // if Account Type is Profit & Loss and Master Type is not General Ledger.
                }
            }

        // Validation of Customer Address Details for Alise Name
        if (mode.equalsIgnoreCase("Customer Address Details")) {
            if (columnCSVindexMap.containsKey("AliasName") && columnCSVindexMap.containsKey("IsBillingAddress") && columnCSVindexMap.containsKey("CustomerID") && !invalidColumn.containsKey((int) columnCSVindexMap.get("AliasName") - 1) && !invalidColumn.containsKey((int) columnCSVindexMap.get("IsBillingAddress") - 1) && !invalidColumn.containsKey((int) columnCSVindexMap.get("CustomerID") - 1)) {
                try {
                    String aliasName = (String) dataMap.get("AliasName");
                    boolean isBillingAddress = (Boolean) dataMap.get("IsBillingAddress");
                    String customerID = (String) dataMap.get("CustomerID");
                    KwlReturnObject result = importDao.getCustomerAddressDetailsForCustomerByAliasName(customerID, aliasName, isBillingAddress, companyId);

                    if (result != null && result.getRecordTotalCount() > 0) {
                        errorMsg += "You have already added address with alias name " + aliasName + ". So please enter different alias name.<br/>";
                        invalidColumn.put((int) columnCSVindexMap.get("AliasName") - 1, "Invalid");
                        invalidColumns += ("col" + (columnCSVindexMap.get("AliasName")) + ",");
                    }
                } catch (ServiceException ex) {
                    Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        // Check Document No is valid as per Document Type for Make Payment Module
        if(mode.equalsIgnoreCase("Payment") || mode.equalsIgnoreCase("Receipt")){
            SimpleDateFormat importDF=new SimpleDateFormat(dateFormat);
            boolean isCustomer = false;
            boolean isVendor = false;
            boolean isGL = false;
            if (requestParams.get("customer")!=null && !StringUtil.isNullOrEmpty(requestParams.get("customer").toString())) {                     //checking import make payment against customer
                isCustomer = Boolean.parseBoolean(requestParams.get("customer").toString());
            }
            if (requestParams.get("vendor")!=null && !StringUtil.isNullOrEmpty(requestParams.get("vendor").toString())) {                       //checking import make payment against vendor    
                isVendor = Boolean.parseBoolean(requestParams.get("vendor").toString());
            }
            if (requestParams.get("GL")!=null && !StringUtil.isNullOrEmpty(requestParams.get("GL").toString())) {                           //checking import make payment against GL
                isGL = Boolean.parseBoolean(requestParams.get("GL").toString());
            }
            Date billDate = null;
            if(mode.equalsIgnoreCase("Payment") && dataMap.get("JournalEntry") != null && !StringUtil.isNullOrEmpty(dataMap.get("JournalEntry").toString())){
                billDate = importDF.parse(dataMap.get("JournalEntry").toString());
            }else if(mode.equalsIgnoreCase("Receipt") && dataMap.get("date") != null && !StringUtil.isNullOrEmpty(dataMap.get("date").toString())){
                billDate = importDF.parse(dataMap.get("date").toString());
            }
            String vendorId = "";
            String customerId = "";
            if(mode.equalsIgnoreCase("Payment")){
                if(dataMap.get("Acccode") != null && !StringUtil.isNullOrEmpty(dataMap.get("Acccode").toString()) && isVendor){
                    vendorId = dataMap.get("Acccode").toString();
                } else if(dataMap.get("Customer") != null && !StringUtil.isNullOrEmpty(dataMap.get("Customer").toString()) && isCustomer){
                    customerId = dataMap.get("Customer").toString();
                }
            } else if(mode.equalsIgnoreCase("Receipt")){
                if(dataMap.get("Vendor") != null && !StringUtil.isNullOrEmpty(dataMap.get("Vendor").toString()) && isVendor){
                    vendorId = dataMap.get("Vendor").toString();
                } else if(dataMap.get("Customer") != null && !StringUtil.isNullOrEmpty(dataMap.get("Customer").toString()) && isCustomer){
                    customerId = dataMap.get("Customer").toString();
                }
            }
            
            if(dataMap.get("documenttype") != null){
                boolean selectedDocumentIsNotAvailableFlag = false; // To handled both scenarios temporary / permanent deleted or say not present in system.
                String documenyNoStr = "";
                Date documentNoDate = null;
                String documenyTypeStr = "";
                String documenyType = dataMap.get("documenttype").toString();
                if (documenyType.equalsIgnoreCase("Advanced/Deposit") || documenyType.equalsIgnoreCase("Advance / Deposit") || documenyType.equalsIgnoreCase("Advance/Deposit") || documenyType.equalsIgnoreCase("Advanced / Deposit")&& ((mode.equalsIgnoreCase("Payment") && isVendor) || (mode.equalsIgnoreCase("Receipt") && isCustomer))) {
                    documenyTypeStr = Constants.ADVANCE_REFUND;
                } else if ((documenyType.equalsIgnoreCase("Refund/Deposit")  || documenyType.equalsIgnoreCase("Refund / Deposit")) && ((mode.equalsIgnoreCase("Payment") && isCustomer) || (mode.equalsIgnoreCase("Receipt") && isVendor))) {//requestParams
                    documenyTypeStr = Constants.ADVANCE_REFUND;
                } else if (documenyType.equalsIgnoreCase("Invoice")) {
                    documenyTypeStr = Constants.INVOICEIMPORT;
                } else if ((documenyType.equalsIgnoreCase("Credit Note") && mode.equalsIgnoreCase("Payment")) || (documenyType.equalsIgnoreCase("Debit Note") && mode.equalsIgnoreCase("Receipt"))) {
                    documenyTypeStr = Constants.CREDITNOTEIMPORT;
                } else if (documenyType.equalsIgnoreCase("General Ledger Code")) {
                    documenyTypeStr = Constants.GLIMPORT;
                }
                if(documenyTypeStr.equalsIgnoreCase(Constants.GLIMPORT)){
                    if(dataMap.get("documentno") != null){
                        documenyNoStr = dataMap.get("documentno").toString();
                        KwlReturnObject returnObject = importDao.getAccountFromName(companyId, documenyNoStr);
                        if (returnObject == null || returnObject.getEntityList() == null || returnObject.getRecordTotalCount() == 0) {
                            errorMsg += "Account Name '" + documenyNoStr + "' is not Available for Account.<br/>";
                            invalidColumn.put((int) columnCSVindexMap.get("documentno") - 1, "Invalid");
                            invalidColumns += ("col" + (columnCSVindexMap.get("documentno")) + ",");
                        } else {
                            List list = returnObject.getEntityList();
                            Object[] details = (Object[]) list.get(0);
                            String usedIn = details[1] != null ? (String) details[1] : "";
                            char wantToPostJe = (char) details[2];
                            char activeAccount = (char) details[3];
                            if(activeAccount == 'F'){
                                errorMsg += "Account Name '" + documenyNoStr + "' is not Available for Account as it is deactivated.<br/>";
                                invalidColumn.put((int) columnCSVindexMap.get("documentno") - 1, "Invalid");
                                invalidColumns += ("col" + (columnCSVindexMap.get("documentno")) + ",");
                            }
                            
                            if(!StringUtil.isNullOrEmpty(usedIn) && wantToPostJe == 'F'){
                                errorMsg += "Account Name '" + documenyNoStr + "' is not Available for Account as it is used in <b>"+usedIn+"</b>";
                                invalidColumn.put((int) columnCSVindexMap.get("documentno") - 1, "Invalid");
                                invalidColumns += ("col" + (columnCSVindexMap.get("documentno")) + ",");
                            }
                        }
                    }
                } else if(documenyTypeStr.equalsIgnoreCase(Constants.INVOICEIMPORT)){
                    if(dataMap.get("documentno") != null){
                        documenyNoStr = dataMap.get("documentno").toString();
                        if(mode.equalsIgnoreCase("Payment")){
                            if(isVendor){
                                // Get Purchase Invoice
                                KwlReturnObject returnObject = importDao.getGoodsReceiptData(documenyNoStr, companyId, vendorId);
                                if (returnObject != null && returnObject.getEntityList() != null && returnObject.getRecordTotalCount() > 0) {
                                    List list = returnObject.getEntityList();
                                    documentNoDate = (Date) list.get(0);
                                } else {
                                    selectedDocumentIsNotAvailableFlag = true;
                                }
                            } else if(isCustomer){ 
                                errorMsg += "You cannot Make Payment against Customer Invoice";
                                invalidColumn.put((int) columnCSVindexMap.get("documentno") - 1, "Invalid");
                                invalidColumns += ("col" + (columnCSVindexMap.get("documentno")) + ",");
                            }
                        }
                        if(mode.equalsIgnoreCase("Receipt") && isCustomer){
                            // Get Sales Invoice
                            KwlReturnObject returnObject = importDao.getInvoiceData(documenyNoStr, companyId, customerId);
                            if (returnObject != null && returnObject.getEntityList() != null && returnObject.getRecordTotalCount() > 0) {
                                List list = returnObject.getEntityList();
                                documentNoDate = (Date) list.get(0);
                            } else {
                                selectedDocumentIsNotAvailableFlag = true;
                            }
                        }
                    }
                } else if(documenyTypeStr.equalsIgnoreCase(Constants.ADVANCE_REFUND)){
                    if(dataMap.get("documentno") != null){
                        documenyNoStr = dataMap.get("documentno").toString();
                        if(mode.equalsIgnoreCase("Receipt") && isVendor){
                            // Get Advanced MP to Vendor for Refund/Deposit - RP from Vendor
                            KwlReturnObject returnObject = importDao.getRefundNameCountForReceipt(documenyNoStr, companyId, vendorId);
                            if (returnObject != null && returnObject.getEntityList() != null && returnObject.getRecordTotalCount() > 0) {
                                List list = returnObject.getEntityList();
                                Object[] details = (Object[]) list.get(0);
                                documentNoDate = (Date) details[4];
                            } else {
                                selectedDocumentIsNotAvailableFlag = true;
                            }
                        } else if(mode.equalsIgnoreCase("Payment") && isCustomer){
                            // Get Advanced RP from Customer for Refund/Deposit - MP to Customer
                            KwlReturnObject returnObject = importDao.getRefundNameCountForPayment(documenyNoStr, companyId, customerId);
                            if (returnObject != null && returnObject.getEntityList() != null && returnObject.getRecordTotalCount() > 0) {
                                List list = returnObject.getEntityList();
                                Object[] details = (Object[]) list.get(0);
                                documentNoDate = (Date) details[4];
                            } else {
                                selectedDocumentIsNotAvailableFlag = true;
                            }
                        }
                    }
                } else if(documenyTypeStr.equalsIgnoreCase(Constants.CREDITNOTEIMPORT) && !isGL){
                    if(dataMap.get("documentno") != null){
                        documenyNoStr = dataMap.get("documentno").toString();
                        KwlReturnObject returnObject = null;
                        if(mode.equalsIgnoreCase("Payment")){
                            if (isCustomer) {
                                // Get CN otherwise (Customer)
                                returnObject = importDao.getCustomerCreditNoCount(documenyNoStr, companyId, customerId);
                            } else if (isVendor) {
                                // Get CN against Vendor
                                returnObject = importDao.getVendorCreditNoCount(documenyNoStr, companyId, vendorId);
                            }
                        } else if(mode.equalsIgnoreCase("Receipt")){
                            if (isVendor) {
                                // Get DN otherwise (Vendor)
                                returnObject = importDao.getVendorDebitNoCount(documenyNoStr, companyId, vendorId);
                            } else if (isCustomer) {
                                // Get DN against Customer
                                returnObject = importDao.getCustomerDebitNoCount(documenyNoStr, companyId, customerId);
                            }
                        }
                        if (returnObject != null && returnObject.getEntityList() != null && returnObject.getRecordTotalCount() > 0) {
                            List list = returnObject.getEntityList();
                            documentNoDate = (Date) list.get(0);
                        } else {
                            selectedDocumentIsNotAvailableFlag = true;
                        }
                    }
                }
                
                if(documentNoDate!=null && billDate!=null && billDate.before(documentNoDate)){
                    errorMsg += mode+" creation date cannot be less than selected document(s) creation date.<br/>";
                    invalidColumn.put((int) columnCSVindexMap.get("documentno") - 1, "Invalid");
                    invalidColumns += ("col" + (columnCSVindexMap.get("documentno")) + ",");
                }
                
                if(!StringUtil.isNullOrEmpty(documenyNoStr) && !StringUtil.isNullOrEmpty(documenyType) && selectedDocumentIsNotAvailableFlag){
                    errorMsg += "Selected document '"+ documenyNoStr +"' is not available for document type '"+ documenyType +"'.<br/>";
                    invalidColumn.put((int) columnCSVindexMap.get("documentno") - 1, "Invalid");
                    invalidColumns += ("col" + (columnCSVindexMap.get("documentno")) + ",");
                }
            }
            if(dataMap.get("PaymentMethod") != null){
                int paymentDetailType = -1;
                List listPaymentMethod = null;
                Object[] dateValuesobj = null;
                String bankAccountId="";
                KwlReturnObject returnObject = importDao.getPaymentMethodDetail(dataMap.get("PaymentMethod").toString(), companyId);
                if (returnObject != null && !returnObject.getEntityList().isEmpty() && returnObject.getEntityList().get(0) != null) {
                    listPaymentMethod= returnObject.getEntityList();
                    dateValuesobj = (Object[]) listPaymentMethod.get(0);
                    if (dateValuesobj != null) {
                        if (dateValuesobj[0] != null) {
                            paymentDetailType=(int) dateValuesobj[0];
                        }
                        if (dateValuesobj[1] != null) {
                            bankAccountId = dateValuesobj[1].toString();
                        }
                    }
                }
                if(paymentDetailType == Constants.bank_detail_type){
                    if(dataMap.get("bankname") != null){
                        String bankNameStr = dataMap.get("bankname").toString();
                        if(StringUtil.isNullOrEmpty(bankNameStr)){
                            errorMsg += "Bank Name is not available.<br/>";
                            invalidColumn.put((int) columnCSVindexMap.get("bankname") - 1, "Invalid");
                            invalidColumns += ("col" + (columnCSVindexMap.get("bankname")) + ",");
                        } else if(mode.equalsIgnoreCase("Receipt")){
                            KwlReturnObject returnObj = importDao.getBankNameMasterItemName(companyId, bankNameStr);
                            if (returnObj.getEntityList().isEmpty()) {
                                errorMsg += "Incorrect Bank Name type value for Bank Name. Please add new Bank Name as \"" + bankNameStr + "\" with other details.";
                                invalidColumn.put((int) columnCSVindexMap.get("bankname") - 1, "Invalid");
                                invalidColumns += ("col" + (columnCSVindexMap.get("bankname")) + ",");
                            }
                        }
                    } else {
                        errorMsg += "Bank Name column is not found.<br/>";
                        invalidColumn.put((int) columnCSVindexMap.get("bankname") - 1, "Invalid");
                        invalidColumns += ("col" + (columnCSVindexMap.get("bankname")) + ",");
                    }
                    if(mode.equalsIgnoreCase("Payment")){
                        if(dataMap.get("ChequeNumber") != null) {
                            if (StringUtil.isNullOrEmpty(dataMap.get("ChequeNumber").toString())) {
                                errorMsg += "Cheque number is not provided. So cannot be imported record "+dataMap.get("PaymentNumber").toString()+".";
                                invalidColumn.put((int) columnCSVindexMap.get("ChequeNumber") - 1, "Invalid");
                                invalidColumns += ("col" + (columnCSVindexMap.get("ChequeNumber")) + ",");
                            } else {
                                int isDuplicateCheck=0;
                                KwlReturnObject cap = importDao.getCompanyAccountPreferences(companyId);
                                if (cap != null && !cap.getEntityList().isEmpty() && cap.getEntityList().get(0) != null) {
                                    isDuplicateCheck = (int)cap.getEntityList().get(0);
                                }
                                /**
                                 * O - Ignore duplicate cheque Number
                                 * 1 - Block,
                                 * 2 - Warn
                                 */
                                if(isDuplicateCheck != 0){
                                    Map params = new HashMap();
                                    boolean checkForNextSequenceNumberAlso = true;
                                    params.put("companyId", companyId);
                                    params.put("sequenceNumber", dataMap.get("ChequeNumber").toString());
                                    params.put("nextChequeNumber", dataMap.get("ChequeNumber").toString());
                                    params.put("checkForNextSequenceNumberAlso", checkForNextSequenceNumberAlso);
                                    params.put("bankAccountId", bankAccountId);
                                    boolean isChequeNumberAvailable = importDao.isChequeNumberAvailable(params);
                                    if(isChequeNumberAvailable){
                                        errorMsg += "Cannot import record as Cheque Number "+ dataMap.get("ChequeNumber").toString() +" already used in some Payment present in the system.";
                                        invalidColumn.put((int) columnCSVindexMap.get("ChequeNumber") - 1, "Invalid");
                                        invalidColumns += ("col" + (columnCSVindexMap.get("ChequeNumber")) + ",");
                                    } else {
                                        if(requestParams.get("checkNumberDuplicate") !=null){
                                            Set<String>  checkNumberDuplicate=(Set<String>) requestParams.get("checkNumberDuplicate");
                                            Set<String>  paymentNumbersSet=(Set<String>) requestParams.get("paymentNumbersSet");
                                            if(!paymentNumbersSet.contains(dataMap.get("PaymentNumber").toString())){
                                                paymentNumbersSet.add(dataMap.get("PaymentNumber").toString());
                                                boolean isDuplicateEntry = checkNumberDuplicate.add(dataMap.get("ChequeNumber").toString());
                                                if(!isDuplicateEntry){
                                                    errorMsg += "Cannot import record as Cheque Number "+ dataMap.get("ChequeNumber").toString() +" already exists in import file.";
                                                    invalidColumn.put((int) columnCSVindexMap.get("ChequeNumber") - 1, "Invalid");
                                                    invalidColumns += ("col" + (columnCSVindexMap.get("ChequeNumber")) + ",");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            errorMsg += "Cheque number is not provided. So cannot be imported record "+dataMap.get("PaymentNumber").toString()+".";
                            invalidColumn.put((int) columnCSVindexMap.get("ChequeNumber") - 1, "Invalid");
                            invalidColumns += ("col" + (columnCSVindexMap.get("ChequeNumber")) + ",");
                        }
                    }
                    if ((dataMap.get("Currency") != null || dataMap.get("KWLCurrency") != null) && dataMap.get("documentno") != null) {
                        if (dataMap.get("documenttype").toString().equalsIgnoreCase(Constants.IMPORT_INVOICE)
                                || dataMap.get("documenttype").toString().equalsIgnoreCase(Constants.IMPORT_CN)
                                || dataMap.get("documenttype").toString().equalsIgnoreCase(Constants.IMPORT_DN)) {
                            String paymentReceiptCurrrency = "";
                            if(mode.equals(Constants.IMPORT_PAYMENT)){
                                paymentReceiptCurrrency = dataMap.get("KWLCurrency").toString();
                            } else {
                                paymentReceiptCurrrency = dataMap.get("Currency").toString();
                            }
                            String paymentMethodAccountCurrency = "";
                            String docCurrency = "";
                            if (!StringUtil.isNullOrEmpty(bankAccountId)) {
                                KwlReturnObject returnObj = importDao.getAccountDetailsFromAccountId(bankAccountId);
                                List listCurId = returnObj.getEntityList();
                                dateValuesobj = (Object[]) listCurId.get(0);
                                if (dateValuesobj != null) {
                                    if (dateValuesobj[4] != null) {
                                        paymentMethodAccountCurrency = dateValuesobj[4].toString();
                                    }
                                }
                            }
                            if (dataMap.get("documentno") != null) {
                                KwlReturnObject returnobct = importDao.getDocumentDetailsFromDocumentNo(dataMap.get("documentno").toString(), companyId, mode, dataMap.get("documenttype").toString());
                                if (returnobct != null && !returnobct.getEntityList().isEmpty() && returnobct.getEntityList().get(0) != null) {
                                    List listDocCur = returnobct.getEntityList();
                                    docCurrency = (String) listDocCur.get(0);
                                }
                            }
                            if (!paymentReceiptCurrrency.equals(paymentMethodAccountCurrency)) {
                                if (!paymentReceiptCurrrency.equals(docCurrency)) {
                                    errorMsg += "Linked documents currency should be same as Payment / Receipt currency.";
                                    invalidColumn.put((int) columnCSVindexMap.get("documentno") - 1, "Invalid");
                                    invalidColumns += ("col" + (columnCSVindexMap.get("documentno")) + ",");
                                }
                            }
                        }
                    }
                }
            }
        } // Payment and Receipt module check block ends
        if (mode.equalsIgnoreCase("Payment") || mode.equalsIgnoreCase("Receipt") || mode.equalsIgnoreCase("Cash Sales") || mode.equalsIgnoreCase("Cash Purchase")) {
            if (dataMap.get("ChequeNumber") != null || dataMap.get("ChequeNo") != null || dataMap.get("chequenumber") != null) {
                String chequeNumber = "",errorMessage = "";
                if (mode.equalsIgnoreCase("Payment")) {
                    chequeNumber = dataMap.get("ChequeNumber").toString();
                } else if(mode.equalsIgnoreCase("Receipt")){
                    chequeNumber = dataMap.get("chequenumber").toString();
                } else {
                    chequeNumber = dataMap.get("ChequeNo").toString();
                }
                if (!StringUtil.isNullOrEmpty(chequeNumber)) {
                    errorMsg += errorMessage = isChequeNumberValidForImport(chequeNumber,true);
                    if (!StringUtil.isNullOrEmpty(errorMessage) && mode.equalsIgnoreCase("Payment")) {
                        invalidColumn.put((int) columnCSVindexMap.get("ChequeNumber") - 1, "Invalid");
                        invalidColumns += ("col" + (columnCSVindexMap.get("ChequeNumber")) + ",");
                    } else if(!StringUtil.isNullOrEmpty(errorMessage) &&  mode.equalsIgnoreCase("Receipt")){
                        invalidColumn.put((int) columnCSVindexMap.get("chequenumber") - 1, "Invalid");
                        invalidColumns += ("col" + (columnCSVindexMap.get("chequenumber")) + ",");
                    } else if(!StringUtil.isNullOrEmpty(errorMessage)){
                        invalidColumn.put((int) columnCSVindexMap.get("ChequeNo") - 1, "Invalid");
                        invalidColumns += ("col" + (columnCSVindexMap.get("ChequeNo")) + ",");
                    }
                }
            }
        }
        if (mode.equalsIgnoreCase("Journal Entry")){
            if (dataMap.get("DebitAmount") != null && dataMap.get("CreditAmount") != null 
                    && !StringUtil.isNullOrEmpty(dataMap.get("DebitAmount").toString()) && !StringUtil.isNullOrEmpty(dataMap.get("CreditAmount").toString())
                    && Double.parseDouble(dataMap.get("DebitAmount").toString()) > 0 && Double.parseDouble(dataMap.get("CreditAmount").toString()) > 0) {
                
                errorMsg += "Amount in both Debit and Credit columns are not allowed.";
                invalidColumn.put((int) columnCSVindexMap.get("CreditAmount") - 1, "Invalid");
                invalidColumns += ("col" + (columnCSVindexMap.get("CreditAmount")) + ",");
            }
        }
        
        if (errorMsg.length() > 0) {
            try {
                JSONObject errorLog = new JSONObject();
                errorLog.put("errorMsg", errorMsg);
                errorLog.put("invalidColumns", invalidColumns);
                errorMsg = errorLog.toString();
            } catch (JSONException ex) {
            }
            throw new DataInvalidateException(errorMsg);
        }
    }
    
    public boolean checkAllLineLevelItemIsEmpty(HashMap<String, Object> dataMap, JSONArray columnConfigArray) {
        boolean isAllLineLevelDataIsEmpty = true;
        try {
            for (int index = 0; index < columnConfigArray.length(); index++) {
                JSONObject columnConfig = columnConfigArray.getJSONObject(index);

                if (columnConfig.optString("fieldtype").equals(Constants.DefaultHeader_DefaultLineItems)) {// Checks only for line level data
                    String column = "";
                    if (columnConfig.has("pojoName")) {
                        column = columnConfig.getString("pojoName");
                    }
                    if (dataMap.containsKey(column)) {
                        String data = dataMap.get(column) == null ? null : dataMap.get(column).toString().replaceAll("\n", "");
                        if (!StringUtil.isNullOrEmpty(data)) {// If data is not empty then break loop and return as false
                            isAllLineLevelDataIsEmpty = false;
                            break;
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            isAllLineLevelDataIsEmpty =false;
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return isAllLineLevelDataIsEmpty;
    }
    
    public boolean checkAllInvoiceTermItemIsEmpty(HashMap<String, Object> dataMap) {
        boolean isAllLineLevelDataIsEmpty = true;
        Set<String> invoiceTermPojoMethods = new HashSet<>();
        invoiceTermPojoMethods.add("InvoiceTerm");
        invoiceTermPojoMethods.add("TermType");
        invoiceTermPojoMethods.add("TermValue");
        for (String column : invoiceTermPojoMethods) {
            if (dataMap.containsKey(column)) {
                String data = dataMap.get(column) == null ? null : dataMap.get(column).toString().replaceAll("\n", "");
                if (!StringUtil.isNullOrEmpty(data)) {// If data is not empty then break loop and return as false
                    isAllLineLevelDataIsEmpty = false;
                    break;
                }
            }
        }
        return isAllLineLevelDataIsEmpty;
    }
    
    public int dumpXLSFileData(String filename, int sheetNo, int startindex) throws ServiceException {
        int dumpedRows = 0;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "xlsfiles";
            FileInputStream fs = new FileInputStream(destinationDirectory + "/" + filename);
            Workbook wb = WorkbookFactory.create(fs);// It will create Workbook according to given excel file version like xls/xlsx
            Sheet sheet = wb.getSheetAt(sheetNo);
            DecimalFormat dfmt = new DecimalFormat("#.#####");
            int maxRow = sheet.getLastRowNum();
            int maxCol = 0;
            int count = 0;
            String tableName = importDao.getTableName(filename);
            for (int i = startindex; i <= maxRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                if (i == startindex) {
                    maxCol = row.getLastCellNum();  //Column Count
                }
                ArrayList<String> dataArray = new ArrayList<String>();
                JSONObject dataObj = new JSONObject();
                for (int j = 0; j < maxCol; j++) {
                    Cell cell = row.getCell(j);
                    String val = null;
                    if (cell == null) {
                        dataArray.add(val);
                        continue;
                    }
                    String colHeader = new CellReference(i, j).getCellRefParts()[2];
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            val = Double.toString(cell.getNumericCellValue());
                            if (DateUtil.isCellDateFormatted(cell)) {
                                java.util.Date date1 = DateUtil.getJavaDate(Double.parseDouble(val));
                                DateFormat sdf = new SimpleDateFormat(df);//(df_full); //BUG:16085
                                val = sdf.format(date1);
                            } else {
                                val = dfmt.format(cell.getNumericCellValue());
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            val = cell.getRichStringCellValue().getString();
                            break;
                    }
                    dataObj.put(colHeader, val);
                    dataArray.add(val); //Collect row data
                }
                //Insert Query
                if (dataObj.length() > 0) { // Empty row check (if lenght==0 then all columns are empty)
                    //dumpedRows += importDao.dumpFileRow(tableName, dataArray.toArray());
                    count++;
                    dumpedRows += importDao.dumpFileRow(tableName, dataArray.toArray());
                    if (count > 50) {
                        txnManager.commit(status);
                        status = txnManager.getTransaction(def);
                        count = 0;
                    }
                }
            }
        } catch (IOException ex) {
            txnManager.rollback(status);
            throw ServiceException.FAILURE("dumpXLSFileData: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            throw ServiceException.FAILURE("dumpXLSFileData: " + ex.getMessage(), ex);
        } finally {
            txnManager.commit(status);
        }
        return dumpedRows;
    }

    public int dumpCSVFileData(String filename, String delimiterType, int startindex, String fromdocument) throws DataInvalidateException {
        int dumpedRows = 0;
        int count = 0;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            CsvReader csvReader = null;
            FileInputStream fstream = null;
            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            File csv = new File(destinationDirectory + "/" + filename);
            fstream = new FileInputStream(csv);
            csvReader = new CsvReader(new InputStreamReader(fstream), delimiterType);
//            csvReader.readHeaders();
            String tableName = importDao.getTableName(filename);

            if (!StringUtil.isNullOrEmpty(fromdocument) && fromdocument.equalsIgnoreCase("true")) {
                csvReader.readRecord();
            }

            while (csvReader.readRecord()) {
                ArrayList<String> dataArray = new ArrayList<String>();
                for (int i = 0; i < csvReader.getColumnCount(); i++) {
                    dataArray.add(cleanHTML(csvReader.get(i)));
                }
                count++;
                dumpedRows += importDao.dumpFileRow(tableName, dataArray.toArray());
                if(count>50){
                    txnManager.commit(status);
                    status = txnManager.getTransaction(def);
                    count = 0;
                }
            }
            txnManager.commit(status);
        } catch (IOException ex) {
            txnManager.rollback(status);
            throw new DataInvalidateException("Invalid file, unable to read record at line " + (dumpedRows + 1), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            throw new DataInvalidateException("Invalid file, unable to parse record at line " + (dumpedRows + 1), ex);
        } 
        return dumpedRows;
    }

    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 1; i < listArray.length - 3; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString()) + "\",";
        }
        return rec;
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

    public HashMap<String, Object> getImportRequestParams(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("modName", request.getParameter("modName"));
        requestParams.put("moduleName", request.getParameter("moduleName"));
        requestParams.put("delimiterType", request.getParameter("delimiterType"));
        requestParams.put("filename", request.getParameter("filename"));
        requestParams.put("resjson", request.getParameter("resjson"));
        requestParams.put("sheetindex", request.getParameter("sheetindex"));
        requestParams.put("onlyfilename", request.getParameter("onlyfilename"));
        requestParams.put("dateFormat", request.getParameter("dateFormat"));
        requestParams.put("masterPreference", request.getParameter("masterPreference"));
        requestParams.put("fetchCustomFields", request.getParameter("fetchCustomFields"));

        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("userid", sessionHandlerImpl.getUserid(request));
        requestParams.put(Constants.userfullname, sessionHandlerImpl.getUserFullName(request));
        requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
        requestParams.put("doAction", request.getParameter("do"));
        requestParams.put("subModuleFlag", !StringUtil.isNullOrEmpty(request.getParameter("subModuleFlag")) ? request.getParameter("subModuleFlag") : "0");
        //requestParams.put("timezome", TimeZone.getTimeZone("GMT"+sessionHandlerImpl.getTimeZoneDifference(request)));
        requestParams.put(Constants.reqHeader, StringUtil.getIpAddress(request));
        requestParams.put(Constants.remoteIPAddress, request.getRemoteAddr());
        requestParams.put("isAssetsImportFromDoc", !StringUtil.isNullOrEmpty(request.getParameter("isAssetsImportFromDoc"))?Boolean.parseBoolean(request.getParameter("isAssetsImportFromDoc")):false);
        return requestParams;
    }

    public HashMap<String, Object> getImportRequestParams(JSONObject paramJobj) throws JSONException {
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("modName", paramJobj.optString("modName", ""));
        requestParams.put("moduleName", paramJobj.optString("moduleName", ""));
        requestParams.put("delimiterType", paramJobj.optString("delimiterType", ""));
        requestParams.put("filename", paramJobj.optString("filename", ""));
        requestParams.put("resjson", paramJobj.optString("resjson", ""));
        requestParams.put("sheetindex", paramJobj.optString("sheetindex", ""));
        requestParams.put("onlyfilename", paramJobj.optString("onlyfilename", ""));
        requestParams.put("dateFormat", paramJobj.optString("dateFormat", ""));
        requestParams.put("masterPreference", paramJobj.optString("masterPreference", ""));
        requestParams.put("fetchCustomFields", paramJobj.optString("fetchCustomFields", ""));
        requestParams.put("companyid", paramJobj.optString(Constants.companyKey));
        requestParams.put("userid", paramJobj.optString(Constants.useridKey));
        requestParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.globalCurrencyKey));
        requestParams.put("doAction", paramJobj.optString("do", ""));
        requestParams.put("subModuleFlag", paramJobj.optString("subModuleFlag", "0"));
        return requestParams;
    }

    public DateFormat getGMTDateFormatter(String df, HashMap<String, Object> requestParams) {
        SimpleDateFormat sdf = new SimpleDateFormat(df);
        sdf.setTimeZone((TimeZone) requestParams.get("timezome"));
        return sdf;
    }
    
    /*
     * Below method is copy of checksEntryNumberForSequenceNumber() in
     * accCompanyPreferencesImpl. It is created because accCompanyPreferencesImpl
     * is not accessible from importHander. So use below method in that case only
     * if for import data to assign sequence fromat
     */
    public List checksEntryNumberForSequenceNumber(Map<String, String> sequenceNumberDataMap) throws ServiceException {
        List ll = new ArrayList();
        try {
            int moduleid = sequenceNumberDataMap.containsKey("moduleID") ? (StringUtil.isNullOrEmpty(sequenceNumberDataMap.get("moduleID").toString()) ? -1 : Integer.parseInt(sequenceNumberDataMap.get("moduleID").toString())) : -1;
            String companyid = sequenceNumberDataMap.containsKey("companyID") ? (StringUtil.isNullOrEmpty(sequenceNumberDataMap.get("companyID").toString()) ? "" : sequenceNumberDataMap.get("companyID").toString()) : "";
            String entryNumber = sequenceNumberDataMap.containsKey("entryNumber") ? (StringUtil.isNullOrEmpty(sequenceNumberDataMap.get("entryNumber").toString()) ? "" : sequenceNumberDataMap.get("entryNumber").toString()) : "";
            boolean isFromImport = sequenceNumberDataMap.containsKey("isFromImport") ? (StringUtil.isNullOrEmpty(sequenceNumberDataMap.get("isFromImport").toString()) ? false : Boolean.parseBoolean(sequenceNumberDataMap.get("isFromImport").toString())) : false;
            
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("moduleid", moduleid);
            map.put("companyid", companyid);
            String formatid = "";
            String formatName = "";
            int intPartValue = 0;
            int intStartFromValue = 0;
            boolean isSeqnum = false;
            boolean isvalidEntryNumber = true;
            
            //If any one add more date formats in UI of sequence format needs to add here as well
            Map<String,String> dataFormatMap = new HashMap<>();
            dataFormatMap.put("YYYY", "yyyy");
            dataFormatMap.put("YYYYMM", "yyyyMM");
            dataFormatMap.put("YYYYMMDD", "yyyyMMdd");
            dataFormatMap.put("YY", "yy");
            dataFormatMap.put("YYMM", "yyMM");
            dataFormatMap.put("YYMMDD", "yyMMdd");
            dataFormatMap.put("YYYY-", "yyyy-");
            dataFormatMap.put("YYYYMM-", "yyyyMM-");
            dataFormatMap.put("YYYYMMDD-", "yyyyMMdd-");
            dataFormatMap.put("YY-", "yy-");
            dataFormatMap.put("YYMM-", "yyMM-");
            dataFormatMap.put("YYMMDD-", "yyMMdd-");
            
            KwlReturnObject result = importDao.getSequenceFormat(map);
            List formatsList = result.getEntityList();
            for (int i=0; i<formatsList.size(); i++) {
                String selecteddateformat = "";
                String selectedSuffixdateformat = "";
                String selecteddateFormatAfterPrefix = "";
                boolean isDateBeforePrefix = false;
                boolean isDateAfterSuffix = false;
                boolean isDateAfterPrefix = false;
                Object[] format = (Object[]) formatsList.get(i);
                String id=format[0]==null?"":(String)format[0];
                formatName=format[1]==null?"":(String)format[1];
                String preffix=format[2]==null?"":(String)format[2];
                String suffix=format[3]==null?"":(String)format[3];
                isDateBeforePrefix = format[4]==null ? false : (format[4].toString().equalsIgnoreCase("T") ? true : false);
                if(isDateBeforePrefix){
                    selecteddateformat=format[5]==null?"":(String)format[5];
                }
                isDateAfterSuffix = format[6]==null ? false : (format[6].toString().equalsIgnoreCase("T") ? true : false);
                if(isDateAfterSuffix){
                    selectedSuffixdateformat=format[7]==null?"":(String)format[7];
                }
                isDateAfterPrefix = format[9]==null ? false : (format[9].toString().equalsIgnoreCase("T") ? true : false);
                if(isDateAfterPrefix){
                    selecteddateFormatAfterPrefix=format[10]==null?"":(String)format[10];
                }
                preffix = preffix.toLowerCase();
                suffix = suffix.toLowerCase();
                String lowerEntryNumber = entryNumber.toLowerCase();
                
                if ((isDateBeforePrefix || isDateAfterSuffix || isDateAfterPrefix)) {//if sequnece format have date
                    if (lowerEntryNumber.length() == (selecteddateformat.length() + formatName.length() + selectedSuffixdateformat.length())) { //when lenght of number as well as lenght of format with date matches
                        if (isDateBeforePrefix) {
                            String datePrefix = lowerEntryNumber.substring(0, selecteddateformat.length());
                            DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                            if (dataFormatMap.containsKey(selecteddateformat.toUpperCase())) {
                                sdf = new SimpleDateFormat(dataFormatMap.get(selecteddateformat.toUpperCase()));
                            }
                            try {
                                sdf.setLenient(false);//make date validation more strictly.
                                sdf.parse(datePrefix);//If datePrefix is sucessfully parsed it means it is datevalue otherwise this number will not generate from this sequence format so continue
                            } catch (Exception ex) {
                                continue;
                            }
                            //If date is valid date then checking for year. If it is less than current year then such entry number should be acceptable
                            // because sequence number does not generate preveious year number
                            Date prefixdate = sdf.parse(datePrefix);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(prefixdate);
                            
                            Calendar todayCal = Calendar.getInstance();
                            todayCal.setTime(new Date());
                            
                            if (cal.get(Calendar.YEAR) < todayCal.get(Calendar.YEAR)) {
                                continue;// continue because this number can not be generated sequence format
                            }
                        }
                        if (isDateAfterPrefix) {
                            String dateAfterPrefix = lowerEntryNumber.substring((selecteddateformat.length() + preffix.length()), (selecteddateformat.length() + preffix.length() + selecteddateFormatAfterPrefix.length()));
                            DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                            if (dataFormatMap.containsKey(selecteddateFormatAfterPrefix.toUpperCase())) {
                                sdf = new SimpleDateFormat(dataFormatMap.get(selecteddateFormatAfterPrefix.toUpperCase()));
                            }
                            try {
                                sdf.setLenient(false);//make date validation more strictly.
                                sdf.parse(dateAfterPrefix);//If dateSuffix is sucessfully parsed it means it is datevalue otherwise entrynumber will not generate from this sequence format so continue
                            } catch (Exception ex) {
                                continue;
                            }
                            //If date is valid date then checking for year. If it is less than current year then such entry number should be acceptable
                            // because sequence number does not generate preveious year number
                            Date suffixdate = sdf.parse(dateAfterPrefix);
                            Calendar suffixCal = Calendar.getInstance();
                            suffixCal.setTime(suffixdate);

                            Calendar todayCal = Calendar.getInstance();
                            todayCal.setTime(new Date());

                            if (suffixCal.get(Calendar.YEAR) < todayCal.get(Calendar.YEAR)) {
                                continue; // continue because this number can not be generated by sequence format
                            }
                        }
                        if (isDateAfterSuffix) {
                            String dateSuffix = lowerEntryNumber.substring((selecteddateformat.length() + formatName.length()), lowerEntryNumber.length());
                            DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                            if (dataFormatMap.containsKey(selectedSuffixdateformat.toUpperCase())) {
                                sdf = new SimpleDateFormat(dataFormatMap.get(selectedSuffixdateformat.toUpperCase()));
                            }
                            try {
                                sdf.setLenient(false);//make date validation more strictly.
                                sdf.parse(dateSuffix);//If dateSuffix is sucessfully parsed it means it is datevalue otherwise entrynumber will not generate from this sequence format so continue
                            } catch (Exception ex) {
                                continue;
                            }
                            //If date is valid date then checking for year. If it is less than current year then such entry number should be acceptable
                            // because sequence number does not generate preveious year number
                            Date suffixdate = sdf.parse(dateSuffix);
                            Calendar suffixCal = Calendar.getInstance();
                            suffixCal.setTime(suffixdate);

                            Calendar todayCal = Calendar.getInstance();
                            todayCal.setTime(new Date());

                            if (suffixCal.get(Calendar.YEAR) < todayCal.get(Calendar.YEAR)) {
                                continue; // continue because this number can not be generated by sequence format
                            }
                        }
                        String lowerEntryNumberWithoutDate = lowerEntryNumber.substring(selecteddateformat.length(), (lowerEntryNumber.length() - selectedSuffixdateformat.length()));//removed prefix and suffix date
                        if (lowerEntryNumberWithoutDate.length() == formatName.length() && lowerEntryNumberWithoutDate.startsWith(preffix) && lowerEntryNumberWithoutDate.endsWith(suffix)) {
                            String intPart = lowerEntryNumberWithoutDate.substring((preffix.length() + selecteddateFormatAfterPrefix.length()), (lowerEntryNumberWithoutDate.length() - suffix.length()));
                            try {
                                intPartValue = Integer.parseInt(intPart);
                            } catch (Exception ex) {
                                continue;
                            }
                            formatid = id;
                            intStartFromValue=format[8]==null?0:(Integer)format[8];
                            isSeqnum = true;
                            formatName = selecteddateformat + formatName + selectedSuffixdateformat;
                            break;//once the sequnce format found no need to chek for other sequnece format
                        }
                    }
                } else {
                    if (lowerEntryNumber.length() == formatName.length() && lowerEntryNumber.startsWith(preffix) && lowerEntryNumber.endsWith(suffix)) {
                        String intPart = entryNumber.substring(preffix.length(), (entryNumber.length() - suffix.length()));
                        try {
                            intPartValue = Integer.parseInt(intPart);
                        } catch (Exception ex) {
                            continue;
                        }
                        formatid = id;
                        intStartFromValue=format[8]==null?0:(Integer)format[8];
                        isSeqnum = true;
                        break;
                    }
                }
            }
            if (isSeqnum) {
                String sqltable = "";
                switch (moduleid) {
                    case Constants.Acc_GENERAL_LEDGER_ModuleId:
                        sqltable = "journalentry";
                        break;
                    case Constants.Acc_Sales_Order_ModuleId:
                        sqltable = "salesorder";
                        break;
                    case Constants.Acc_Invoice_ModuleId:
                        sqltable = "invoice";
                        break;
                    case Constants.Acc_Cash_Sales_ModuleId:
                        sqltable = "invoice";
                        break;
                    case Constants.Acc_Credit_Note_ModuleId:
                        sqltable = "creditnote";
                        break;
                    case Constants.Acc_Receive_Payment_ModuleId:
                        sqltable = "receipt";
                        break;
                    case Constants.Acc_Purchase_Order_ModuleId:
                        sqltable = "purchaseorder";
                        break;
                    case Constants.Acc_Vendor_Invoice_ModuleId:
                        sqltable = "goodsreceipt";
                        break;
                    case Constants.Acc_Cash_Purchase_ModuleId:
                        sqltable = "goodsreceipt";
                        break;
                    case Constants.Acc_Debit_Note_ModuleId:
                        sqltable = "debitnote";
                        break;
                    case Constants.Acc_Make_Payment_ModuleId:
                        sqltable = "payment";
                        break;
                    case Constants.Acc_Contract_Order_ModuleId:
                        sqltable = "contract";
                        break;
                    case Constants.Acc_Customer_Quotation_ModuleId:
                        sqltable = "quotation";
                        break;
                    case Constants.Acc_Vendor_Quotation_ModuleId:
                        sqltable = "vendorquotation";
                        break;
                    case Constants.Acc_Purchase_Requisition_ModuleId:
                        sqltable = "purchaserequisition";
                        break;
                    case Constants.Acc_RFQ_ModuleId:
                        sqltable = "requestforquotation";
                        break;
                    case Constants.Acc_Product_Master_ModuleId:
                        sqltable = "product";
                        break;
                    case Constants.Acc_Delivery_Order_ModuleId:
                        sqltable = "deliveryorder";
                        break;
                    case Constants.Acc_Goods_Receipt_ModuleId:
                        sqltable = "grorder";
                        break;
                    case Constants.Acc_Sales_Return_ModuleId:
                        sqltable = "salesreturn";
                        break;
                    case Constants.Acc_Purchase_Return_ModuleId:
                        sqltable = "purchasereturn";
                        break;
                    case Constants.Acc_Customer_ModuleId:
                        sqltable = "customer";
                        break;
                    case Constants.Acc_Vendor_ModuleId:
                        sqltable = "vendor";
                        break;
                    case Constants.Acc_FixedAssets_AssetsGroups_ModuleId:
                        sqltable = "product";
                        break;
                }
                if(!isFromImport){
                    int maxseqnum = intStartFromValue-1;// Initialize with start from number to check sequence no. can be generated from available sequence format or not.
                    HashMap<String, Object> hmap = new HashMap<String, Object>();
                    hmap.put("companyid", companyid);
                    hmap.put("formatid", formatid);
                    hmap.put("sqltablename", sqltable);
                    KwlReturnObject result1 = importDao.getMaxSequenceFormatNUmber(hmap);
                    List list = result1.getEntityList();
                    if (!list.isEmpty() && list.get(0) != null) {
                        maxseqnum = Integer.parseInt(list.get(0).toString());
                    }
                    if (intPartValue > maxseqnum) {// user entered number can also be generated by sequence number
                        isvalidEntryNumber = false;
                    }
                } else{
                    isvalidEntryNumber = false;
                }
            }
            ll.add(isvalidEntryNumber);
            ll.add(formatName);
            ll.add(formatid);
            ll.add(intPartValue);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.checksEntryNumberForSequenceNumber : " + ex.getMessage(), ex);
        }
        return ll;
    }
    
    public JSONObject importBankReconciliationCSV(HashMap<String, Object> requestParams1) throws IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        CsvReader csvReader = null;
        int total = 0, failed = 0;
        String companyid = requestParams1.get("companyid").toString();
        String userId = requestParams1.get("userid").toString();
        //DateFormat df = (DateFormat) requestParams1.get("dateFormat");
        JSONObject jobj = new JSONObject();
        jobj = (JSONObject) requestParams1.get("jobj");
        String masterPreference = requestParams1.get("masterPreference").toString();
        String delimiterType = requestParams1.get("delimiterType").toString();
        int limit = Constants.Transaction_Commit_Limit;
        int count = 1;
        String fileName = jobj.getString("filename");

        JSONObject returnObj = new JSONObject();
        String failureMsg = "";
        String logId = null;

        JSONArray DataJArr = new JSONArray();
        try {
            logId = addPendingImportLog(requestParams1);

            String dateFormat = null, dateFormatId = requestParams1.get("dateFormat").toString();
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KWLDateFormat kdf = (KWLDateFormat) KwlCommonTablesDAOObj.getClassObject(KWLDateFormat.class.getName(), dateFormatId);
                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);

            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
            int cnt = 0;

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

            while (csvReader.readRecord()) {
                failureMsg = "";
                String[] recarr = csvReader.getValues();
                Map<Integer, Object> invalidColumn = new HashMap<>();
                if (cnt == 0) {
                    List failureRecArr = new ArrayList();
                    for (int header = 0; header < recarr.length; header++) {
                        failureRecArr.add(recarr[header]);
                    }
                    failureRecArr.add("Error Message");
                    failureArr.add(failureRecArr);
                    failureColumnArr.add(invalidColumn);
                }
                if (cnt != 0) {
                    try {

                        Date date = null;
                        if (columnConfig.containsKey("date")) {//when AsofDate header is mapped
                            String dateStr = recarr[(Integer) columnConfig.get("date")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(dateStr)) {
                                try {
                                    df.setLenient(false);
                                    date = df.parse(dateStr);
                                } catch (Exception ex) {
                                    invalidColumn.put((Integer) columnConfig.get("date"), "Invalid");
                                    failureMsg += "Incorrect date format for Date, Please specify values in " + dateFormat + " format.";
                                    throw new DataInvalidateException("Incorrect date format for Date, Please specify values in " + dateFormat + " format.");
                                }
                            }
                        } else {//when value is null or empty
                            invalidColumn.put((Integer) columnConfig.get("date"), "Invalid");
                            failureMsg += "Empty data found in Date, cannot set empty data for Date.";
                        }


                        String amount = "0";
                        if (columnConfig.containsKey("amount")) {
                            amount = recarr[(Integer) columnConfig.get("amount")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(amount)) { //when value is null or empty
                                invalidColumn.put((Integer) columnConfig.get("amount"), "Invalid");
                                failureMsg += "Empty data found in Amount, cannot set empty data for Amount.";
                            } else {
                                try {
                                    Object vDataValue = Double.parseDouble(amount);
                                } catch (Exception ex) {
                                    invalidColumn.put((Integer) columnConfig.get("amount"), "Invalid");
                                    failureMsg += "Incorrect numeric value for Amount, Please ensure that value type of Amount matches with the Amount.";
                                    throw new DataInvalidateException("Incorrect numeric value for Amount, Please ensure that value type of Amount matches with the Amount.");
                                }
                            }
                        }

                        String accountnames = "";
                        if (columnConfig.containsKey("accountnames")) {
                            accountnames = recarr[(Integer) columnConfig.get("accountnames")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(accountnames)) {
                                invalidColumn.put((Integer) columnConfig.get("accountnames"), "Invalid");
                                failureMsg += "Payee is not available.";
                            }
                            JSONObject configObj = configMap.get("accountnames");
                            int maxLength = configObj.optInt("maxLength", 0);
                            String validationType = configObj.optString("validatetype");
                            if ("string".equalsIgnoreCase(validationType) && !StringUtil.isNullOrEmpty(accountnames) && accountnames.length() > maxLength) {
                                if (masterPreference.equals(0)) {
                                    invalidColumn.put((Integer) columnConfig.get("accountnames"), "Invalid");
                                    failureMsg += "Data length greater than " + maxLength + " for column Payee.";
                                } else {// for other two cases need to trim data upto max length
                                    accountnames = accountnames.substring(0, maxLength);
                                }
                            }
                            boolean validData = validatePayee(companyid, accountnames);
                            if (!validData) {
                                failureMsg += "Payee is not Present in System.";
                            }
                        } else {
                            failureMsg += "Payee column is not found.";
                        }


                        String desc = "";
                        if (columnConfig.containsKey("transactionID")) {
                            String temp = recarr[(Integer) columnConfig.get("transactionID")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                desc = temp;
                            }
                        }

                        String reference = "";
                        if (columnConfig.containsKey("reference")) {
                            String temp = recarr[(Integer) columnConfig.get("reference")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                reference = temp;
                            }
                        }

                        String chequeno = "";
                        if (columnConfig.containsKey("chequeno")) {
                            String temp = recarr[(Integer) columnConfig.get("chequeno")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                chequeno = temp;
                            }
                        }

                        JSONObject obj = new JSONObject();
                        obj.put("date", df.format(date));
                        obj.put("amount", amount);
                        obj.put("payee", accountnames);
                        obj.put("desc", desc);
                        obj.put("reference", reference);
                        obj.put("chequenumber", chequeno);
                        DataJArr.put(obj);

                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        List failureRecArr = new ArrayList();
                        for (int cellData = 0; cellData < recarr.length; cellData++) {
                            failureRecArr.add(recarr[cellData]);
                        }
                        failureRecArr.add(errorMsg.replaceAll("\"", ""));
                        failureArr.add(failureRecArr);

                        failureColumnArr.add(invalidColumn);
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
                importDao.createFailureXlsFiles(fileName, failureArr, ".xls", failureColumnArr);
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

            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
                logDataMap.put("FileName", ImportLog.getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed > 0 ? "xls" : "");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Bank_Reconciliation_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                logDataMap.put("Id", logId);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("data", DataJArr);
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", ImportLog.getActualFileName(fileName));
                returnObj.put("Module", Constants.Bank_Reconciliation_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    /*
     * public void validateAssemblyProduct() used to validate Assembly Product related records
     * ERP-26471 : Validate - Single Assembly Product do not have same Product in BOM.
     * ERP-26481 : Validate - Serial activated BOM product should have BOM Percetage = 100
     */
    public void validateAssemblyProduct(HashSet<String> assemblyset, HashMap<String, Object> dataMap, Locale locale, boolean isActivateMRPModule) throws ServiceException, DataInvalidateException {
        if (dataMap.containsKey("BOMProductid") && dataMap.get("BOMProductid") != null) {    //ERP-26481
            String assemblyproduct = dataMap.get("Productid").toString();
            /*
            * SDP-14522 - Check BOM Code as mandatory column if MRP is activated for the subdomain
            */
            String bomCode = "";
            if(isActivateMRPModule ){
                if(dataMap.containsKey("BOMCode")){
                    if(dataMap.get("BOMCode") != null && !StringUtil.isNullOrEmpty(dataMap.get("BOMCode").toString())){
                        bomCode = dataMap.get("BOMCode").toString();
                    } else {
                        throw new DataInvalidateException("BOM Code is not available.");
                    }
                } else {
                    throw new DataInvalidateException("BOM Code column is not found.");
                }
            }
            String bomproductcode = dataMap.get("BOMProductid").toString();
            String company = dataMap.containsKey("Company") ? dataMap.get("Company").toString() : "";
            
            if(dataMap.containsKey("Producttype") && dataMap.get("Producttype") != null && (!(dataMap.get("Producttype").toString().equalsIgnoreCase("Inventory Assembly")) && !(dataMap.get("Producttype").toString().equalsIgnoreCase("Job Work Assembly")))){
                throw new DataInvalidateException(messageSource.getMessage("acc.assemblyproduct.validation.producttype", null, locale));
            }
            //To avoid duplicate record with respect to Assembly Product ID, BOM Code and BOM Product ID
            if (assemblyset.contains(assemblyproduct+bomCode+bomproductcode)) {
                throw new DataInvalidateException("Duplicate record found for Product ID, BOM Code and BOM Product ID.");
            } else {
                assemblyset.add(assemblyproduct+bomCode+bomproductcode);
            }

            //Validate - Serial activated BOM product should have BOM Percetage = 100
            if (!StringUtil.isNullOrEmpty(bomproductcode) && !StringUtil.isNullOrEmpty(company)) {
                boolean isSerialForProduct = false;
                KwlReturnObject returnObject = importDao.getProductByProductCode(company, bomproductcode);
                if (!returnObject.getEntityList().isEmpty()) {
                    Object[] bomobj = (Object[]) returnObject.getEntityList().get(0);
                    isSerialForProduct = (Boolean) bomobj[1];
                    if (isSerialForProduct) {
                        if (dataMap.containsKey("BOMPercent") && dataMap.get("BOMPercent") != null) {
                            String bompercent = dataMap.get("BOMPercent").toString();
                            if (!bompercent.equals("100")) {
                                throw new DataInvalidateException(messageSource.getMessage("acc.assemblyproduct.validation.serialactivated", null, locale) +" '"+ bomproductcode + "'.  "+messageSource.getMessage("acc.assemblyproduct.validation.bompercentage", null, locale));   
        }
    }
                    } else {
                        if (dataMap.containsKey("BOMPercent") && dataMap.get("BOMPercent") != null) {
                            double bompercent = !StringUtil.isNullOrEmpty(dataMap.get("BOMPercent").toString())?Double.parseDouble(dataMap.get("BOMPercent").toString()):0;
                            if (bompercent==0) {
                                throw new DataInvalidateException(messageSource.getMessage("acc.assemblyproduct.validation.invalid.bompercentage", null, locale));
                            } else if(bompercent>100){
                                throw new DataInvalidateException(messageSource.getMessage("acc.assemblyproduct.validation.bompercentage.range", null, locale));
}
                        }
                    }
                }
            }
    }
    }
    /**
     * Validate GST Details for Customer/ Vendor import (Fields - GSTIN Number/
     * Customer/Vendor Type, GST Registration type )
     * 
     * GSTReg. Type 	        GSTIN 	        Customer Type 
     * -----------------------------------------------------------------------------------
     * Composition 	        Required 	NA
     * Registered 	        Required 	NA  SEZ (WPAY),SEZ (WOPAY)
     * Unregistered 	        Not Required 	NA, Export (WPAY), Export (WOPAY)
     * 
     * GSTReg. Type 	        GSTIN 	        Vendor Type 	
     * -----------------------------------------------------------------------------------
     * Composition 	        Required 	NA
     * Registered 	        Required 	NA, SEZ (WPAY),SEZ (WOPAY)
     * Unregistered 	        Not Required 	NA, Import
     * 
     * 
     *
     * @param requestParams
     * @param GSTDetails
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject isCustomVendorGSTMasterDetailsValidForImport(HashMap<String, Object> requestParams, JSONObject GSTDetails) throws ServiceException, JSONException, ParseException {
        JSONObject validationDetails = new JSONObject();
        String mode = (String) requestParams.get("modName");
        String validationMSG = "";
        Locale locale = new Locale("en", "US");   //Default value English
        if (requestParams.containsKey("locale") && requestParams.get("locale") != null) {
            locale = (Locale) requestParams.get("locale");
        }
        String companyid = (String) requestParams.get("companyid");
        // Data key GSTCustomerVendorType , GSTIN , GSTRegistrationType
        String GSTIN_Number = GSTDetails.optString("GSTIN", "").replaceAll("\"", "").trim();
        String GST_Registration = GSTDetails.optString("GSTRegistrationType", "").replaceAll("\"", "").trim();
        String GST_CustVendType = GSTDetails.optString("GSTCustomerVendorType", "").replaceAll("\"", "").trim();
        String csvHeader = GSTDetails.optString("csvHeader", "").replaceAll("\"", "").trim();
        String columnHeader = GSTDetails.optString("columnHeader", "").replaceAll("\"", "").trim();
        String column = GSTDetails.optString("column", "").replaceAll("\"", "").trim();
        if ((StringUtil.isNullOrEmpty(GST_CustVendType) && (column.equalsIgnoreCase("GSTCustomerType") || column.equalsIgnoreCase("GSTVendorType"))) || (StringUtil.isNullOrEmpty(GST_Registration) && column.equalsIgnoreCase("GSTRegistrationType"))) {
            validationMSG = messageSource.getMessage("acc.export.Emptydatafoundin", null, locale) + " " + csvHeader + ", " + messageSource.getMessage("acc.export.cannotsetemptydatafor", null, locale) + " " + columnHeader + ".";
        } else {
            KwlReturnObject returnObject = importDao.getMasterItemDetails(companyid, GST_Registration, "62");
            String defaultGST_RegTypeID = "";
            String GST_RegTypeID = "";
            if (returnObject != null && returnObject.getEntityList() != null && !returnObject.getEntityList().isEmpty()) {
                Object data[] = (Object[]) returnObject.getEntityList().get(0);
                GST_RegTypeID = data.length >= 2 && data[0] != null ? (String) data[0] : "";
                defaultGST_RegTypeID = data.length >= 2 && data[1] != null ? (String) data[1] : "";
            }
            String defaultGST_CustVendID = "";
            String GST_CustVendID = "";
            returnObject = importDao.getMasterItemDetails(companyid, GST_CustVendType, "63");
            if (returnObject != null && returnObject.getEntityList() != null && !returnObject.getEntityList().isEmpty()) {
                Object data[] = (Object[]) returnObject.getEntityList().get(0);
                GST_CustVendID = data.length >= 2 && data[0] != null ? (String) data[0] : "";
                defaultGST_CustVendID = data.length >= 2 && data[1] != null ? (String) data[1] : "";
            }
            if ((StringUtil.isNullOrEmpty(GST_RegTypeID) && column.equalsIgnoreCase("GSTRegistrationType")) || (StringUtil.isNullOrEmpty(GST_CustVendID) && (column.equalsIgnoreCase("GSTCustomerType") || column.equalsIgnoreCase("GSTVendorType")))) {
                if (column.equalsIgnoreCase("GSTRegistrationType")) {
                    validationMSG = GST_Registration + " entry not found in master list for " + columnHeader + " dropdown.";
                } else if (column.equalsIgnoreCase("GSTCustomerType") || column.equalsIgnoreCase("GSTVendorType")) {
                    validationMSG = GST_CustVendType + " entry not found in master list for " + columnHeader + " dropdown.";
                }

            } else {
                if (mode.equalsIgnoreCase("Customer")) {
                    if (GSTDetails.has("custVenProdID") && column.equalsIgnoreCase("gstapplicabledate") && !StringUtil.isNullOrEmpty(GSTDetails.optString("custVenProdID",""))) {
                            DateFormat df = null;
                        try {
                            df = authHandler.getOnlyDateFormat();
                        } catch (SessionExpiredException ex) {
                            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                            Map<String, Object> reqMap = new HashMap();
                            if (!StringUtil.isNullOrEmpty(companyid)) {
                                reqMap.put("companyid", companyid);
                            }
                            if (!StringUtil.isNullOrEmpty(GSTDetails.optString("custVenProdID"))) {
                                reqMap.put("customerid", GSTDetails.optString("custVenProdID"));
                            }
                            if (!StringUtil.isNullOrEmpty(GSTDetails.optString("gstapplicabledate"))) {
                                reqMap.put("applyDate", df.parse(GSTDetails.optString("gstapplicabledate")));
                            }
                            List result = importDao.getGstCustomerUsedHistory(reqMap);
                            if (result!=null && !result.isEmpty()) {
                                validationMSG = messageSource.getMessage("acc.import.invalid.customer", null, locale); 
                            } 
                        }
                    
                    if (defaultGST_RegTypeID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Regular))
                            || defaultGST_RegTypeID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Composition))) {
                        if (StringUtil.isNullOrEmpty(GSTIN_Number) && column.equalsIgnoreCase("GSTIN")) {
                            validationMSG = messageSource.getMessage("acc.export.Emptydatafoundin", null, locale) + " " + csvHeader + ", " + messageSource.getMessage("acc.export.cannotsetemptydatafor", null, locale) + " " + columnHeader + ".";
                        }                         
//                        else if(!(StringUtil.isGSTINValid(GSTIN_Number)) && column.equalsIgnoreCase("GSTIN")){
//                            validationMSG = messageSource.getMessage("acc.import.invalid.records", null, locale) + " " + csvHeader + ".";
//                        }
                        /**
                         * Validate GST Registration for Vendor if
                         * type Composition
                         */
                        if (column.equalsIgnoreCase("GSTCustomerType") && defaultGST_RegTypeID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Composition))
                                && !(defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA)))) {
                            validationMSG = messageSource.getMessage("acc.india.vecdorcustomer.column.type.import", new Object[]{csvHeader, IndiaComplianceConstants.GST_CUSTOMER_VENDORTYPEID_NA}, null, locale);
                        }//
                                             /**
                         * Validate GST Registration for Customer if
                         * type Registered
                         */
                        if (column.equalsIgnoreCase("GSTCustomerType") && defaultGST_RegTypeID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Regular))
                                && !(defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA))
                                || defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZ))
                                || defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZWOPAY)))) {
                            validationMSG = messageSource.getMessage("acc.india.vecdorcustomer.column.type.import", new Object[]{csvHeader, IndiaComplianceConstants.GST_CUSTOMER_VENDORTYPEID_NA_SEZ}, null, locale);
                        }//
                    }else if (defaultGST_RegTypeID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered))) {
                        if (column.equalsIgnoreCase("GSTCustomerType")
                                && !(defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA))
                                || defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Export))
                                || defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_ExportWOPAY)))) {
                            validationMSG = messageSource.getMessage("acc.india.vecdorcustomer.column.type.import", new Object[]{csvHeader, IndiaComplianceConstants.GST_CUSTOMER_UNREGISTERED_TYPEID}, null, locale);
                        }
                    }else if(column.equalsIgnoreCase("GSTRegistrationType")){
                        validationMSG = messageSource.getMessage("acc.india.vecdorcustomer.column.type.import", new Object[]{csvHeader, IndiaComplianceConstants.GST_REGISTRATION_TYPE}, null, locale);
                    }                   
                } else if (mode.equalsIgnoreCase("Vendor")) {
                     if (GSTDetails.has("custVenProdID") && column.equalsIgnoreCase("gstapplicabledate") && !StringUtil.isNullOrEmpty(GSTDetails.optString("custVenProdID",""))) {
                            DateFormat df = null;
                        try {
                            df = authHandler.getOnlyDateFormat();
                        } catch (SessionExpiredException ex) {
                            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                            Map<String, Object> reqMap = new HashMap();
                            if (!StringUtil.isNullOrEmpty(companyid)) {
                                reqMap.put("companyid", companyid);
                            }
                            if (!StringUtil.isNullOrEmpty(GSTDetails.optString("custVenProdID"))) {
                                reqMap.put("vendorid", GSTDetails.optString("custVenProdID"));
                            }
                            if (!StringUtil.isNullOrEmpty(GSTDetails.optString("gstapplicabledate"))) {
                                reqMap.put("applyDate", df.parse(GSTDetails.optString("gstapplicabledate")));
                            }
                            List result = importDao.getGstVendorUsedHistory(reqMap);
                            if (result!=null && !result.isEmpty()) {
                                validationMSG = messageSource.getMessage("acc.import.invalid.customer", null, locale); 
                            }
                        }
                    if (defaultGST_RegTypeID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Regular))
                            || defaultGST_RegTypeID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Composition))) {
                        if (StringUtil.isNullOrEmpty(GSTIN_Number) && column.equalsIgnoreCase("GSTIN")) {
                            validationMSG = messageSource.getMessage("acc.export.Emptydatafoundin", null, locale) + " " + csvHeader + ", " + messageSource.getMessage("acc.export.cannotsetemptydatafor", null, locale) + " " + columnHeader + ".";
                        }
//                        else if(!(StringUtil.isGSTINValid(GSTIN_Number)) && column.equalsIgnoreCase("GSTIN")){
//                            validationMSG = messageSource.getMessage("acc.import.invalid.records", null, locale) + " " + csvHeader + ".";
//                        }
                        /**
                         * Validate GST Registration for Vendor if
                         * type Composition
                         */
                        if (column.equalsIgnoreCase("GSTVendorType") && defaultGST_RegTypeID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Composition))
                                && !(defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA)))) {
                            validationMSG = messageSource.getMessage("acc.india.vecdorcustomer.column.type.import", new Object[]{csvHeader, IndiaComplianceConstants.GST_CUSTOMER_VENDORTYPEID_NA}, null, locale);
                        }
                        /**
                         * Validate GST Registration for Vendor if
                         * type Registered
                         */
                        if (column.equalsIgnoreCase("GSTVendorType") && defaultGST_RegTypeID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Regular))
                                && !(defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA))
                                || defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZ)))) {
                            validationMSG = messageSource.getMessage("acc.india.vecdorcustomer.column.type.import", new Object[]{csvHeader, IndiaComplianceConstants.GST_VENDORTYPEID_NA_SEZWPAY}, null, locale);
                        }
                    }else if (defaultGST_RegTypeID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered))) {
                        if (column.equalsIgnoreCase("GSTVendorType")
                                && !(defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA))
                                || defaultGST_CustVendID.equalsIgnoreCase(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Import)))) {
                            validationMSG = messageSource.getMessage("acc.india.vecdorcustomer.column.type.import", new Object[]{csvHeader, IndiaComplianceConstants.GST_VENDOR_UNREGISTERED_TYPEID}, null, locale);
                        }
                    }else if(column.equalsIgnoreCase("GSTRegistrationType")){
                        validationMSG = messageSource.getMessage("acc.india.vecdorcustomer.column.type.import", new Object[]{csvHeader, IndiaComplianceConstants.GST_REGISTRATION_TYPE}, null, locale);
                    }
                }
            }
        }
        validationDetails.put("validationMSG", validationMSG);
        return validationDetails;
    }
    /**
     * To check Cheque Number contains special characters or not
     * To check Cheque Number length Greater than limit.
     * @param chequeNumber
     * @param isFromValidation
     * @return 
     */
    public String isChequeNumberValidForImport(String chequeNumber,boolean isFromValidation) {
        String failureMsg="";
        Pattern pattern = Pattern.compile(Constants.CHEQUE_NUMBER_ALLOWED_LETTERS);
        Matcher matcher = pattern.matcher(chequeNumber);
        if (!matcher.matches()) {
            failureMsg += "Special characters are not allowed in Cheque number.";
            if(isFromValidation){
                failureMsg +=" <br/>";
            } else {
                failureMsg +=" \n";
            }
        }
        if (chequeNumber.length() > Constants.CHEQUE_NUMBER_DIGIT_LIMIT) {
            failureMsg += "Cheque number should not be more than 16 characters.";
            if(isFromValidation){
                failureMsg +=" <br/>";
            } else {
                failureMsg +=" \n";
            }
        }
        return  failureMsg;
    }
    
    public ArrayList getTax(Map taxMap) throws ServiceException {
        ArrayList taxDataArray = new ArrayList();
        String failureMsg = "";
        try {
            Object taxObj = null;
            String taxId = "";
            KwlReturnObject result = importDao.getTax(taxMap);
            if (!result.getEntityList().isEmpty() && result.getEntityList() != null) {
                List<Object[]> list = result.getEntityList();
                for (Object[] obj : list) {
                    if (obj[2] != null && (boolean) obj[2]) {//Tax "activated" at 2nd position.
                        taxObj = obj[0];//Tax Obj at 0th position.
                        taxId = (String) obj[1];//Tax "ID" at 1st position.
                        break;
                    }
                }
                if (taxObj == null) {
                    failureMsg += "Tax Code is deactivated ";
                }
            } else {
                failureMsg += "Tax Code is not found for code ";
            }
            taxDataArray.add(taxObj);
            taxDataArray.add(taxId);
            taxDataArray.add(failureMsg);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportHandler.getTax", ex);
        }
        return taxDataArray;
    }
}
