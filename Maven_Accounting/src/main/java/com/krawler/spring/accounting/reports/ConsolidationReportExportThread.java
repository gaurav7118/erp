/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProductExportDetail;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class ConsolidationReportExportThread implements Runnable {

    ArrayList processQueue = new ArrayList();
    private boolean isworking = false;

    private AccFinancialReportsService accFinancialReportsService;
    private exportMPXDAOImpl exportMPXDAOImpl;
    private HibernateTransactionManager txnManager;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private accProductDAO accProductObj;
    private AccProductService accProductService;

    public void setAccFinancialReportsService(AccFinancialReportsService accFinancialReportsService) {
        this.accFinancialReportsService = accFinancialReportsService;
    }

    public void setExportMPXDAOImpl(exportMPXDAOImpl exportMPXDAOImpl) {
        this.exportMPXDAOImpl = exportMPXDAOImpl;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setKwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setAuthHandlerDAO(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }

    public void setAccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setAccProductService(AccProductService accProductService) {
        this.accProductService = accProductService;
    }
    
    /*addProductExportDetails
     add request params into queue using Json Object
     */
    public void add(JSONObject requestJobj) {
        try {
            processQueue.add(requestJobj);
        } catch (Exception ex) {
            Logger.getLogger(ExportGroupDetailReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {

        /*
         Exceute code for JE Export
         */
        while (!processQueue.isEmpty() && !isworking) {
            try {
                JSONObject requestJobj = (JSONObject) processQueue.get(0);
                if (requestJobj.getString("exportModule").equalsIgnoreCase("ProfitAndLoss")) {
                    getProfitAndLoss(requestJobj);
                } else if (requestJobj.getString("exportModule").equalsIgnoreCase("Consolidation")) {
                    getConsolidation(requestJobj);
                } else if (requestJobj.getString("exportModule").equalsIgnoreCase("Stock")) {
                    getStock(requestJobj);
                } 
            } catch (JSONException ex) {
                Logger.getLogger(ConsolidationReportExportThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void getProfitAndLoss(JSONObject requestJobj) {
        JSONObject jobj = new JSONObject();
        /*
         Declaration of transaction manager
         */
        isworking = true;

        try {

            Date requestTime = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            int exportStatus = 1;
            SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_HHmmssa");
            String fileName = "ConsolidationProfitLoss_" + (sdfTemp.format(requestTime)).toString();
            HashMap<String, Object> exportDetails = new HashMap<String, Object>();
            exportDetails.put("fileName", fileName + "." + requestJobj.getString("filetype"));
            exportDetails.put("requestTime", requestTime);
            exportDetails.put("status", exportStatus);
            exportDetails.put("companyId", requestJobj.getString(Constants.companyKey));
            exportDetails.put("fileType", requestJobj.getString("filetype"));

            requestJobj.put("filename", fileName);

            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("ConsolThread_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            KwlReturnObject result1 = accProductObj.saveProductExportDetails(exportDetails);
            ProductExportDetail obj = (ProductExportDetail) result1.getEntityList().get(0);
            txnManager.commit(status);
            
            status = txnManager.getTransaction(def);
            jobj = accFinancialReportsService.getConsolidationProfitAndLossReport(requestJobj);
            exportMPXDAOImpl.processRequestNew(requestJobj, jobj);

            HashMap<String, Object> requestParamsForExport = new HashMap<String, Object>();
            requestParamsForExport.put("loginUserId", requestJobj.getString("userId"));
            requestParamsForExport.put("exportFileName", requestJobj.optString("filename", requestJobj.optString("name")));
            requestParamsForExport.put("filetype", requestJobj.optString("filetype"));
            requestParamsForExport.put("email_sub", "Consolidated Profit And Loss Report Export Status");
            requestParamsForExport.put("email_det_txt", "Profit And Loss");

            SendMail(requestParamsForExport);

            txnManager.commit(status);
            if (obj != null) {
                status = txnManager.getTransaction(def);
                exportDetails.put(Constants.Acc_id, obj.getId());
                exportDetails.put("status", 2);
                result1 = accProductObj.saveProductExportDetails(exportDetails);
                obj = (ProductExportDetail) result1.getEntityList().get(0);
                txnManager.commit(status);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            processQueue.remove(requestJobj);
            isworking = false;
        }
    }

    private void getConsolidation(JSONObject requestJobj) {
        JSONObject jobj = new JSONObject();

        isworking = true;

        try {
            
            Date requestTime = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            int exportStatus = 1;
            SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_HHmmssa");
            String fileName = "ConsolidationReport_" + (sdfTemp.format(requestTime)).toString();
            HashMap<String, Object> exportDetails = new HashMap<String, Object>();
            exportDetails.put("fileName", fileName + "." + requestJobj.getString("filetype"));
            exportDetails.put("requestTime", requestTime);
            exportDetails.put("status", exportStatus);
            exportDetails.put("companyId", requestJobj.getString(Constants.companyKey));
            exportDetails.put("fileType", requestJobj.getString("filetype"));

            requestJobj.put("filename", fileName);

            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("ConsolThread_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            KwlReturnObject result1 = accProductObj.saveProductExportDetails(exportDetails);
            ProductExportDetail obj = (ProductExportDetail) result1.getEntityList().get(0);
            txnManager.commit(status);
            
            status = txnManager.getTransaction(def);
            JSONArray array = accFinancialReportsService.getConsolidationReport(requestJobj);
            jobj.put(Constants.RES_data, array);
            exportMPXDAOImpl.processRequestNew(requestJobj, jobj);

            HashMap<String, Object> requestParamsForExport = new HashMap<String, Object>();
            requestParamsForExport.put("loginUserId", requestJobj.getString("userId"));
            requestParamsForExport.put("exportFileName", requestJobj.optString("filename", requestJobj.optString("name")));
            requestParamsForExport.put("filetype", requestJobj.optString("filetype"));
            requestParamsForExport.put("email_sub", "Consolidated Report Export Status");
            requestParamsForExport.put("email_det_txt", "Consolidation");

            SendMail(requestParamsForExport);

            txnManager.commit(status);
            if (obj != null) {
                status = txnManager.getTransaction(def);
                exportDetails.put(Constants.Acc_id, obj.getId());
                exportDetails.put("status", 2);
                result1 = accProductObj.saveProductExportDetails(exportDetails);
                obj = (ProductExportDetail) result1.getEntityList().get(0);
                txnManager.commit(status);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            processQueue.remove(requestJobj);
            isworking = false;
        }
    }

    private void getStock(JSONObject requestJobj) {
        JSONObject jobj = new JSONObject();

        isworking = true;

        try {
            
            Date requestTime = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            int exportStatus = 1;
            SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_HHmmssa");
            String fileName = "ConsolidationStock_" + (sdfTemp.format(requestTime)).toString();
            HashMap<String, Object> exportDetails = new HashMap<String, Object>();
            exportDetails.put("fileName", fileName + "." + requestJobj.getString("filetype"));
            exportDetails.put("requestTime", requestTime);
            exportDetails.put("status", exportStatus);
            exportDetails.put("companyId", requestJobj.getString(Constants.companyKey));
            exportDetails.put("fileType", requestJobj.getString("filetype"));

            requestJobj.put("filename", fileName);

            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("ConsolStockThread_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            KwlReturnObject result1 = accProductObj.saveProductExportDetails(exportDetails);
            ProductExportDetail obj = (ProductExportDetail) result1.getEntityList().get(0);
            txnManager.commit(status);
            
            jobj = accProductService.getConsolidationStockReport(requestJobj);
            
            status = txnManager.getTransaction(def);
            exportMPXDAOImpl.processRequestNew(requestJobj, jobj);

            HashMap<String, Object> requestParamsForExport = new HashMap<String, Object>();
            requestParamsForExport.put("loginUserId", requestJobj.getString("userId"));
            requestParamsForExport.put("exportFileName", requestJobj.optString("filename", requestJobj.optString("name")));
            requestParamsForExport.put("filetype", requestJobj.optString("filetype"));
            requestParamsForExport.put("email_sub", "Consolidated Stock Report Export Status");
            requestParamsForExport.put("email_det_txt", "Consolidation Stock");

            SendMail(requestParamsForExport);

            txnManager.commit(status);
            if (obj != null) {
                status = txnManager.getTransaction(def);
                exportDetails.put(Constants.Acc_id, obj.getId());
                exportDetails.put("status", 2);
                result1 = accProductObj.saveProductExportDetails(exportDetails);
                obj = (ProductExportDetail) result1.getEntityList().get(0);
                txnManager.commit(status);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            processQueue.remove(requestJobj);
            isworking = false;
        }
    }

    public void SendMail(HashMap requestParams) throws ServiceException {

        String loginUserId = (String) requestParams.get("loginUserId");
        User user = (User) kwlCommonTablesDAOObj.getClassObject(User.class.getName(), loginUserId);
        Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), user.getCompany().getCompanyID());
        String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String fileName = (String) requestParams.get("exportFileName");
        String exportFileType = (String) requestParams.get("filetype");
        fileName = fileName + "." + exportFileType;
        String cEmail = user.getEmailID() != null ? user.getEmailID() : "";
        if (!StringUtil.isNullOrEmpty(cEmail)) {
            try {
                String subject = (String) requestParams.get("email_sub"); //"Product Report Export Status";
                //String sendorInfo="admin@deskera.com";
                String htmlTextC = "";
                htmlTextC += "<br/>Hello " + user.getFirstName() + "<br/>";
                htmlTextC += "<br/>" + requestParams.get("email_det_txt") + " Report <b>\"" + fileName + "\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.<br/>";

                htmlTextC += "<br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";

                String plainMsgC = "";
                plainMsgC += "\nHello " + user.getFirstName() + "\n";
                plainMsgC += "\n" + requestParams.get("email_det_txt") + " Report <b>\"" + fileName + "\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.\n";
                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nERP System\n";

                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

}
