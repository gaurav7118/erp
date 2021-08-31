/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProductExportDetail;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.mail.MessagingException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class ExportGroupDetailReport implements Runnable {

    ArrayList processQueue = new ArrayList();
    private boolean isworking = false;
    private HibernateTransactionManager txnManager;
    private AccReportsService accReportsService;
    private exportMPXDAOImpl exportDaoObj;
    private accProductDAO accProductObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private authHandlerDAO authHandlerDAOObj;
    private CommonExportService commonExportService;

    public void setCommonExportService(CommonExportService commonExportService) {
        this.commonExportService = commonExportService;
    }

    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }

    public void add(JSONObject requestJobj) {
        try {
            processQueue.add(requestJobj);
        } catch (Exception ex) {
            Logger.getLogger(ExportGroupDetailReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        while (!processQueue.isEmpty() && !isworking) {
            isworking = true;
            JSONObject requestJobj = (JSONObject) processQueue.get(0);
            JSONArray dataArr = new JSONArray();
            try {
                Date requestTime = new Date();
                SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_hhmmssaa");
                String filename = "GroupDetailReport_" + (sdfTemp.format(requestTime)).toString();
                String append = StringUtil.equal(requestJobj.optString("filetype"), "detailedXls") ? "(Detail)" : "(Summary)";
                String type = "xls";
                if (!StringUtil.isNullOrEmpty(requestJobj.optString("filename", ""))) {
                    filename = requestJobj.optString("filename", "") + (sdfTemp.format(requestTime)).toString();
                }
                if (requestJobj.has("filetype") && requestJobj.getString("filetype").equals("detailedXls")) {
                    type = "xlsx";
                } else if (requestJobj.optString("filetype").equalsIgnoreCase("detailedPDF")|| requestJobj.optString("filetype").equalsIgnoreCase("PDF")) {
                    type = "pdf";
                    append = StringUtil.equal(requestJobj.optString("filetype"), "detailedPDF") ? "(Detail)" : "(Summary)";
                }
                filename = filename.replace(" ","");
                filename = filename + append;
                
                HashMap<String, Object> exportDetails = new HashMap<String, Object>();
                exportDetails.put("fileName", filename + "." + type);
                exportDetails.put("requestTime", requestTime);
                exportDetails.put("status", 1);
                exportDetails.put("companyId", requestJobj.optString("companyid"));
                exportDetails.put("fileType", type);
                KwlReturnObject resultExportObj = accProductObj.saveProductExportDetails(exportDetails);
                ProductExportDetail productExportDetail = (ProductExportDetail) resultExportObj.getEntityList().get(0);
                
                String filePath = exportGroupDetailReportThread(requestJobj, dataArr, filename);

                exportDetails.clear();
                exportDetails.put("id", productExportDetail.getId());
                exportDetails.put("status", 2);
                exportDetails.put("fileType", type);
                accProductObj.saveProductExportDetails(exportDetails);

                SendMail(requestJobj, filePath, type, filename);
                
            } catch (Exception ex) {
                Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
                
            } finally {
                processQueue.remove(requestJobj);
                isworking = false;
                System.out.println("Done.Group Detail Report Exported Successfully.");
            }
        }
    }
    
    
    public void exportGeneralLedger(JSONObject requestJobj) throws ServiceException, JSONException, IOException, SessionExpiredException {
        JSONArray dataArr = new JSONArray();
            Map params = new HashMap();
            params.put("exportid", requestJobj.optString("exportid"));
            commonExportService.updateRequestStatus(2, params);
            Date requestTime = new Date();
            SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_hhmmssaa");
            String append = "";
            String filename = requestJobj.optString("filename");
            String type = requestJobj.optString("filetype");
            if (requestJobj.has("filetype") && (requestJobj.getString("filetype").equals("detailedXls") || requestJobj.getString("filetype").equals("xls"))) {
                type = "xlsx";
            } else if (requestJobj.optString("filetype").equalsIgnoreCase("detailedPDF") || requestJobj.optString("filetype").equalsIgnoreCase("PDF")) {
                type = "pdf";
                append = StringUtil.equal(requestJobj.optString("filetype"), "detailedPDF") ? "(Detail)" : "(Summary)";
            }
            String filePath = exportGroupDetailReportThread(requestJobj, dataArr, filename);
            SendMail(requestJobj, filePath, type, filename);

    }
    
    public String exportGroupDetailReportThread(JSONObject requestJobj, JSONArray dataArr, String filename) throws JSONException, ServiceException {
        String filePath = "";
        if (requestJobj.optString("filetype").equalsIgnoreCase("detailedPDF")) {
            filePath = exportDetailPdf(requestJobj, filename);
        } else {
            accReportsService.getGroupWiseGLReport(requestJobj, dataArr, null);
            JSONObject dataObj = new JSONObject();
            dataObj.put(Constants.RES_data, dataArr);
            dataObj.put("filename", filename);
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("Account_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try{
                if (requestJobj.optString("filetype").equalsIgnoreCase("pdf")) {
                    filePath = exportDetailPdfSummary(requestJobj, filename, dataArr);
                } else {
                    filePath = exportRequestDAO(requestJobj, dataObj);
                }
            }
            finally{
                txnManager.rollback(status);
            }
        }
        return filePath;
    }
    
    public String exportRequestDAO(JSONObject requestJobj, JSONObject dataObj)throws JSONException, ServiceException {
        String filePath = "";
        try {
        filePath = exportDaoObj.processRequestNew(requestJobj, dataObj);
        } catch (IOException | SessionExpiredException ex) {
            Logger.getLogger(AccReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return filePath;
    }
    
    public String exportDetailPdf(JSONObject requestJobj, String filename) {
        JSONArray dataArr = new JSONArray();
        String jrxmlRealPath = requestJobj.optString("jrxmlRealPath");

        String destinationDirectory = storageHandlerImpl.GetDocStorePath();
        String filepath = destinationDirectory + "/" + filename + ".pdf";

        try {
            File destDir = new File(filepath);
            FileOutputStream oss = new FileOutputStream(destDir, true);

            /*
             * Get Data with map needed for jasper
             */
            Map<String, Object> financeDetailsMap = accReportsService.getSubLedgerToExport(requestJobj, dataArr);

            boolean satsTemplateFlag = false;
            if (requestJobj.has("templateflag")) {
                satsTemplateFlag = requestJobj.optInt("templateflag", 0) == Constants.sats_templateflag;
            }
            boolean isLandscape = requestJobj.optBoolean("isLandscape", false);

            InputStream inputStream = new FileInputStream(jrxmlRealPath + "/GeneralLedger.jrxml");
            InputStream inputStreamSubReport = null;
            if (satsTemplateFlag) {
                inputStreamSubReport = new FileInputStream(jrxmlRealPath + "/SATSGeneralLedgerSubReport.jrxml");
            } else if (isLandscape) {
                inputStream = new FileInputStream(jrxmlRealPath + "/GeneralLedgerLandscape.jrxml");
                inputStreamSubReport = new FileInputStream(jrxmlRealPath + "/GeneralLedgerSubReportLandscape.jrxml");
            } else {
                inputStreamSubReport = new FileInputStream(jrxmlRealPath + "/GeneralLedgerSubReport.jrxml");
            }

            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JasperDesign jasperDesignSubReport = JRXmlLoader.load(inputStreamSubReport);
            JasperReport jasperReportSubReport = JasperCompileManager.compileReport(jasperDesignSubReport);
            financeDetailsMap.put("GeneralLedgerSubReport", jasperReportSubReport);
            JRBeanCollectionDataSource beanColDataSource = (JRBeanCollectionDataSource) financeDetailsMap.get("datasource");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, financeDetailsMap, beanColDataSource);

            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, baos1);
            exp.exportReport();
            oss.write(baos1.toByteArray());

            if (oss != null) {
                oss.flush();
                oss.close();
            }
        } catch (Exception ex) {
            Logger.getLogger(ExportGroupDetailReport.class.getName()).log(Level.SEVERE, "ExportLedger.SendMail :" + ex.getMessage(), ex);
        }
        return filepath;
    }
    
    private String exportDetailPdfSummary(JSONObject requestJobj, String filename, JSONArray dataArr) {
        String jrxmlRealPath = requestJobj.optString("jrxmlRealPath");
        String destinationDirectory = storageHandlerImpl.GetDocStorePath();
        String filepath = destinationDirectory + "/" + filename + ".pdf";
        boolean isLandscape = requestJobj.optBoolean("isLandscape",false);
         try {
            File destDir = new File(filepath);
            FileOutputStream oss = new FileOutputStream(destDir, true);

            /*
             * Get Data with map needed for jasper
             */
            Map<String, Object> financeDetailsMap = accReportsService.getGeneralLedgerToExportPDFSummary(requestJobj, dataArr);
            InputStream inputStream = null;
            InputStream inputStreamSubReport = null;
            if (isLandscape) {
                        inputStream = new FileInputStream(requestJobj.get("jrxmlRealPath") + "/GeneralLedgerLandscape.jrxml");
                        inputStreamSubReport = new FileInputStream(requestJobj.get("jrxmlRealPath")+ "/GeneralLedgerSummaryLandScape.jrxml");
                    } else {
                        inputStream = new FileInputStream(requestJobj.get("jrxmlRealPath") + "/GeneralLedger.jrxml");
                        inputStreamSubReport = new FileInputStream(requestJobj.get("jrxmlRealPath") + "/GeneralLedgerSummary.jrxml");
                    }
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JasperDesign jasperDesignSubReport = JRXmlLoader.load(inputStreamSubReport);
            JasperReport jasperReportSubReport = JasperCompileManager.compileReport(jasperDesignSubReport);
            financeDetailsMap.put("GeneralLedgerSubReport", jasperReportSubReport);
            JRBeanCollectionDataSource beanColDataSource = (JRBeanCollectionDataSource) financeDetailsMap.get("datasource");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, financeDetailsMap, beanColDataSource);

            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, baos1);
            exp.exportReport();
            oss.write(baos1.toByteArray());

            if (oss != null) {
                oss.flush();
                oss.close();
            }
        } catch (Exception ex) {
             Logger.getLogger(ExportGroupDetailReport.class.getName()).log(Level.SEVERE, "ExportLedger.SendMail :" + ex.getMessage(), ex);
//             Map params = new HashMap();
//             try {
//                 params.put("exportid", requestJobj.optString("exportid"));
//                 commonExportService.updateRequestStatus(5, params);
//             } catch (Exception e) {
//                 Logger.getLogger(JMSExportConsumer.class.getName()).log(Level.SEVERE, null, e);
//             }
         }
        
        return filepath;
    }
    public void SendMail(JSONObject requestJobj, String path, String fileType, String filename) throws ServiceException {


        String loginUserId = requestJobj.optString("userid");

        KwlReturnObject KWLUser = accountingHandlerDAOobj.getObject(User.class.getName(), loginUserId);
        User user = (User) KWLUser.getEntityList().get(0);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String fileName = filename;
        fileName = fileName + "." + fileType;

        String cEmail = user.getEmailID() != null ? user.getEmailID() : "";
        if (!StringUtil.isNullOrEmpty(cEmail)) {
            try {
                String subject = "General Ledger Report Export Status";
                String htmlTextC = "";
                htmlTextC += "<br/>Hello " + user.getFirstName() + "<br/>";
                htmlTextC += "<br/>General Ledger Report <b>\"" + fileName + "\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.<br/>";

                htmlTextC += "<br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";

                String plainMsgC = "";
                plainMsgC += "\nHello " + user.getFirstName() + "\n";
                plainMsgC += "\nGeneral Ledger Report <b>\"" + fileName + "\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.\n";
                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nERP System\n";


                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                try {
                    SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, new String[]{path}, smtpConfigMap);
                } catch (MessagingException ex1) {
                    Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, "ExportLedger.SendMail :" + ex1.getMessage(), ex1);
                    SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
                } finally {
                    System.out.println("Mail Catch-1 Completed: " + new Date());
//                    Map params = new HashMap();
//                    params.put("exportid", requestJobj.optString("exportid"));
//                    commonExportService.updateRequestStatus(3, params);
                }

            } catch (Exception ex) {
                Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            } finally {
                System.out.println("Mail Catch-2 Completed: " + new Date());
            }
        }

    }
}