/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.exportFuctionality;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProductExportDetail;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.reports.ExportXlsServiceHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import org.eclipse.jdt.core.dom.CatchClause;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class ExportSOA implements Runnable {

    private HibernateTransactionManager txnManager;
    private ExportXlsServiceHandler exportXlsservicehandlerobj;
    private Company company;
    private String filename;
    private boolean iscustomer;
    ArrayList processQueue = new ArrayList();
    private boolean isworking = false;
    private accProductDAO accProductObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccExportReportsServiceDAO accExportReportsServiceDAOobj;

    public accProductDAO getAccProductObj() {
        return accProductObj;
    }

    public void setaccExportReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ExportXlsServiceHandler getExportXlsservicehandlerobj() {
        return exportXlsservicehandlerobj;
    }

    public void setExportXlsservicehandlerobj(ExportXlsServiceHandler exportXlsservicehandlerobj) {
        this.exportXlsservicehandlerobj = exportXlsservicehandlerobj;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isIsworking() {
        return isworking;
    }

    public void setIsworking(boolean isworking) {
        this.isworking = isworking;
    }

    public boolean isIscustomer() {
        return iscustomer;
    }

    public void setIscustomer(boolean iscustomer) {
        this.iscustomer = iscustomer;
    }

    public ArrayList getProcessQueue() {
        return processQueue;
    }

    public void setProcessQueue(ArrayList processQueue) {
        this.processQueue = processQueue;
    }

    public HibernateTransactionManager getTxnManager() {
        return txnManager;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void add(HashMap<String, Object> requestParams) {
        try {
            processQueue.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(ExportSOA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            while (!processQueue.isEmpty() && !isworking) {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setName("ExportSOA_Tx");
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
                TransactionStatus status = txnManager.getTransaction(def);
                HashMap<String, Object> requestParams = (HashMap<String, Object>) processQueue.get(0);
                JSONObject paramJobj = requestParams.containsKey("paramsJsonObject") ? (JSONObject) requestParams.get("paramsJsonObject") : new JSONObject();
                isworking = true;
                String companyid = paramJobj.optString(Constants.companyKey);
                try {
                    // Create Entry In Export report with Status as pending
                    JSONObject jobj = new JSONObject();

                    String fileType = paramJobj.optString("typeoffile", "pdf").contains("ZIP") ? "zip" : "pdf";
                    Date requestTime = new Date();
                    SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_hhmmssaa");
                    filename = "StatementOfAccount_" + (sdfTemp.format(requestTime)).toString();
                    String filePath = StorageHandler.GetDocStorePath() + filename + "." + fileType;
                    
                    // Add entry To Export Detail Report in progress
                    HashMap<String, Object> exportDetails = new HashMap<String, Object>();
                    exportDetails.put("fileName", filename + "." + fileType);
                    exportDetails.put("requestTime", requestTime);
                    exportDetails.put("status", 1);
                    exportDetails.put("companyId", companyid);
                    exportDetails.put("fileType", fileType);
                    KwlReturnObject resultExportObj = accProductObj.saveProductExportDetails(exportDetails);
                    ProductExportDetail productExportDetail = (ProductExportDetail) resultExportObj.getEntityList().get(0);
                    txnManager.commit(status);
                    status = txnManager.getTransaction(def);//defining new status to update
                    
                    //  Call Customer SOA /  Vendor SOA from Export Service              
                    if (iscustomer) { /// Customer SOA
                        if (!fileType.equalsIgnoreCase("zip")) {
                            JasperPrint jasperPrint = accExportReportsServiceDAOobj.exportCustomerLedgerJasperReport(paramJobj);
                            JRPdfExporter exp = new JRPdfExporter();
                            JasperExportManager.exportReportToPdfFile(jasperPrint, filePath);
                        } else {
                            paramJobj.put("filename", filePath);
                            accExportReportsServiceDAOobj.exportSOAtoZIP_FILE(paramJobj, true);
                        }
                    } else {// Vendor SAO
                        if (!fileType.equalsIgnoreCase("zip")) {
                            JasperPrint jasperPrint = accExportReportsServiceDAOobj.exportVendorLedgerJasperReport(paramJobj);
                            JRPdfExporter exp = new JRPdfExporter();
                            JasperExportManager.exportReportToPdfFile(jasperPrint, filePath);
                        } else {
                            paramJobj.put("filename", filePath);
                            accExportReportsServiceDAOobj.exportSOAtoZIP_FILE(paramJobj, false);
                        }
                    }
                    
                    // Update entry in Export Detail Report as Completed
                    exportDetails.put("id", productExportDetail.getId());
                    exportDetails.put("status", 2);
                    exportDetails.put("fileType", fileType);
                    accProductObj.saveProductExportDetails(exportDetails);
                    txnManager.commit(status);
                    
                    // Send Mail after download completed
                    paramJobj.put("mailSubject", "Statement of AccountExport Status");
                    paramJobj.put("reportName", "Statement of Account ");
                    exportXlsservicehandlerobj.SendMail(paramJobj, filePath, fileType, filename);
                } catch (JSONException ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(ExportSOA.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(ExportSOA.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    processQueue.remove(requestParams);
                    isworking = false;
                    System.out.println("Done");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ExportSOA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
