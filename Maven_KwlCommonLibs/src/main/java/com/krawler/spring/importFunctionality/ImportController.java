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

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ImportLog;
import com.krawler.common.admin.Modules;
import com.krawler.common.util.Constants;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import javax.servlet.ServletContext;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;

public class ImportController extends MultiActionController {

    private ImportDAO importDao;
    public ImportHandler importHandler;
    private MessageSource messageSource;

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    public ModelAndView getColumnConfig(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String moduleName = request.getParameter("module");
            boolean isExport = false; 
            
            boolean fetchCustomFields=false, isBomlessFile = false;
            boolean isdocumentimportFlag=false;
            boolean updateExistingRecordFlag=false;
            boolean isExpenseInvoiceImport=false;
            boolean incash=false;
            if(!StringUtil.isNullOrEmpty(request.getParameter("fetchCustomFields"))){
               fetchCustomFields= Boolean.parseBoolean(request.getParameter("fetchCustomFields"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("bomlessfile"))){     //In Column Mapping UI, BOM Column fields will be non-mandatory if this check is true.
               isBomlessFile = Boolean.parseBoolean(request.getParameter("bomlessfile"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isdocumentimportFlag"))){
               isdocumentimportFlag= Boolean.parseBoolean(request.getParameter("isdocumentimportFlag"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("updateExistingRecordFlag"))){
               updateExistingRecordFlag= Boolean.parseBoolean(request.getParameter("updateExistingRecordFlag"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isExport"))){
               isExport= Boolean.parseBoolean(request.getParameter("isExport"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isExpenseInvoiceImport"))){
               isExpenseInvoiceImport= Boolean.parseBoolean(request.getParameter("isExpenseInvoiceImport"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("incash"))){
               incash= Boolean.parseBoolean(request.getParameter("incash"));
            }
            int subModuleFlag = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subModuleFlag"))) {
                subModuleFlag = Integer.parseInt(request.getParameter("subModuleFlag"));
            }
            
            if (moduleName.equalsIgnoreCase("Cash Sales")) {
                moduleName = "Customer Invoices";
            } else if (moduleName.equalsIgnoreCase("Cash Purchase")) {
                moduleName = "Vendor Invoice";
            }

            String ModuleId = null;
            String companyId = sessionHandlerImpl.getCompanyid(request);
            try {
                List list = importDao.getModuleObject(moduleName);
                Modules module = (Modules) list.get(0); //Will throw null pointer if no module entry found
                ModuleId = module.getId();
            } catch (Exception ex) {
                throw new DataInvalidateException("Column config entries are not available for " + moduleName + " module.");
            }
            String isdocumentimport="F";
            if(isdocumentimportFlag){
                isdocumentimport="T";
            }
            //Replaced the multiple arguments of getModuleColumnConfig() with single HashMap object
            HashMap<String, Object> params = new HashMap<String, Object> ();
            params.put("moduleId", ModuleId);
            params.put("companyid", companyId);
            params.put("isdocumentimport", isdocumentimport);
            params.put("subModuleFlag", new Integer(subModuleFlag));
            params.put("isBomlessFile", isBomlessFile);
            params.put("updateExistingRecordFlag", updateExistingRecordFlag);
            params.put("isExpenseInvoiceImport", isExpenseInvoiceImport);
            params.put("incash", incash);
            JSONArray DataJArr = importHandler.getModuleColumnConfig(params);
	    if (moduleName.equalsIgnoreCase("Assembly Product")) {    //This check added becasue Assembly Product have different module id but in actual Assembly Product is a part of Product Module.
                ModuleId = String.valueOf(Constants.Acc_Product_Master_ModuleId); // To get Custom fields associated to Product we need to send Product module id instead of Assembly Product.                
            }
            if (fetchCustomFields) {
                JSONArray CustomDataJArr = importHandler.getCustomModuleColumnConfig(ModuleId, companyId,isExport,params);
                for (int i = 0; i < CustomDataJArr.length(); i++) {
                    JSONObject jSONObject = CustomDataJArr.getJSONObject(i);
                    DataJArr.put(jSONObject);
                }                
                /*
                   * Getting Custom Fields of product which are marked as sales order custom fields  
                */
                if (ModuleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                    ModuleId = String.valueOf(Constants.Acc_Product_Master_ModuleId);
                    CustomDataJArr = importHandler.getCustomModuleColumnConfig(ModuleId, companyId, isExport, params);
                    for (int i = 0; i < CustomDataJArr.length(); i++) {
                        JSONObject jSONObject = CustomDataJArr.getJSONObject(i);
                        String relatedmoduleid = jSONObject.optString("relatedmoduleid");
                        if (!StringUtil.isNullOrEmpty(relatedmoduleid)){
                            String relatedmoduleArr[] = relatedmoduleid.split(",");
                            for (int j = 0; j < relatedmoduleArr.length; j++) {
                                if (relatedmoduleArr[j].equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                                    DataJArr.put(jSONObject);
                                }
                            }
                        }
                    }
                }
            }
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView importRecords(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String View = "jsonView";
        String msg = "";
        try {
            boolean isBomlessFile = !StringUtil.isNullOrEmpty(request.getParameter("isBomlessFile")) ? Boolean.parseBoolean(request.getParameter("isBomlessFile")) : false;
            String doAction = request.getParameter("do");
            String fromdocument = request.getParameter("fromdocument");
            System.out.println("A(( " + doAction + " start : " + new Date());
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String type = request.getParameter("type");
            if (doAction.compareToIgnoreCase("import") == 0 || doAction.compareToIgnoreCase("validateData") == 0) {
                HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
                String eParams = request.getParameter("extraParams");
                JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
                requestParams.put("extraParams", extraParams);
                requestParams.put("extraObj", null);
                requestParams.put("servletContext", this.getServletContext());
                requestParams.put("isBomlessFile", isBomlessFile);  //To do validation for WithoutBOM file & withBOM File.
                if (doAction.compareToIgnoreCase("import") == 0) {
                    System.out.println("A(( Import start : " + new Date());
                    String exceededLimit = request.getParameter("exceededLimit");
                    if (exceededLimit.equalsIgnoreCase("yes")) { //If file contains records more than 1500 then Import file in background using thread
                        String logId = importHandler.addPendingImportLog(requestParams);
                        requestParams.put("logId", logId);
                        importHandler.add(requestParams);
                        if (!importHandler.isIsWorking()) {
                            Thread t = new Thread(importHandler);
                            t.start();
                        }
                        jobj.put("success", true);
                    } else {
                        jobj = importHandler.importFileData(requestParams);
                    }
                    jobj.put("exceededLimit", exceededLimit);
                    System.out.println("A(( Import end : " + new Date());
                } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                    jobj = importHandler.validateFileData(requestParams);
                    jobj.put("success", true);
                }
            } else if (doAction.compareToIgnoreCase("getMapCSV") == 0) {
                if (StringUtil.isNullOrEmpty(fromdocument)) {
                    jobj = importHandler.getMappingCSVHeader(request);
                } else {
                    jobj = importHandler.getMappingCSVHeaderForDocument(request);
                }
                View = "jsonView_ex";                
                if(jobj.has("isInvalidFileForWithoutBOM") && jobj.get("isInvalidFileForWithoutBOM")!=null && jobj.getBoolean("isInvalidFileForWithoutBOM")){
                    throw new DataInvalidateException("This is a Assembly Product without BOM. So import the file without BOM details.");
                }
                // Dump csv data in DB
                String filename = jobj.getString("name");
                int headerCount = jobj.optInt("headerCount",0);
//                String actualfilename = ImportLog.getActualFileName(filename);
                String tableName = importDao.getTableName(filename);
                if (tableName.length() > 64) { // To fixed Mysql ERROR 1103 (42000): Incorrect table name
                    throw new DataInvalidateException(messageSource.getMessage("acc.import.limit.filename", null, RequestContextUtils.getLocale(request)));
                }
                // ERP-36168 - commenting below code to revert action for ERP-19144 and allow csv file with single column to map column screen.
//                if (headerCount==1) { // means file is not read with valid delimiter.
//                    throw new DataInvalidateException(messageSource.getMessage("acc.import.upload.valid.delimiter", null, RequestContextUtils.getLocale(request)));
//                }
                importDao.createFileTable(tableName, jobj.getInt("cols"));
                int totalRecsInFile = importHandler.dumpCSVFileData(filename, jobj.getString("delimiterType"), 1, fromdocument);
                totalRecsInFile=totalRecsInFile-1;//subtracting by one because header get count as record
                int maxAllowedRec=Constants.MAXIMUM_ALLOWED_RECORDS_FOR_IMPORT;
                if(!StringUtil.isNullOrEmpty(type) && (Constants.IMPORT_TIME_10000_MOD_LIST.contains(type))){
                    maxAllowedRec= Constants.MAXIMUM_ALLOWED_RECORDS_FOR_IMPORT_10000;
                }
                if(totalRecsInFile > maxAllowedRec){
                    throw new DataInvalidateException("File contains more than "+maxAllowedRec+" records. Please upload file with "+maxAllowedRec+" records only.");
                }
//                importDao.makeUploadedFileEntry(filename, actualfilename, tableName, companyId);
            } else if (doAction.compareToIgnoreCase("getXLSData") == 0) {
                try {
                    String filename = request.getParameter("filename");
                    int sheetNo = Integer.parseInt(request.getParameter("index"));
                    jobj = importHandler.parseXLS(filename, sheetNo);
                    int lastRowNum = jobj.optInt("maxrow");
                    int maxAllowedRec=Constants.MAXIMUM_ALLOWED_RECORDS_FOR_IMPORT;
                    if (lastRowNum > maxAllowedRec) {
                        throw new DataInvalidateException("File contains more than " + maxAllowedRec + " records. Please upload file with " + maxAllowedRec + " records only.");
                    }
                } catch (Exception e) {
                    Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, e);
                    try {
                        jobj.put("msg", e.getMessage());
                        jobj.put("lsuccess", false);
                        jobj.put("valid", true);
                    } catch (JSONException ex) {
                    }
                }
                View = "jsonView_ex";
            } else if (doAction.compareToIgnoreCase("dumpXLS") == 0) {
                int sheetNo = Integer.parseInt(request.getParameter("index"));
                int rowIndex = Integer.parseInt(request.getParameter("rowIndex"));
                int columns = Integer.parseInt(request.getParameter("totalColumns"));
                String filename = request.getParameter("onlyfilename");
//                String actualfilename = ImportLog.getActualFileName(filename);
                String tableName = importDao.getTableName(filename);
                importDao.createFileTable(tableName, columns);
                int totalRecsInFile = importHandler.dumpXLSFileData(filename, sheetNo, rowIndex);
                totalRecsInFile=totalRecsInFile-1;//subtracting by one because header get count as record
                int maxAllowedRec=Constants.MAXIMUM_ALLOWED_RECORDS_FOR_IMPORT;
                if(totalRecsInFile > maxAllowedRec){
                    throw new DataInvalidateException("File contains more than "+maxAllowedRec+" records. Please upload file with "+maxAllowedRec+" records only.");
                }
//                importDao.makeUploadedFileEntry(filename, actualfilename, tableName, companyId);
                jobj.put("success", true);
            }
            System.out.println("A(( " + doAction + " end : " + new Date());
        } catch (IOException ex) {
            try {
                jobj.put("success", false);
                msg = ex.getMessage();
                if(msg.contains("Too many open files")){
                    msg = "The server is processing too many requests at this time. Please try this again some time later";
                }
                jobj.put("msg", msg);
            } catch (JSONException ex1) {
                Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView(View, "model", jobj.toString());
    }

    public ModelAndView fileUploadXLS(HttpServletRequest request, HttpServletResponse response) {
        String View = "jsonView_ex";
        JSONObject jobj = new JSONObject();
        try {
            String xlsFileTmpPath = StorageHandler.GetDocStorePath();
            System.out.println("A(( Upload XLS start : " + new Date());
            jobj.put("success", true);
            FileItemFactory factory = new DiskFileItemFactory(4096, new File(System.getProperty("java.io.tmpdir")));  //"/tmp"));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(10000000);
            List fileItems = upload.parseRequest(request);
            Iterator i = fileItems.iterator();
            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "xlsfiles";
            String fileName = null;
            String fileid = UUID.randomUUID().toString();
            fileid = fileid.replaceAll("-", ""); // To append UUID without "-" [SK]
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
                    fileName = fileName.replaceAll(" ", "");
                    fileName = fileName.replaceAll("/", "");
                    fileName = fileName.replaceAll("&", "");
                    int startIndex = fileName.contains("\\") ? (fileName.lastIndexOf("\\") + 1) : 0;
                    fileName = fileName.substring(startIndex, fileName.lastIndexOf("."));
                }

                if (fileName.length() > 28) { // To fixed Mysql ERROR 1103 (42000): Incorrect table name
                    throw new DataInvalidateException(messageSource.getMessage("acc.import.limit.filename", null, RequestContextUtils.getLocale(request)));
                }
                fi.write(new File(destinationDirectory, fileName + "_" + fileid + Ext));
            }

            FileInputStream fs = new FileInputStream(destinationDirectory + StorageHandler.GetFileSeparator() + fileName + "_" + fileid + Ext);
            Workbook wb = WorkbookFactory.create(fs); // create workbook for all types of xls file like xls, xlsx etc
            int count = wb.getNumberOfSheets();
            JSONArray jArr = new JSONArray();
            for (int x = 0; x < count; x++) {
                JSONObject obj = new JSONObject();
                obj.put("name", wb.getSheetName(x));
                obj.put("index", x);
                jArr.put(obj);
            }
            jobj.put("file", destinationDirectory + StorageHandler.GetFileSeparator() + fileName + "_" + fileid + Ext);
            jobj.put("filename", fileName + "_" + fileid + Ext);
            jobj.put("data", jArr);
            jobj.put("msg", "Image has been successfully uploaded");
            jobj.put("lsuccess", true);
            jobj.put("valid", true);
        } catch (OldExcelFormatException e) {
            Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, e);
            try {
                jobj.put("msg", "Uploaded excel file is of old version. System only supports excel versions from 97/2000/XP/2003 onwards.");
                jobj.put("lsuccess", false);
                jobj.put("valid", true);
            } catch (Exception ex) {
            }
        } catch (Exception e) {
            Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, e);
            try {
                jobj.put("msg", e.getMessage());
                jobj.put("lsuccess", false);
                jobj.put("valid", true);
            } catch (Exception ex) {
            }
        } finally {
            System.out.println("A(( Upload XLS end : " + new Date());
            return new ModelAndView(View, "model", jobj.toString());
        }
    }

    public ModelAndView getImportLog(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("startdate", sdf.parse(request.getParameter("startdate")));
            requestParams.put("enddate", sdf.parse(request.getParameter("enddate")));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            requestParams.put("ss", request.getParameter("ss"));
            } 
            KwlReturnObject result = importDao.getImportLog(requestParams);
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
                jtemp.put("failurename", ilog.getStorageName()!=null ? ilog.getFailureFileName() : "");
                jtemp.put("log", ilog.getLog());
                jtemp.put("imported", ilog.getImported());
                jtemp.put("total", ilog.getTotalRecs());
                jtemp.put("rejected", ilog.getRejected());
                jtemp.put("type", ilog.getType());
                jtemp.put("importon", df.format(ilog.getImportDate()));
                jtemp.put("module", ilog.getModule().getModuleName());
                jtemp.put("importedby", (ilog.getUser().getFirstName() == null ? "" : ilog.getUser().getFirstName()) + " " + (ilog.getUser().getLastName() == null ? "" : ilog.getUser().getLastName()));
                jtemp.put("company", ilog.getCompany().getCompanyName());
                jtemp.put("failurefiletype", ilog.getFailureFileType());
                jArr.put(jtemp);
            }
            jobj.put("data", jArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView removeAllFileTables(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            int cnt = importDao.removeAllFileTables();
            msg = "Deleted " + cnt + " tables successfully";
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void downloadFileData(HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = request.getParameter("filename");
            String storagename = request.getParameter("storagename");
            String filetype = request.getParameter("type");
            String destinationDirectory = storageHandlerImpl.GetDocStorePath();
            destinationDirectory += filetype.equalsIgnoreCase("csv") ? "importplans" : "xlsfiles";
            File intgfile = new File(destinationDirectory + StorageHandler.GetFileSeparator() + storagename);
            
            // if sample file is not exist then create it
            if (!intgfile.exists()) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("storagename", storagename);
                requestParams.put("inputFile", intgfile);
                
                copyFileFromWarToStore(requestParams);
                
                intgfile = new File(destinationDirectory + StorageHandler.GetFileSeparator() + storagename);
            }
            
            byte[] buff = new byte[(int) intgfile.length()];

            try {
                FileInputStream fis = new FileInputStream(intgfile);
                int read = fis.read(buff);
            } catch (IOException ex) {
                filename = "file_not_found.txt";
            }

            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentType("application/octet-stream");
            response.setContentLength(buff.length);
            response.getOutputStream().write(buff);
            response.getOutputStream().flush();
        } catch (IOException ex) {
            KrawlerLog.op.warn("Unable To Download File :" + ex.toString());
        } catch (Exception ex) {
            KrawlerLog.op.warn("Unable To Download File :" + ex.toString());
        }

    }
    
    public void copyFileFromWarToStore(HashMap<String, Object> requestParams) {
        try {
            String storagename = "";
            if (requestParams.containsKey("storagename") && requestParams.get("storagename") != null) {
                storagename = (String) requestParams.get("storagename");
            }
            
            File inputFile = null;
            if (requestParams.containsKey("inputFile") && requestParams.get("inputFile") != null) {
                inputFile = (File) requestParams.get("inputFile");
            }

            // create directory if not exist
            if (inputFile != null && !inputFile.isDirectory()) {
                inputFile.getParentFile().mkdirs();
            }
            
            ServletContext context = this.getServletContext();
            String warFileFullPath = context.getRealPath("/WEB-INF/sampleFiles/" + storagename);
            File warFile = new File(warFileFullPath);
            
            if (inputFile != null && warFile.exists()) {
                FileInputStream input = new FileInputStream(warFile);
                java.io.FileOutputStream output = new java.io.FileOutputStream(inputFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                // copy the file content in bytes 
                while ((bytesRead = input.read(buffer)) > 0) {
                    output.write(buffer, 0, bytesRead);
                }
                
                output.close();
                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

public void downloadExportedFileData(HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = request.getParameter("filename");
            String storagename = request.getParameter("storagename");
            String filetype = request.getParameter("type");
            String destinationDirectory = storageHandlerImpl.GetDocStorePath();
//            destinationDirectory += filetype.equalsIgnoreCase("csv")?"importplans":"xlsfiles";
            File intgfile = new File(destinationDirectory + StorageHandler.GetFileSeparator() + storagename);
            byte[] buff = new byte[(int) intgfile.length()];

            try {
                FileInputStream fis = new FileInputStream(intgfile);
                int read = fis.read(buff);
            } catch (IOException ex) {
                filename = "file_not_found.txt";
}

            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentType("application/octet-stream");
            response.setContentLength(buff.length);
            response.getOutputStream().write(buff);
            response.getOutputStream().flush();
        } catch (IOException ex) {
            KrawlerLog.op.warn("Unable To Download File :" + ex.toString());
        } catch (Exception ex) {
            KrawlerLog.op.warn("Unable To Download File :" + ex.toString());
        }

    }
}
