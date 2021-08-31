/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ImportLog;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author sagar
 */
public class OlympusImportDataHandler {
    public static void maintainLogFile(String filePath, StringBuilder destinationFileName, StringBuilder successRecords, StringBuilder failedRecords, int failedCnt, int total, StringBuilder msg) {
        // Move File to another location after file process
        StringBuilder ExT = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String fileName = getActualFileName(filePath, ExT) + "_" + df.format(new Date()) + ".csv";
        String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
        destinationFileName.append(destinationDirectory).append(File.separator).append(fileName);
        File dirFrom = new File(filePath);
        File dirTo = new File(destinationFileName.toString());
//                copyFileUsingFileStreams(dirFrom, dirTo);
        createFiles(fileName, successRecords, ".csv", false);

        // Create Failure File 
        if (failedCnt > 0) {
            createFiles(fileName, failedRecords, ".csv", true);
        }
        int success = total - failedCnt;
        if (total == 0) {
            msg.append("Empty file.");
        } else if (success == 0) {
            msg.append("Failed to import all the records.");
        } else if (success == total) {
            msg.append("All records are imported successfully.");
        } else {
            msg.append("Imported " + success + " record" + (success > 1 ? "s" : "")).append(" successfully");
            msg.append(failedCnt == 0 ? "." : " and failed to import " + failedCnt + " record" + (failedCnt > 1 ? "s" : "") + ".");
        }
    }
    
    public static void maintainLogFile(String filePath, StringBuilder destinationFileName, StringBuilder successRecords, StringBuilder failedRecords, 
            int failedCnt, int total, StringBuilder msg, Date processStartDate, int totalrec) {
        // Move File to another location after file process
        StringBuilder ExT = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String fileName = getActualFileName(filePath, ExT) + "_" + df.format(processStartDate) + ".csv";
        String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
        File destinationDir = new File(destinationDirectory);
        if (!destinationDir.exists()) { //Create dir. if not present
            destinationDir.mkdirs();
        }
        destinationFileName.append(destinationDirectory).append(File.separator).append(fileName);
        File dirFrom = new File(filePath);
        File dirTo = new File(destinationFileName.toString());
//                copyFileUsingFileStreams(dirFrom, dirTo);
        createFiles(fileName, successRecords, ".csv", false);

        // Create Failure File 
        if (failedCnt > 0) {
            createFiles(fileName, failedRecords, ".csv", true);
        }
        int success = 0;
        if(total>0){
            success = total - failedCnt;
        }
        if (total == 0) {
            msg.replace(0, msg.length(), "");
            msg.append("Empty file.");
        } else if (success == 0) {
            msg.replace(0, msg.length(), "");
            msg.append("Failed to import all the records.");
        } else if (success-1 == totalrec) {
            msg.replace(0, msg.length(), "");
            msg.append("All records are imported successfully.");
        } else {
            msg.replace(0, msg.length(), "");
            msg.append("Imported " + success + " record" + (success > 1 ? "s" : "")).append(" successfully");
            msg.append(failedCnt == 0 ? "." : " and failed to import " + failedCnt + " record" + (failedCnt > 1 ? "s" : "") + ".");
        }
    }
    
    public static String maintainBatchSerialFile(String filePath, StringBuilder destinationFileName, StringBuilder batchSerialRecords, 
           int total, StringBuilder msg, Date processStartDate) {
        // Move File to another location after file process
        StringBuilder ExT = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String fileName = getActualFileName(filePath, ExT) + "_" + df.format(processStartDate) + ".csv";
        String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
        File destinationDir = new File(destinationDirectory);
        if (!destinationDir.exists()) { //Create dir. if not present
            destinationDir.mkdirs();
        }
        destinationFileName.append(destinationDirectory).append(File.separator).append(fileName);
       
        String filepath = createBatchSerialFiles(fileName, batchSerialRecords, ".csv");
     
        int success = 0;
        if(total>0){
            success = total;
        }
        if (total == 0) {
            msg.replace(0, msg.length(), "");
            msg.append("Empty file.");
        } 
        return filepath;
    }
    
    public static String getActualFileName(String storageName, StringBuilder ext) {
        ext.append(storageName.substring(storageName.lastIndexOf(".")));
        String actualName = storageName.substring(storageName.lastIndexOf(File.separator)+1, storageName.lastIndexOf("."));
//        actualName = actualName;
        return actualName;
    }
    
    public static void createFiles(String filename, StringBuilder failedRecords, String ext, boolean isFailure) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
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
    public static String createBatchSerialFiles(String filename, StringBuilder batchSerialRecords, String ext) {
        String createdFilePath = "";
        try {
            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            filename = filename.substring(0, filename.lastIndexOf("."));
            createdFilePath = destinationDirectory + File.separator + filename + "_BS" + ext;
            java.io.FileOutputStream batchserialFile = new java.io.FileOutputStream(createdFilePath);
            batchserialFile.write(batchSerialRecords.toString().getBytes());
            batchserialFile.flush();
            batchserialFile.close();
    
        } catch (Exception ex) {
            System.out.println("\n Batch Serial file write [success/failed] " + ex);
        }
        return createdFilePath;
    }
    
    public static void addLogEntry(ImportDAO importDao, StringBuilder destinationFileName, StringBuilder msg, int total, int failedCnt, Company company, boolean issuccess, String moduleid) throws ServiceException, DataInvalidateException {
        StringBuilder ExT = new StringBuilder();
        String onlyFileName = getActualFileName(destinationFileName.toString(), ExT);
        HashMap<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("FileName", onlyFileName + ExT);
        logDataMap.put("StorageName", onlyFileName + ExT);
        logDataMap.put("Log", msg.toString());
        logDataMap.put("Type", destinationFileName.substring(destinationFileName.lastIndexOf(".") + 1));
        logDataMap.put("TotalRecs", total);
        logDataMap.put("Rejected", issuccess ? failedCnt : total);// if fail then rejected = total
        logDataMap.put("Module", moduleid);
        logDataMap.put("ImportDate", new Date());
        logDataMap.put("User", company.getCreator().getUserID());
        logDataMap.put("Company", company.getCompanyID());
        importDao.saveImportLog(logDataMap);
    }
    
    public static ImportLog addInitialLogEntry(ImportDAO importDao, String logid, StringBuilder destinationFileName, StringBuilder msg, int total, int failedCnt, Company company, 
            boolean issuccess, String moduleid, Date ImportDate) throws ServiceException, DataInvalidateException {
        HashMap<String, Object> logDataMap = new HashMap<String, Object>();
        if(!StringUtil.isNullOrEmpty(logid)) {
            logDataMap.put("Id", logid);
        }
        
        logDataMap.put("Log", msg.toString());
        logDataMap.put("TotalRecs", total);
        logDataMap.put("Rejected", issuccess ? failedCnt : total);// if fail then rejected = total
        logDataMap.put("Module", moduleid);
        if(ImportDate!=null) {
            logDataMap.put("ImportDate", new Date());
        } 
        if(company!=null) {
            logDataMap.put("User", company.getCreator().getUserID());
            logDataMap.put("Company", company.getCompanyID());
        }
        
        StringBuilder ExT = new StringBuilder();
        String onlyFileName = getActualFileName(destinationFileName.toString(), ExT);
        logDataMap.put("FileName", onlyFileName + ExT);
        logDataMap.put("StorageName", onlyFileName + ExT);
        logDataMap.put("Type", destinationFileName.substring(destinationFileName.lastIndexOf(".") + 1));
        
        return (ImportLog) importDao.saveImportLog(logDataMap);
    }
    
    
    public static void updateLogEntry(ImportDAO importDao, String logid, StringBuilder destinationFileName, StringBuilder msg, int total, int failedCnt, Company company, boolean issuccess, String moduleid) throws ServiceException, DataInvalidateException {
        StringBuilder ExT = new StringBuilder();
        HashMap<String, Object> logDataMap = new HashMap<String, Object>();
        if(!StringUtil.isNullOrEmpty(destinationFileName.toString())) {
            String onlyFileName = getActualFileName(destinationFileName.toString(), ExT);
            logDataMap.put("FileName", onlyFileName + ExT);
            logDataMap.put("StorageName", onlyFileName + ExT);
            logDataMap.put("Type", destinationFileName.substring(destinationFileName.lastIndexOf(".") + 1));
        } else {
            logDataMap.put("ImportDate", new Date());
            logDataMap.put("Module", moduleid);
            if(company!=null) {
                logDataMap.put("User", company.getCreator().getUserID());
                logDataMap.put("Company", company.getCompanyID());
            }
        }
        if(!StringUtil.isNullOrEmpty(logid)) {
            logDataMap.put("Id", logid);
        }
        logDataMap.put("Log", msg.toString());
        logDataMap.put("TotalRecs", total);
        logDataMap.put("Rejected", issuccess ? failedCnt : total);// if fail then rejected = total
        importDao.saveImportLog(logDataMap);
    }
    
    public static String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString()) + "\"\t";
        }
        return rec;
    }
}
