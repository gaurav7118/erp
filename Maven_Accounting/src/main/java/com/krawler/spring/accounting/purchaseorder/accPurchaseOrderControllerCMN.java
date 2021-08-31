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
package com.krawler.spring.accounting.purchaseorder;

import com.krawler.hql.accounting.InvoiceDocuments;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.CommonIndonesianNumberToWords;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.SHIPDATE;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.TAXPERCENT;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonEnglishNumberToWords;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.invoice.*;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.purchaseorder.service.AccPurchaseOrderModuleService;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderController;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.*;
import com.krawler.spring.exportFuctionality.AccExportReportsServiceDAO;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accPurchaseOrderControllerCMN extends MultiActionController implements MessageSourceAware {
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj;
    private AccExportReportsServiceDAO accExportReportsServiceDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private accAccountDAO accAccountDAOobj;
    private accTaxDAO accTaxObj;
    private String successView;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private CustomDesignDAO customDesignDAOObj;
    private VelocityEngine velocityEngine;
    private CommonEnglishNumberToWords EnglishNumberToWordsOjb = new CommonEnglishNumberToWords();
    private CommonIndonesianNumberToWords IndonesianNumberToWordsOjb = new CommonIndonesianNumberToWords();
    private AccCommonTablesDAO accCommonTablesDAO; 
    private accProductDAO accProductObj;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccExportReportsServiceDAO accExportOtherReportsServiceDAOobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private HibernateTransactionManager txnManager;
    private MessageSource messageSource;
    private fieldDataManager fieldDataManagercntrl;
    private auditTrailDAO auditTrailObj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private AccInvoiceServiceDAO accInvoiceServiceDAOObj;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj;
    private fieldManagerDAO fieldManagerDAOobj;
    private AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj;
 
    public void setAccInvoiceServiceDAOObj(AccInvoiceServiceDAO accInvoiceServiceDAOObj) {
        this.accInvoiceServiceDAOObj = accInvoiceServiceDAOObj;
    }

    public void setAccGoodsReceiptServiceDAOObj(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj) {
        this.accGoodsReceiptServiceDAOObj = accGoodsReceiptServiceDAOObj;
    }
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    
    public void setaccExportOtherReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportOtherReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
        
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    public void setaccPurchaseOrderServiceDAO(AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj) {
        this.accPurchaseOrderServiceDAOobj = accPurchaseOrderServiceDAOobj;
    }
    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }
    public void setaccExportReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setaccTaxDAO (accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setcustomDesignDAO(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }
    public void setvelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }   
    public void setAccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccSalesOrderServiceDAO(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }
 
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
 
    public void setAccPurchaseOrderModuleServiceObj(AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj) {
        this.accPurchaseOrderModuleServiceObj = accPurchaseOrderModuleServiceObj;
    }

    public ModelAndView getPurchaseOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            if (request.getParameter("isFixedAsset") != null && !StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) {
                boolean isFixedAsset = Boolean.FALSE.parseBoolean(requestParams.get("isFixedAsset").toString());
                requestParams.put("isFixedAsset", isFixedAsset);
            }
            if (request.getParameter("isjobworkwitoutgrn") != null && !StringUtil.isNullOrEmpty(request.getParameter("isjobworkwitoutgrn"))) {
                boolean isjobworkwitoutgrn = Boolean.parseBoolean(request.getParameter("isjobworkwitoutgrn").toString());
                requestParams.put("isjobworkwitoutgrn", isjobworkwitoutgrn);
            }
            /*
            isForSGELink flag is used in case of linking PO in SGE
            */
            if(request.getParameter("isForSGELink") != null && !StringUtil.isNullOrEmpty(request.getParameter("isForSGELink"))){
                boolean isForSGELink =Boolean.parseBoolean(request.getParameter("isForSGELink"));
                requestParams.put("isForSGELink", isForSGELink);
            }
            /*
             * "Job Work Order" linking case i.e. in GRN and PI
            */
            if(request.getParameter("isForJobWorkOutLinkingInGRN") != null && !StringUtil.isNullOrEmpty(request.getParameter("isForJobWorkOutLinkingInGRN"))){
                boolean isForJobWorkOutLinkingInGRN =Boolean.parseBoolean(request.getParameter("isForJobWorkOutLinkingInGRN"));
                requestParams.put("isForJobWorkOutLinkingInGRN", isForJobWorkOutLinkingInGRN);
            }
            if(request.getParameter("isJobWorkOrderInPI") != null && !StringUtil.isNullOrEmpty(request.getParameter("isJobWorkOrderInPI"))){
                boolean isJobWorkOrderInPI =Boolean.parseBoolean(request.getParameter("isJobWorkOrderInPI"));
                requestParams.put("isJobWorkOrderInPI", isJobWorkOrderInPI);
            }              
            boolean isGenerateOrderFromOrder=false;
            /*-------isGenerateOrderFromOrder comes true when Generating SO from PO-------*/
            if (!StringUtil.isNullOrEmpty(request.getParameter("isGenerateOrderFromOrder"))) {
                isGenerateOrderFromOrder = Boolean.parseBoolean((String) request.getParameter("isGenerateOrderFromOrder"));
                requestParams.put("isGenerateOrderFromOrder", isGenerateOrderFromOrder);

            }
            //fetch isDraft flag from request
            boolean isDraft = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isDraft))) {
                isDraft = Boolean.parseBoolean((String) request.getParameter(Constants.isDraft));
                requestParams.put(Constants.isDraft, isDraft);
            }
            //get module id which requested getPurchaseOrders
            if (request.getParameter("requestModuleid") != null && !StringUtil.isNullOrEmpty(request.getParameter("requestModuleid"))) {
                int requestModuleID=Integer.parseInt(request.getParameter("requestModuleid"));
                    requestParams.put("requestModuleid", requestModuleID);    
                if(extraCompanyPreferences.isEnableLinkToSelWin() && !Boolean.parseBoolean(request.getParameter("isGrid")) && (requestModuleID == Constants.Acc_Sales_Order_ModuleId ||requestModuleID == Constants.Acc_Goods_Receipt_ModuleId || requestModuleID==Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId || requestModuleID==Constants.Acc_Vendor_Invoice_ModuleId ||  requestModuleID==Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId )){
                     requestParams.put("start","0");
                     requestParams.put("limit", "10");
                     if(isGenerateOrderFromOrder){
                         requestParams.put("linkTransactionId", request.getParameter("linkTransactionId")); 
                     }
                }
            }
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            KwlReturnObject result = accPurchaseOrderobj.getPurchaseOrders(requestParams);
            requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(request));
         
            jobj = getPurchaseOrdersJsonOptimized(requestParams, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getPurchaseOrders : "+ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
/**
 * Description To Get data in security gate entry report
 * @param request
 * @param response
 * @return 
 */
    public ModelAndView getSecurityGateEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            String dir = "";
            String sort = "";
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            
            KwlReturnObject result = accPurchaseOrderobj.getSecurityGateEntry(requestParams);
            
            requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(request));
            
            jobj = getSecurityGateJsonOptimized(requestParams, result.getEntityList());
            
            jobj.put("count", result.getRecordTotalCount());
            
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getSecurityGateEntry : " + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
     public void exportPurchaseOrderJasper(HttpServletRequest request, HttpServletResponse response) {
        try {
            List jasperPrint = accExportReportsServiceDAOobj.exportPurchaseOrderJasper(request,response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
     public void exportSwatowPurchaseOrderJasper(HttpServletRequest request, HttpServletResponse response) {
        try {
            List jasperPrint = accExportOtherReportsServiceDAOobj.exportSwatowPurchaseOrder(request,response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
     public void exportVendorQuoataion(HttpServletRequest request, HttpServletResponse response) {
        try {
            List jasperPrint = accExportOtherReportsServiceDAOobj.exportVendorQuotation(request,response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
  
    public void exportSenwanGroupPurchaseOrderJasper(HttpServletRequest request, HttpServletResponse response) {
        try {
            List jasperPrint = accExportReportsServiceDAOobj.exportSenwanGroupPurchaseOrderJasper(request,response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
        public void exportFerrateGroupPurchaseOrderJasper(HttpServletRequest request, HttpServletResponse response) {
            try {
            List jasperPrint = accExportReportsServiceDAOobj.exportFerrateGroupPurchaseOrderJasper(request,response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
        
         public void exportDiamondAviationPurchaseOrderJasper(HttpServletRequest request, HttpServletResponse response) {
            try {
            List jasperPrint = accExportOtherReportsServiceDAOobj.exportDiamondAviationPuchaseOrder(request,response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
         public void exportMonzonePuchaseOrder(HttpServletRequest request, HttpServletResponse response) {
            try {
            List jasperPrint = accExportOtherReportsServiceDAOobj.exportMonzonePuchaseOrder(request,response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

        public void exportF1RecreationPurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        try {
            List jasperPrint = accExportReportsServiceDAOobj.exportF1RecreationPurchaseOrder(request,response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
        //Purchase Order Line  Details   Excel File  Report For MonZone
    public void exportPurchaseOrderRegisterXlsReport(HttpServletRequest request, HttpServletResponse response) {
        try {
            accExportOtherReportsServiceDAOobj.getPurchaseOrdersForXls(request, response);
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    public ModelAndView getOpeningBalancePurchaseOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            String[] companyids = sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);

            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            String vendorId = request.getParameter("custVenId");
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("vendorid", vendorId);
                requestParams.put("start", request.getParameter("start"));
                requestParams.put("limit", request.getParameter("limit"));
                KwlReturnObject result = accPurchaseOrderobj.getOpeningBalancePurchaseOrders(requestParams);
                List<PurchaseOrder> list = result.getEntityList();
                DataJArr = getOpeningBalancePOJson(request, list, DataJArr);
            }

            int count = DataJArr.length();
            JSONArray pagedJson = DataJArr;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }

            jobj.put("data", pagedJson);
            jobj.put("count", count);
            issuccess = true;

        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    public JSONArray getOpeningBalancePOJson(HttpServletRequest request, List<PurchaseOrder> list, JSONArray dataArray) {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap requestParams = getPurchaseOrderMap(request);

            for (PurchaseOrder purchaseOrder : list) {
                JSONObject poJson = new JSONObject();
                poJson.put("transactionId", purchaseOrder.getID());
                poJson.put("transactionNo", purchaseOrder.getPurchaseOrderNumber());

                Iterator itrRow = purchaseOrder.getRows().iterator();
                double amount = 0, totalDiscount = 0, discountPrice = 0;
                while (itrRow.hasNext()) {
                    PurchaseOrderDetail pod = (PurchaseOrderDetail) itrRow.next();
                    double rowTaxPercent = 0;
                    if (pod.getTax() != null) {
                        requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                        requestParams.put("taxid", pod.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    }

                    double poPrice = authHandler.round(pod.getQuantity() * pod.getRate(), companyid);
                    if (pod.getDiscountispercent() == 1) {
                        discountPrice = (poPrice) - (poPrice * pod.getDiscount() / 100);
                    } else {
                        discountPrice = poPrice - pod.getDiscount();
                    }


                    amount += discountPrice + pod.getRowTaxAmount();//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                }
                if (purchaseOrder.getDiscount() != 0) {
                    if (purchaseOrder.isPerDiscount()) {
                        totalDiscount = amount * purchaseOrder.getDiscount() / 100;
                        amount = amount - totalDiscount;
                    } else {
                        amount = amount - purchaseOrder.getDiscount();
                        totalDiscount = purchaseOrder.getDiscount();
                    }
                }
                double totalTermAmount = 0;
                HashMap<String, Object> requestParam = new HashMap();
                requestParam.put("purchaseOrder", purchaseOrder.getID());
                KwlReturnObject purchaseOrderResult = null;
                purchaseOrderResult = accPurchaseOrderobj.getPurchaseOrderTermMap(requestParam);
                List<PurchaseOrderTermMap> termMap = purchaseOrderResult.getEntityList();
                for (PurchaseOrderTermMap purchaseOrderTermMap : termMap) {
                    InvoiceTermsSales mt = purchaseOrderTermMap.getTerm();
                    double termAmnt = purchaseOrderTermMap.getTermamount();
                    totalTermAmount += termAmnt;
                }


                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, purchaseOrder.getCurrency().getCurrencyID(), purchaseOrder.getOrderDate(), 0);
                double taxPercent = 0;
                if (purchaseOrder.getTax() != null) {
                    requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                    requestParams.put("taxid", purchaseOrder.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                }
                double orderAmount = amount;//double orderAmount=(Double) bAmt.getEntityList().get(0);
                double ordertaxamount = (taxPercent == 0 ? 0 : orderAmount * taxPercent / 100);
                amount += totalTermAmount;
                orderAmount += totalTermAmount;
                poJson.put("transactionDate", df.format(purchaseOrder.getOrderDate()));
                poJson.put("transactionAmount", orderAmount+ordertaxamount);
                poJson.put("currencysymbol", (purchaseOrder.getCurrency() == null ? "" : purchaseOrder.getCurrency().getSymbol()));
                poJson.put("currencyid", (purchaseOrder.getCurrency() == null ? "" : purchaseOrder.getCurrency().getCurrencyID()));
                
                dataArray.put(poJson);
            }


        } catch (ServiceException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }

    public HashMap<String, Object> getPurchaseOrderMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            requestParams.put(Constants.start, request.getParameter(Constants.start));
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        }
        requestParams.put(Constants.ss, StringUtil.DecodeText(request.getParameter(Constants.ss)));
        if(!StringUtil.isNullOrEmpty(request.getParameter("sort")) && !StringUtil.isNullOrEmpty(request.getParameter("dir"))){
            requestParams.put("sort", request.getParameter("sort"));
            requestParams.put("dir", request.getParameter("dir"));
        }
        requestParams.put(Constants.REQ_costCenterId,request.getParameter(Constants.REQ_costCenterId));
        requestParams.put(Constants.REQ_vendorId,request.getParameter(Constants.REQ_vendorId));
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
         requestParams.put(InvoiceConstants.newvendorid, request.getParameter(InvoiceConstants.newvendorid));
        requestParams.put(InvoiceConstants.productid, request.getParameter(InvoiceConstants.productid));
        requestParams.put(InvoiceConstants.productCategoryid, request.getParameter(InvoiceConstants.productCategoryid));
        requestParams.put("doflag", request.getParameter("doflag")!=null?true:false);
        boolean closeflag = request.getParameter("closeflag")!=null?Boolean.parseBoolean(request.getParameter("closeflag")):false;
        requestParams.put("closeflag", closeflag);
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put(Constants.ValidFlag, request.getParameter(Constants.ValidFlag));
        requestParams.put(Constants.BillDate ,request.getParameter(Constants.BillDate));
        requestParams.put("pendingapproval" ,(request.getParameter("pendingapproval") != null)? Boolean.parseBoolean(request.getParameter("pendingapproval")): false);
        requestParams.put("istemplate" ,(request.getParameter("istemplate") != null)? Integer.parseInt(request.getParameter("istemplate")): 0);
        requestParams.put(Constants.MARKED_FAVOURITE, request.getParameter(Constants.MARKED_FAVOURITE));
        requestParams.put("currencyid",request.getParameter("currencyid"));
        requestParams.put("exceptFlagINV" ,request.getParameter("exceptFlagINV"));
        requestParams.put("exceptFlagORD" ,request.getParameter("exceptFlagORD"));
        requestParams.put("linkFlagInPO" ,request.getParameter("linkFlagInPO"));
        requestParams.put("linkFlagInGR" ,request.getParameter("linkFlagInGR"));
        requestParams.put("linkflag" ,request.getParameter("linkflag"));
        requestParams.put(Constants.Acc_Search_Json ,request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria ,request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid ,request.getParameter(Constants.moduleid));
        requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null)? "" : request.getParameter("currencyfilterfortrans"));
        requestParams.put("isOpeningBalanceOrder", request.getParameter("isOpeningBalanceOrder")!=null?Boolean.parseBoolean(request.getParameter("isOpeningBalanceOrder")):false);
        requestParams.put(CCConstants.REQ_vendorId,request.getParameter(CCConstants.REQ_vendorId));
        requestParams.put(Constants.customerCategoryid, request.getParameter(Constants.customerCategoryid));
        requestParams.put("billId",request.getParameter("billid"));
        requestParams.put("blockedDocuments",request.getParameter("blockedDocuments"));
        requestParams.put("unblockedDocuments",request.getParameter("unblockedDocuments"));
        if(request.getParameter("includingGSTFilter")!=null){
            requestParams.put("includingGSTFilter",Boolean.parseBoolean(request.getParameter("includingGSTFilter")));
        }
        requestParams.put("isConsignment", request.getParameter("isConsignment")!=null?Boolean.parseBoolean(request.getParameter("isConsignment")):false);
        requestParams.put("isMRPJOBWORKIN", request.getParameter("isMRPJOBWORKIN")!=null?Boolean.parseBoolean(request.getParameter("isMRPJOBWORKIN")):false);
        requestParams.put("isMRPJOBWORKOUT", request.getParameter("isMRPJOBWORKOUT")!=null?Boolean.parseBoolean(request.getParameter("isMRPJOBWORKOUT")):false);
        requestParams.put("isJobWorkOrderReciever", request.getParameter("isJobWorkOrderReciever")!=null?Boolean.parseBoolean(request.getParameter("isJobWorkOrderReciever")):false);
        requestParams.put("isFixedAsset", (request.getParameter("isFixedAsset") != null) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false);
        requestParams.put("isShowAddress", (request.getParameter("isShowAddress") != null) ? Boolean.parseBoolean(request.getParameter("isShowAddress")) : false);
        requestParams.put("isDraft", (request.getParameter("isDraft") != null) ? Boolean.parseBoolean(request.getParameter("isDraft")) : false); 
        if(!StringUtil.isNullOrEmpty(request.getParameter("linknumber"))){
            requestParams.put("linknumber", request.getParameter("linknumber"));
        }
        if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
            requestParams.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
        }
        return requestParams;
    }
    
    public JSONObject getPurchaseOrdersJson(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            boolean closeflag = (Boolean) requestParams.get("closeflag");
            boolean doflag = (Boolean) requestParams.get("doflag");
            boolean exceptFlagINV=false;
            if (requestParams.containsKey("exceptFlagINV") && requestParams.get("exceptFlagINV") != null) {
                exceptFlagINV = Boolean.parseBoolean((String)requestParams.get("exceptFlagINV"));
            }
            boolean isConsignment = false;
            if (requestParams.containsKey("isConsignment")) {
                isConsignment = Boolean.FALSE.parseBoolean(requestParams.get("isConsignment").toString());
            }
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                    String currencyid = (String) requestParams.get("gcurrencyid");
                    KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
                    KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            
            String companyid = requestParams.get("companyid").toString();
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, isConsignment?Constants.Acc_ConsignmentVendorRequest_ModuleId:Constants.Acc_Purchase_Order_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
                    DateFormat df = (DateFormat) requestParams.get("df");
                PurchaseOrder purchaseOrder = (PurchaseOrder) itr.next();
                    Vendor vendor = purchaseOrder.getVendor();
                    KWLCurrency currency = null;
                
                if(purchaseOrder.getCurrency() != null){
                        currency = purchaseOrder.getCurrency();
                    } else {
                    currency=purchaseOrder.getVendor().getAccount().getCurrency()==null?kwlcurrency:purchaseOrder.getVendor().getAccount().getCurrency();
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("billid", purchaseOrder.getID());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("personid", vendor.getID());
                    obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                    obj.put("duedate", df.format(purchaseOrder.getDueDate()));
                    obj.put("date", df.format(purchaseOrder.getOrderDate()));
                    obj.put("shipdate", purchaseOrder.getShipdate()==null? "" : df.format(purchaseOrder.getShipdate()));
                    obj.put("shipvia", purchaseOrder.getShipvia()==null? "" : purchaseOrder.getShipvia());
                    obj.put("fob", purchaseOrder.getFob()==null?"" : purchaseOrder.getFob());
                    obj.put("termdetails", getTermDetails(purchaseOrder.getID(),true));
                    obj.put("shiplengthval", purchaseOrder.getShiplength());
                    Iterator itrRow = purchaseOrder.getRows().iterator();
                    double amount = 0,totalDiscount = 0, discountPrice = 0;
                    while (itrRow.hasNext()) {
                        PurchaseOrderDetail pod = (PurchaseOrderDetail) itrRow.next();
                        amount += authHandler.round(pod.getQuantity() * pod.getRate(), companyid);
                        double  rowTaxPercent=0;
                        if(pod.getTax()!=null){
                            requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                            requestParams.put("taxid", pod.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                        }

                        double poPrice = authHandler.round(pod.getQuantity() * pod.getRate(), companyid); 
                        if(pod.getDiscountispercent() == 1) {
                            discountPrice = (poPrice) - (poPrice * pod.getDiscount()/100);
                        } else {
                            discountPrice = poPrice - pod.getDiscount();
                        }
                        amount += discountPrice + (discountPrice * rowTaxPercent/100);
                    }
                    obj.put("currencysymbol", currency.getSymbol());
                    if (purchaseOrder.getDiscount() != 0) {
                        if (purchaseOrder.isPerDiscount()) {
                            totalDiscount = amount * purchaseOrder.getDiscount() / 100;
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - purchaseOrder.getDiscount();
                            totalDiscount = purchaseOrder.getDiscount();
                        }
                        obj.put("discounttotal", purchaseOrder.getDiscount());
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    obj.put("discount", totalDiscount);
                    obj.put("discountispertotal", purchaseOrder.isPerDiscount());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), purchaseOrder.getOrderDate(), 0);
                    double  taxPercent=0;
                    if(purchaseOrder.getTax()!=null){
                        requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                        requestParams.put("taxid", purchaseOrder.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        taxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }
                    double orderAmount=(Double) bAmt.getEntityList().get(0);
                    double ordertaxamount=(taxPercent==0?0:orderAmount*taxPercent/100);
                    obj.put("taxpercent", taxPercent);
                    obj.put("taxamount",ordertaxamount );
                    obj.put("orderamount",orderAmount );
                    obj.put("orderamountwithTax",orderAmount+ordertaxamount);
                    obj.put("amount", amount);
                    obj.put("personname", vendor.getName());
                    obj.put("memo", purchaseOrder.getMemo());
                    obj.put("shiplengthval", purchaseOrder.getShiplength());
                    obj.put("taxid", purchaseOrder.getTax()==null?"":purchaseOrder.getTax().getID());
                    obj.put("taxname", purchaseOrder.getTax()==null?"":purchaseOrder.getTax().getName());
                    obj.put("costcenterid", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getID());
                    obj.put("costcenterName", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getName());
                    obj.put("billto", purchaseOrder.getBillTo()==null?"":purchaseOrder.getBillTo());
                    obj.put("shipto", purchaseOrder.getShipTo()==null?"":purchaseOrder.getShipTo());
                    obj.put("gstIncluded", purchaseOrder.isGstIncluded());
                    obj.put("termid", purchaseOrder.getTerm()==null?"":purchaseOrder.getTerm().getID());
                    obj.put("externalcurrencyrate", purchaseOrder.getExternalCurrencyRate());
                    obj.put("agent", purchaseOrder.getMasteragent()==null ? "" : purchaseOrder.getMasteragent().getID());
                    if(purchaseOrder.getModifiedby()!=null){
                        obj.put("lasteditedby",StringUtil.getFullName(purchaseOrder.getModifiedby()));
                    }
                    boolean includeprotax = false;
                    Set<PurchaseOrderDetail> purchaseOrderDetails = purchaseOrder.getRows();
                    for (PurchaseOrderDetail purchaseOrderDetail : purchaseOrderDetails) {
                         if (purchaseOrderDetail.getTax() != null) {
                            includeprotax = true;
                            break;
                        }
                    }
                    obj.put("includeprotax", includeprotax);
                    String status = "";
                    if(exceptFlagINV) {
                        Iterator itr1 = purchaseOrder.getRows().iterator();
                        status = "Closed";
                        while(itr1.hasNext()) {
                            PurchaseOrderDetail row = (PurchaseOrderDetail) itr1.next();
                            double addobj = doflag ?  row.getBalanceqty() : accPurchaseOrderServiceDAOobj.getPurchaseOrderDetailStatus(row);   
                            if (addobj > 0) {
                                status = "Open";
                                break;
                            }
                        }
                    } else {
                        status = (doflag)? getPurchaseOrderStatusForGRO(purchaseOrder) : accPurchaseOrderServiceDAOobj.getPurchaseOrderStatus(purchaseOrder);
                    }
                    obj.put("status",status);
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    PurchaseOrderCustomData jeDetailCustom = (PurchaseOrderCustomData) purchaseOrder.getPoCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue()!=null?varEntry.getValue().toString():"";
                            if (customFieldMap.containsKey(varEntry.getKey())) {
                                String value = "";
                                String Ids[] = coldata.split(",");
                                for (int i = 0; i < Ids.length; i++) {
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                    if (fieldComboData != null) {
                                        if(fieldComboData.getField().getFieldtype()==12){
//                                            value += Ids[i] != null ? Ids[i] + "," : ",";
                                            value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                        }else{
                                            value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                        }

                                    }
                                }
                                if (!StringUtil.isNullOrEmpty(value)) {
                                    value = value.substring(0, value.length() - 1);
                                }
                                obj.put(varEntry.getKey(), value);
                            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                try {
                                    dateFromDB = defaultDateFormat.parse(coldata);
                                    coldata = sdf.format(dateFromDB);
                                } catch (Exception e) {
                                }
                                obj.put(varEntry.getKey(), coldata);
                            } else {
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    String[] coldataArray = coldata.split(",");
                                        String Coldata="";
                                        for(int countArray=0;countArray < coldataArray.length ; countArray++){
                                            Coldata+="'"+coldataArray[countArray]+"',";
                                    }
                                        Coldata=Coldata.substring(0,Coldata.length()-1);
                                        String ColValue=accAccountDAOobj.getfieldcombodatabyids(Coldata);
                                        obj.put(varEntry.getKey(),coldata );
                                        obj.put(varEntry.getKey() + "_Values", ColValue );
                                }
                            }
                        }
                    }
                    boolean addFlag = true;
                    if(closeflag && purchaseOrder.isDeleted()){
                        addFlag = false;
                    } else if (closeflag && status.equalsIgnoreCase("Closed")) {
                        addFlag = false;
                    } else if(status.equalsIgnoreCase("QA Failed") || status.equalsIgnoreCase("Pending QA Approval")){
                        addFlag = false;
                    }
                    if(addFlag){
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch (Exception ex){
            throw ServiceException.FAILURE("getPurchaseOrdersJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }
    
    
    public JSONArray getTermDetails(String id,boolean isOrder) throws ServiceException {
       JSONArray jArr=new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            if(isOrder){
                requestParam.put("purchaseOrder", id);
                KwlReturnObject curresult = accPurchaseOrderobj.getPurchaseOrderTermMap(requestParam);
                List<PurchaseOrderTermMap> termMap = curresult.getEntityList();
                for(PurchaseOrderTermMap purchaseOrderTermMap : termMap) {
                    InvoiceTermsSales mt = purchaseOrderTermMap.getTerm();
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("id", mt.getId());
                    jsonobj.put("term", mt.getTerm());
                    jsonobj.put("glaccount", mt.getAccount().getID());
                    jsonobj.put("sign", mt.getSign());
                    jsonobj.put("formula", mt.getFormula());
                    jsonobj.put("termpercentage", purchaseOrderTermMap.getPercentage());
                    jsonobj.put("termamount", purchaseOrderTermMap.getTermamount());
                    jArr.put(jsonobj);
                }
            }else{
                requestParam.put("vendorQuotation", id);
                KwlReturnObject curresult = accPurchaseOrderobj.getVendorQuotationTermMap(requestParam);
                List<VendorQuotationTermMap> termMap = curresult.getEntityList();
                for(VendorQuotationTermMap vendorQuotationTermMap : termMap) {
                    InvoiceTermsSales mt = vendorQuotationTermMap.getTerm();
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("id", mt.getId());
                    jsonobj.put("term", mt.getTerm());
                    jsonobj.put("glaccount", mt.getAccount().getID());
                    jsonobj.put("sign", mt.getSign());
                    jsonobj.put("formula", mt.getFormula());
                    jsonobj.put("termpercentage", vendorQuotationTermMap.getPercentage());
                    jsonobj.put("termamount", vendorQuotationTermMap.getTermamount());
                    jArr.put(jsonobj);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    /**
     * Description To get json data for security gate entry
     * @param requestParams
     * @param list
     * @return
     * @throws ServiceException 
     */
    public JSONObject getSecurityGateJsonOptimized(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyid = requestParams.get("companyid").toString();
            boolean doflag = (Boolean) requestParams.get("doflag");

            Iterator itr = list.iterator();
            
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_SecurityGateEntry_ModuleId));
            
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            
            KWLCurrency currency = null;
            
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            
            while (itr.hasNext()) {
                SecurityGateEntry securityGateEntry = (SecurityGateEntry) itr.next();
                String status = "";
                boolean addFlag = true;
                if (addFlag) {
                    Vendor vendor = securityGateEntry.getVendor();
                    if (securityGateEntry.getCurrency() != null) {
                        currency = securityGateEntry.getCurrency();
                    } else {
                        currency = vendor.getAccount().getCurrency() == null ? kwlcurrency : vendor.getAccount().getCurrency();
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("billid", securityGateEntry.getID());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("personid", vendor.getID());
                    obj.put("billno", securityGateEntry.getSecurityNumber());
                    obj.put("duedate", df.format(securityGateEntry.getDueDate()));
                    obj.put("date", df.format(securityGateEntry.getSecurityDate())); //SDP-2245
                    obj.put("shipdate", securityGateEntry.getShipdate() == null ? "" : df.format(securityGateEntry.getShipdate()));
                    obj.put("shipvia", securityGateEntry.getShipvia() == null ? "" : securityGateEntry.getShipvia());
                    obj.put("fob", securityGateEntry.getFob() == null ? "" : securityGateEntry.getFob());
                    obj.put("termdetails", getTermDetails(securityGateEntry.getID(), true));

                    obj = AccountingAddressManager.getTransactionAddressJSON(obj, securityGateEntry.getBillingShippingAddresses(), true);
                    boolean includeprotax = false;
                    double amount = 0,totalDiscount = 0, discountPrice = 0;

                    for (SecurityGateDetails pod : securityGateEntry.getRows()) {
                        if (pod.getTax() != null) {
                            includeprotax = true;
                        }
                        
                        double rate = authHandler.roundUnitPrice(pod.getRate(), companyid);
                        if (securityGateEntry.isGstIncluded()) {
                            rate = pod.getRateincludegst();
                        }
                        
                        double quantity = authHandler.roundQuantity(pod.getQuantity(), companyid);

                        double poPrice = authHandler.round(quantity * rate, companyid);
                        double discountPOD = authHandler.round(pod.getDiscount(), companyid);
                        if (pod.getDiscountispercent() == 1) {
                            discountPrice = (poPrice) - authHandler.round((poPrice * discountPOD / 100), companyid);
                        } else {
                            discountPrice = poPrice - discountPOD;
                        }
                        
                        amount += discountPrice;
                        if (!securityGateEntry.isGstIncluded()) {
                            amount += authHandler.round(pod.getRowTaxAmount(), companyid);
                        }
                    }
                    double totalGlobalTermAmount = 0, taxableGlobalTermamount = 0; //Global Term Amount

                    double taxPercent = 0;
                    
                    if (securityGateEntry.getTax() != null) {
                        requestParams.put("transactiondate", securityGateEntry.getSecurityDate());
                        requestParams.put("taxid", securityGateEntry.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                    }
                    
                    double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round(((amount + taxableGlobalTermamount) * taxPercent / 100), companyid));

                    obj.put("amount", (amount + totalGlobalTermAmount + ordertaxamount));
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, (amount + totalGlobalTermAmount + ordertaxamount), securityGateEntry.getCurrency().getCurrencyID(), securityGateEntry.getSecurityDate(), securityGateEntry.getExternalCurrencyRate());
                    obj.put("amountinbase", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));

                    obj.put("currencysymbol", currency.getSymbol());
                    
                    if (securityGateEntry.getDiscount() != 0) {
                        if (securityGateEntry.isPerDiscount()) {
                            totalDiscount = amount * securityGateEntry.getDiscount() / 100;
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - securityGateEntry.getDiscount();
                            totalDiscount = securityGateEntry.getDiscount();
                        }
                        obj.put("discounttotal", securityGateEntry.getDiscount());
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    
                    obj.put("discount", totalDiscount);
                    obj.put("discountispertotal", securityGateEntry.isPerDiscount());

                    obj.put("personname", vendor.getName());
                    obj.put("memo", securityGateEntry.getMemo());
                    obj.put("shiplengthval", securityGateEntry.getShiplength());
                    obj.put("taxid", securityGateEntry.getTax() == null ? "" : securityGateEntry.getTax().getID());
                    obj.put("taxname", securityGateEntry.getTax() == null ? "" : securityGateEntry.getTax().getName());
                    obj.put("costcenterid", securityGateEntry.getCostcenter() == null ? "" : securityGateEntry.getCostcenter().getID());
                    obj.put("costcenterName", securityGateEntry.getCostcenter() == null ? "" : securityGateEntry.getCostcenter().getName());
                    obj.put("billto", securityGateEntry.getBillTo() == null ? "" : securityGateEntry.getBillTo());
                    obj.put("shipto", securityGateEntry.getShipTo() == null ? "" : securityGateEntry.getShipTo());
                    obj.put("gstIncluded", securityGateEntry.isGstIncluded());
                    obj.put("termid", securityGateEntry.getTerm() == null ? "" : securityGateEntry.getTerm().getID());
                     obj.put("externalcurrencyrate", securityGateEntry.getExternalCurrencyRate());
                    obj.put("agent", securityGateEntry.getMasteragent() == null ? "" : securityGateEntry.getMasteragent().getID());
                    
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    SecurityGateEntryCustomData securityGateEntryCustomData = (SecurityGateEntryCustomData) securityGateEntry.getSgeCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    if (securityGateEntryCustomData != null) {
                        AccountingManager.setCustomColumnValues(securityGateEntryCustomData, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyid);
                        boolean linkflag = false;
                        if (requestParams.containsKey("linkflag") && requestParams.get("linkflag") != null) {
                            linkflag = Boolean.FALSE.parseBoolean(requestParams.get("linkflag").toString());
                        }
                        if (doflag) {
                            params.put("isLink", true);
                            int moduleId =Constants.Acc_Goods_Receipt_ModuleId;
                                                    
                            params.put("linkModuleId", moduleId);
                            params.put("customcolumn", 0);
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    
                    if (securityGateEntry.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(securityGateEntry.getModifiedby()));
                    }
                    
                    obj.put("includeprotax", includeprotax);
                    obj.put("status", status);
                    obj.put(Constants.SUPPLIERINVOICENO, securityGateEntry.getSupplierInvoiceNo() != null ? securityGateEntry.getSupplierInvoiceNo() : "");
                    
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
            System.out.println("Total count " + jArr.length());
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPurchaseOrdersJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    public JSONObject getPurchaseOrdersJsonOptimized(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyid = requestParams.get("companyid").toString();
            KwlReturnObject pref = accountingHandlerDAOobj.loadObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) pref.getEntityList().get(0);
            boolean closeflag = (Boolean) requestParams.get("closeflag");
            boolean doflag = (Boolean) requestParams.get("doflag");
            boolean exceptFlagINV = false;
            boolean isForJobWorkOutModule=false;
            if (requestParams.containsKey("exceptFlagINV") && requestParams.get("exceptFlagINV") != null) {
                exceptFlagINV = Boolean.parseBoolean((String) requestParams.get("exceptFlagINV"));
            }
            boolean isFixedAsset = false;
            if (requestParams.containsKey("isFixedAsset") && requestParams.get("isFixedAsset") != null) {
                isFixedAsset = Boolean.parseBoolean(requestParams.get("isFixedAsset").toString());
            }
            boolean isForSGELink = false;
             /*
               isForSGELink flag is used in case of linking PO in SGE
            */
            boolean isForJobWorkOutLinkingInGRN = false;
            if (requestParams.containsKey("isForJobWorkOutLinkingInGRN") && requestParams.get("isForJobWorkOutLinkingInGRN") != null) {
                isForJobWorkOutLinkingInGRN = Boolean.parseBoolean(requestParams.get("isForJobWorkOutLinkingInGRN").toString());
            }
            
             boolean isjobworkwitoutgrn = false;
            
            if (requestParams.containsKey("isjobworkwitoutgrn") && requestParams.get("isjobworkwitoutgrn") != null) {
                isjobworkwitoutgrn =(Boolean) requestParams.get("isjobworkwitoutgrn");
            }
            /*
            Linking cases of Job Work Order In PI and Good Receipt
            */
            boolean isJobWorkOrderInPI = false;
            if (requestParams.containsKey("isJobWorkOrderInPI") && requestParams.get("isJobWorkOrderInPI") != null) {
                isJobWorkOrderInPI = Boolean.parseBoolean(requestParams.get("isJobWorkOrderInPI").toString());
            }            
            if (requestParams.containsKey("isForSGELink") && requestParams.get("isForSGELink") != null) {
                isForSGELink = Boolean.parseBoolean(requestParams.get("isForSGELink").toString());
            }
            boolean isConsignment = false;
            if (requestParams.containsKey("isConsignment")) {
                isConsignment = Boolean.FALSE.parseBoolean(requestParams.get("isConsignment").toString());
            }
            boolean isShowAddress=false;
            if (requestParams.containsKey("isShowAddress")) {
                isShowAddress = Boolean.FALSE.parseBoolean(requestParams.get("isShowAddress").toString());
            }
             boolean isGenerateOrderFromOrder=false;
            /*------isGenerateOrderFromOrder comes true when generating SO from PO----------- */
             if (requestParams.containsKey("isGenerateOrderFromOrder") && requestParams.get("isGenerateOrderFromOrder") != null) {
                isGenerateOrderFromOrder = Boolean.parseBoolean(requestParams.get("isGenerateOrderFromOrder").toString());
            }
            isForJobWorkOutModule=(isForJobWorkOutLinkingInGRN || isJobWorkOrderInPI); 
            DateFormat userdf=(DateFormat)requestParams.get(Constants.userdf);
            int moduleid=-1;
            if(requestParams.containsKey("requestModuleid")){
                moduleid=Integer.parseInt(requestParams.get("requestModuleid").toString());
            }
            
            Iterator itr = list.iterator();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, isForJobWorkOutModule ? Constants.JOB_WORK_OUT_ORDER_MODULEID: isConsignment ? Constants.Acc_ConsignmentVendorRequest_ModuleId :isFixedAsset ? Constants.Acc_FixedAssets_Purchase_Order_ModuleId:Constants.Acc_Purchase_Order_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KWLCurrency currency = null;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            while (itr.hasNext()) {
                PurchaseOrder purchaseOrder = (PurchaseOrder) itr.next();
                String status = "";
                boolean addFlag = true;
                boolean isOpen = purchaseOrder.isIsOpen(); // Status of PO 
                if (!isShowAddress) {//when call came from show address component we need all queried data so no need to check for addflag
                    if (closeflag && purchaseOrder.isDeleted()) {
                        addFlag = false;
                } else if (closeflag && !isOpen && moduleid != Constants.Acc_Sales_Order_ModuleId) { //(closeflag && status.equalsIgnoreCase("Closed"))
                    /*
                         * While linking Purchase Order in Sales Order, we
                         * should show all the Purchase Orders even if it is
                         * linked with Purchase Invoice or Goods Receipt
                         */
                        addFlag = false;
                } else if (!isOpen && moduleid != Constants.Acc_Sales_Order_ModuleId) { //  (status.equalsIgnoreCase("QA Failed") || status.equalsIgnoreCase("Pending QA Approval")) 
                    /*
                         * While linking Purchase Order in Sales Order, we
                         * should show all the Purchase Orders even if it is
                         * linked with Purchase Invoice or Goods Receipt
                         */
                        addFlag = false;
                    }
                }
                
                JSONObject columnprefObj = new JSONObject();
                KwlReturnObject extracapresult = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                    columnprefObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                }
                boolean activatedropship = false;
                if (columnprefObj.has("activatedropship") && columnprefObj.get("activatedropship") != null && (Boolean) columnprefObj.get("activatedropship") != false) {
                    activatedropship = true;
                }

                /*----- Dropship type is not being loaded in Invoice --------- */
                if (!activatedropship && purchaseOrder.isIsDropshipDocument()) {
                    addFlag = false;
                }
                
                if (addFlag||isjobworkwitoutgrn) {
                    Vendor vendor = purchaseOrder.getVendor();
                    if (purchaseOrder.getCurrency() != null) {
                        currency = purchaseOrder.getCurrency();
                    } else {
                        currency = vendor.getAccount().getCurrency() == null ? kwlcurrency : vendor.getAccount().getCurrency();
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("billid", purchaseOrder.getID());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("personid", vendor.getID());
                    obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                    obj.put("duedate", df.format(purchaseOrder.getDueDate()));
                    obj.put("date", df.format(purchaseOrder.getOrderDate())); //SDP-2245
                    obj.put("shipdate", purchaseOrder.getShipdate() == null ? "" : df.format(purchaseOrder.getShipdate()));
                    obj.put("shipvia", purchaseOrder.getShipvia() == null ? "" : purchaseOrder.getShipvia());
                    obj.put("fob", purchaseOrder.getFob() == null ? "" : purchaseOrder.getFob());
                    obj.put("termdetails", accPurchaseOrderServiceDAOobj.getTermDetails(purchaseOrder.getID(), true));
                    obj.put("isexpenseinv", purchaseOrder.isIsExpenseType());
                    obj.put("gtaapplicable", purchaseOrder.isGtaapplicable());
                    /**
                     * Put Merchant Exporter Check
                     */
                    obj.put(Constants.isMerchantExporter, purchaseOrder.isIsMerchantExporter());
                    obj.put("isDisabledPOforSO",purchaseOrder.isDisabledPOforSO());     //Passing the Status of Purchase order whether it is blocked for linking in SO ERP-35541 
                    obj.put("isdropshipchecked", purchaseOrder.isIsDropshipDocument());
                    if(purchaseOrder.isIsMRPJobWorkOut()){ 
                         obj.put("productid",purchaseOrder.getProduct() != null ? purchaseOrder.getProduct().getID() :"");
                         obj.put("isMRPJOBWORKOUT",purchaseOrder.isIsMRPJobWorkOut());
                    }
                    obj=AccountingAddressManager.getTransactionAddressJSON(obj,purchaseOrder.getBillingShippingAddresses(),true);
                    boolean includeprotax = false;
                    double amount = 0,totalDiscount = 0, discountPrice = 0;
                   
                    if(purchaseOrder.isIsExpenseType()){//** For Expense grid
                        for (ExpensePODetail epod : purchaseOrder.getExpenserows()) {
                            double rowAmount = epod.getRate();//this is user enter amount gainst column Amount on which discount can applied
                            double rowTax = 0;
                            double rowDiscount = 0;

                            //Discount
                            if (epod.getDiscount() != null) {
                                rowDiscount = epod.getDiscount().getDiscountValue();// adding discount to get total row level discount amount
                            }
                            //Tax
                            if (epod.getTax() != null) {//line level tax is given
                                includeprotax = true;
                                rowTax = epod.getRowTaxAmount();
                            }

                            if (purchaseOrder.isGstIncluded()) {//in including gst, tax already included so no need to add seperetaly
                                if (epod.isIsdebit()) {//if debit type then amount will be added
                                    amount += (rowAmount - rowDiscount);//subtracting discount to calculate total amount from all line level    
                                } else {//id debit type then amount will be subtracted
                                    amount -= (rowAmount - rowDiscount);//subtracting discount to calculate total amount from all line level    
                                }
                            } else {
                                if (epod.isIsdebit()) {//if debit type then amount will be added
                                    amount += (rowAmount - rowDiscount + rowTax);//subtracting discount and then sum tax to find total amount from all line level    
                                } else {//if debit type then amount will be subtracted
                                    amount -= (rowAmount - rowDiscount + rowTax);//subtracting discount and then sum tax to find total amount from all line level    
                                }
                            }
                        }
                    } else {//** For Product Grid
                        for (PurchaseOrderDetail pod :purchaseOrder.getRows()) {
                            if (pod.getTax() != null) {
                                includeprotax = true;
                            }
                            double rate = authHandler.roundUnitPrice(pod.getRate(),companyid);
                            if (purchaseOrder.isGstIncluded()) {
                                rate = pod.getRateincludegst();
                            }
                            double quantity = authHandler.roundQuantity(pod.getQuantity(), companyid);

                            double poPrice = authHandler.round(quantity * rate, companyid);
                            double discountPOD = authHandler.round(pod.getDiscount(), companyid);
                            if (pod.getDiscountispercent() == 1) {
                                discountPrice = (poPrice) - authHandler.round((poPrice * discountPOD / 100), companyid);
                            } else {
                                discountPrice = poPrice - discountPOD;
                            }

                            amount += discountPrice;
                            if (!purchaseOrder.isGstIncluded()) {
                                amount += authHandler.round(pod.getRowTaxAmount(), companyid);
                            }
                        }
                    }
                    double totalGlobalTermAmount = 0, taxableGlobalTermamount = 0; //Global Term Amount
                    HashMap<String, Object> requestParam = new HashMap();
                    requestParam.put("purchaseOrder", purchaseOrder.getID());
                    HashMap<String, Object> filterrequestParams = new HashMap();
                    KwlReturnObject purchaseOrderResult = null;
                    purchaseOrderResult = accPurchaseOrderobj.getPurchaseOrderTermMap(requestParam);
                    filterrequestParams.put("taxid", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getID());
                    List<PurchaseOrderTermMap> termMap = purchaseOrderResult.getEntityList();
                    for (PurchaseOrderTermMap purchaseOrderTermMap : termMap) {
                        filterrequestParams.put("term", purchaseOrderTermMap.getTerm() == null ? "" : purchaseOrderTermMap.getTerm().getId());
                        double GlobaltermAmnt = purchaseOrderTermMap.getTermamount();
                        totalGlobalTermAmount += GlobaltermAmnt;
                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);

                        if (isTermMappedwithTax) {
                            taxableGlobalTermamount += GlobaltermAmnt;
                        }
                    }

                    double taxPercent = 0;
                    if (purchaseOrder.getTax() != null) {
                        requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                        requestParams.put("taxid", purchaseOrder.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        if (taxList != null && !taxList.isEmpty()) { 
                            Object[] taxObj = (Object[]) taxList.get(0);
                            taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        }  
                    }
                    double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round(((amount + taxableGlobalTermamount) * taxPercent / 100), companyid));

                    obj.put("amount", (amount+totalGlobalTermAmount+ordertaxamount));
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, (amount+totalGlobalTermAmount+ordertaxamount), purchaseOrder.getCurrency().getCurrencyID(), purchaseOrder.getOrderDate(), purchaseOrder.getExternalCurrencyRate());
                    obj.put("amountinbase", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                    
                    obj.put("currencysymbol", currency.getSymbol());
                    if (purchaseOrder.getDiscount() != 0) {
                        if (purchaseOrder.isPerDiscount()) {
                            totalDiscount = amount * purchaseOrder.getDiscount() / 100;
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - purchaseOrder.getDiscount();
                            totalDiscount = purchaseOrder.getDiscount();
                        }
                        obj.put("discounttotal", purchaseOrder.getDiscount());
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    obj.put("discount", totalDiscount);
                    obj.put("discountispertotal", purchaseOrder.isPerDiscount());
                    obj.put("personname", vendor.getName());
                    obj.put("memo", purchaseOrder.getMemo());
                    obj.put("shiplengthval", purchaseOrder.getShiplength());
                    obj.put("taxid", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getID());
                    obj.put("taxname", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getName());
                    obj.put("costcenterid", purchaseOrder.getCostcenter() == null ? "" : purchaseOrder.getCostcenter().getID());
                    obj.put("costcenterName", purchaseOrder.getCostcenter() == null ? "" : purchaseOrder.getCostcenter().getName());
                    obj.put("billto", purchaseOrder.getBillTo() == null ? "" : purchaseOrder.getBillTo());
                    obj.put("shipto", purchaseOrder.getShipTo() == null ? "" : purchaseOrder.getShipTo());
                    obj.put("gstIncluded", purchaseOrder.isGstIncluded());
                    obj.put("termid", purchaseOrder.getTerm() == null ? "" : purchaseOrder.getTerm().getID());
                    obj.put("externalcurrencyrate", purchaseOrder.getExternalCurrencyRate());
                    obj.put("agent", purchaseOrder.getMasteragent() == null ? "" : purchaseOrder.getMasteragent().getID());
                    obj.put("agentname", purchaseOrder.getMasteragent() == null ? "" : purchaseOrder.getMasteragent().getValue());
                    if (purchaseOrder.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(purchaseOrder.getModifiedby()));
                    }
                    obj.put("includeprotax", includeprotax);
                    obj.put("isapplytaxtoterms", purchaseOrder.isApplyTaxToTerms());
                    obj.put("status", status);
                    obj.put(Constants.SUPPLIERINVOICENO, purchaseOrder.getSupplierInvoiceNo() != null ? purchaseOrder.getSupplierInvoiceNo() : "");
                    obj.put(Constants.IsRoundingAdjustmentApplied, purchaseOrder.isIsRoundingAdjustmentApplied());
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    PurchaseOrderCustomData jeDetailCustom = (PurchaseOrderCustomData) purchaseOrder.getPoCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyid);
                        boolean linkflag = false;
                        if (requestParams.containsKey("linkflag") && requestParams.get("linkflag") != null) {
                            linkflag = Boolean.FALSE.parseBoolean(requestParams.get("linkflag").toString());
                        }
                        /*------isGenerateOrderFromOrder comes true when generating SO from PO----------- */
                        if (doflag || exceptFlagINV || linkflag || isGenerateOrderFromOrder || isForSGELink) {
                            params.put("isLink", true);
                            int moduleId = doflag ? Constants.Acc_Goods_Receipt_ModuleId : exceptFlagINV ? Constants.Acc_Vendor_Invoice_ModuleId : Constants.Acc_Sales_Order_ModuleId;
                            if (isConsignment) {
                                moduleId = Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId;
                            }
                            if (isFixedAsset) {
                                moduleId = Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId;
                            }
                            if (isGenerateOrderFromOrder) {
                                moduleId = Constants.Acc_Sales_Order_ModuleId;
                            }
                            if (isForSGELink) {
                                moduleId = Constants.Acc_SecurityGateEntry_ModuleId;
                            }
                            if (isJobWorkOrderInPI) {
                                moduleId = Constants.Acc_Vendor_Invoice_ModuleId;
                            }                            
                            params.put("linkModuleId", moduleId);
                            params.put("customcolumn", 0);
                        }
                            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
            System.out.println("Total count " + jArr.length());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPurchaseOrdersJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    public String getPurchaseOrderStatusForGRO(PurchaseOrder so) throws ServiceException {

        String result = "Closed";

        //Check Integration and QA Approval flow then check status
        int status = accPurchaseOrderServiceDAOobj.getQAPOStatus(so);
        if (status == Constants.Pending_QA_Approval) {
            result = "Pending QA Approval";
            return result;
        } else if (status == Constants.QA_Rejected) {
            result = "QA Failed";
            return result;
        }
        Set<PurchaseOrderDetail> orderDetail = so.getRows();

        for (PurchaseOrderDetail poDetail:orderDetail) {
            if (poDetail.getQastatus() != Constants.QA_Rejected) {
                if (poDetail.getBalanceqty() > 0) {
                    result = "Open";
                    break;
                }
            }
        }
        return result;
    }
    public String getPurchaseOrderStatusForGRONew(Set<PurchaseOrderDetail> orderDetail) throws ServiceException {
        String result = "Closed";
        //Check Integration and QA Approval flow then check status
        int status = accPurchaseOrderServiceDAOobj.getQAPOStatusUsingPODetails(orderDetail);
        if (status == Constants.Pending_QA_Approval) {
            result = "Pending QA Approval";
            return result;
        } else if (status == Constants.QA_Rejected) {
            result = "QA Failed";
            return result;
        }
        for (PurchaseOrderDetail soDetail:orderDetail) {
            double qua = 0;
            if (soDetail.getQastatus() != Constants.QA_Rejected) {
                KwlReturnObject idresult = accGoodsReceiptobj.getGROIDFromPOD(soDetail.getID());
                List<GoodsReceiptOrderDetails> grdList = idresult.getEntityList();
                for (GoodsReceiptOrderDetails ge:grdList) {
                    qua += ge.getInventory().getQuantity();
                }
                if (qua < soDetail.getQuantity()) {
                    result = "Open";
                    break;
                }
            }
        }
        return result;
    }

    public ModelAndView getPurchaseOrderRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            
            // Get line-level CUSTOMFIELDS for Purchase Order
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            if (request.getParameter("bills") != null) {
                requestParams.put("bills", request.getParameter("bills").split(","));
            }
            if (request.getParameter("isJobWorkOutRemain") != null) {
                requestParams.put("isJobWorkOutRemain", request.getParameter("isJobWorkOutRemain"));
            }
            if (request.getParameter("prodIds") != null) {
                requestParams.put("prodIds", request.getParameterValues("prodIds"));
            }
            requestParams.put("closeflag", request.getParameter("closeflag"));
            requestParams.put("moduleid", request.getParameter("moduleid"));
            requestParams.put("doflag", request.getParameter("doflag")!=null?true:false);
            requestParams.put("dateFormatValue", authHandler.getDateOnlyFormat());
            boolean isConsignment= StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))?false:Boolean.parseBoolean(request.getParameter("isConsignment"));
            requestParams.put("isConsignment", isConsignment);
            boolean isForDOGROLinking = (StringUtil.isNullOrEmpty(request.getParameter("isForDOGROLinking")))?false:Boolean.parseBoolean(request.getParameter("isForDOGROLinking"));
            requestParams.put("isForDOGROLinking", isForDOGROLinking);
            boolean isForSGELinking=(StringUtil.isNullOrEmpty(request.getParameter("isForSGELinking")))?false:Boolean.parseBoolean(request.getParameter("isForSGELinking"));
            requestParams.put("isForSGELinking", isForSGELinking);
            boolean isForInvoice = (StringUtil.isNullOrEmpty(request.getParameter("isForInvoice")))?false:Boolean.parseBoolean(request.getParameter("isForInvoice"));
            requestParams.put("isForInvoice", isForInvoice);
            boolean sopolinkflag = (StringUtil.isNullOrEmpty(request.getParameter(Constants.POSOFLAG)))?false:Boolean.parseBoolean(request.getParameter(Constants.POSOFLAG));
            requestParams.put(Constants.POSOFLAG, sopolinkflag);
            boolean isFixedAsset = Boolean.FALSE.parseBoolean(request.getParameter("isFixedAsset"));
            requestParams.put("isFixedAsset", isFixedAsset);
            boolean FA_POlinkToFA_PI = Boolean.FALSE.parseBoolean(request.getParameter("FA_POlinkToFA_PI"));
            requestParams.put("FA_POlinkToFA_PI", FA_POlinkToFA_PI);
            boolean isForJobWorkOut=(StringUtil.isNullOrEmpty(request.getParameter("isForJobWorkOut")))?false:Boolean.parseBoolean(request.getParameter("isForJobWorkOut"));            
            requestParams.put("isForJobWorkOut", isForJobWorkOut);
            boolean isJobWorkOutLinkedWithGRN=(StringUtil.isNullOrEmpty(request.getParameter("isJobWorkOutLinkedWithGRN")))?false:Boolean.parseBoolean(request.getParameter("isJobWorkOutLinkedWithGRN"));                        
            requestParams.put("isJobWorkOutLinkedWithGRN", isJobWorkOutLinkedWithGRN);
            boolean isJobWorkOutLinkedWithPI=(StringUtil.isNullOrEmpty(request.getParameter("isJobWorkOutLinkedWithPI")))?false:Boolean.parseBoolean(request.getParameter("isJobWorkOutLinkedWithPI"));                        
            requestParams.put("isJobWorkOutLinkedWithPI", isJobWorkOutLinkedWithPI);
            boolean isForLinking=Boolean.FALSE.parseBoolean(request.getParameter("isForLinking"));
            requestParams.put("isForLinking", isForLinking);
            boolean isJobWorkStockOut = (StringUtil.isNullOrEmpty(request.getParameter("isJobWorkStockOut")))?false:Boolean.parseBoolean(request.getParameter("isJobWorkStockOut"));
            requestParams.put("isJobWorkStockOut", isJobWorkStockOut);
            String storeId=request.getParameter("storeId");
            if(!StringUtil.isNullOrEmpty(storeId)){
                requestParams.put("storeId", storeId);
            }
            
            String dtype = request.getParameter("dtype");
            
            boolean isForReport = false;
            
            if(!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")){
                isForReport = true;
            }
            requestParams.put("isForReport", isForReport);
            boolean isExpenseType = StringUtil.isNullOrEmpty(request.getParameter("isexpenseinv"))?false:Boolean.parseBoolean(request.getParameter("isexpenseinv"));// will be true for expense PO
            boolean isCopy = !StringUtil.isNullOrEmpty(request.getParameter("iscopy")) ? Boolean.parseBoolean(request.getParameter("iscopy")) : false;
            requestParams.put("isExpenseType", isExpenseType);
            requestParams.put("iscopy", isCopy);
            
            boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag"))) ? false : Boolean.parseBoolean(request.getParameter("linkingFlag"));
            requestParams.put("linkingFlag", linkingFlag);
            if(request.getParameter("requestModuleid")!=null && !StringUtil.isNullOrEmpty(request.getParameter("requestModuleid"))){
                requestParams.put("requestModuleid",request.getParameter("requestModuleid"));
            }
              /*--- generateInvoiceFromTransactionForms ->Flag is true if invoice is generated from PO form------*/
            boolean generateInvoiceFromTransactionForms = (StringUtil.isNullOrEmpty(request.getParameter("generateInvoiceFromTransactionForms")))?false:Boolean.parseBoolean(request.getParameter("generateInvoiceFromTransactionForms"));
            /*
            'isSOfromPO' flag is used to get custom field or dimension while generating SO from PO
            */
            boolean isSOfromPO = (StringUtil.isNullOrEmpty(request.getParameter("isSOfromPO")))?false:Boolean.parseBoolean(request.getParameter("isSOfromPO"));
             requestParams.put("generateInvoiceFromTransactionForms",generateInvoiceFromTransactionForms);
             requestParams.put("isSOfromPO",isSOfromPO);
             JSONArray DataJArr = accPurchaseOrderServiceDAOobj.getPurchaseOrderRows(requestParams);
            jobj.put("data", DataJArr);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getPurchaseOrderRows : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Description To get line level data of security gate entry form.
     * @param request
     * @param response
     * @return 
     */
     public ModelAndView getSecurityGateEntryRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("bills", request.getParameter("bills").split(","));
            requestParams.put("closeflag", request.getParameter("closeflag"));
            requestParams.put("moduleid", request.getParameter("moduleid"));
            requestParams.put("doflag", request.getParameter("doflag")!=null?true:false);
            requestParams.put("dateFormatValue", authHandler.getDateOnlyFormat());
            boolean isForDOGROLinking = (StringUtil.isNullOrEmpty(request.getParameter("isForDOGROLinking")))?false:Boolean.parseBoolean(request.getParameter("isForDOGROLinking"));
            requestParams.put("isForDOGROLinking", isForDOGROLinking);
            
            boolean isForLinking=Boolean.FALSE.parseBoolean(request.getParameter("isForLinking"));
            requestParams.put("isForLinking", isForLinking);
            
            String dtype = request.getParameter("dtype");
            
            boolean isForReport = false;
            
            if(!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")){
                isForReport = true;
            }
            requestParams.put("isForReport", isForReport);
            
            boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag"))) ? false : Boolean.parseBoolean(request.getParameter("linkingFlag"));
            requestParams.put("linkingFlag", linkingFlag);
            if(request.getParameter("requestModuleid")!=null && !StringUtil.isNullOrEmpty(request.getParameter("requestModuleid"))){
                requestParams.put("requestModuleid",request.getParameter("requestModuleid"));
            }
            
            boolean generateInvoiceFromTransactionForms = (StringUtil.isNullOrEmpty(request.getParameter("generateInvoiceFromTransactionForms")))?false:Boolean.parseBoolean(request.getParameter("generateInvoiceFromTransactionForms"));
            requestParams.put("generateInvoiceFromTransactionForms",generateInvoiceFromTransactionForms);
            JSONArray DataJArr = accPurchaseOrderServiceDAOobj.getSecurityGateEbtryOrderRows(requestParams);
            jobj.put("data", DataJArr);
            issuccess = true;
            
        } catch (SessionExpiredException | ServiceException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getPurchaseOrderRows : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getPurchaseOrderOtherDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("poid", request.getParameter("poid"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject podresult = accPurchaseOrderobj.getPurchaseOrderOtherDetails(requestParams);
            Iterator itr = podresult.getEntityList().iterator();

            while (itr.hasNext()) {
                POOtherDetails poData = (POOtherDetails) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("poyourref", poData.getPoyourref());
                obj.put("podelyterm", poData.getPodelyterm());
                obj.put("podelydate", poData.getPodelydate());
                obj.put("poinvoiceto", poData.getPoinvoiceto());
                obj.put("podept", poData.getPodept());
                obj.put("porequestor", poData.getPorequestor());
                obj.put("poproject", poData.getPoproject());
                obj.put("pomerno", poData.getPomerno());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getPurchaseOrderRows : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

public ModelAndView getPoLinkedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getPoLinkedInTransaction(request);
            issuccess = true;
        }catch (SessionExpiredException | ServiceException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        }  catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getPurchaseOrderRows : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getPoLinkedInTransaction(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj=new JSONObject();
         JSONArray jArr=new JSONArray();       
        try {
             String poid= request.getParameter("billid");
             boolean isExpenseInvoice = StringUtil.isNullOrEmpty(request.getParameter("isExpenseInvoice"))?false:Boolean.FALSE.parseBoolean(request.getParameter("isExpenseInvoice"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            requestParams.put("poid", poid);
            DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);
            requestParams.put("companyid", companyid);
            if(isExpenseInvoice){
                requestParams.put("onlyexpenseinv", "true");// Sending as a string because it trated as string 
            }
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(poid)) {   
             KwlReturnObject result = accGoodsReceiptobj.getGoodsReceiptsMerged(requestParams);
             List list = result.getEntityList();
            if (list != null && !list.isEmpty()) {
                //for(GoodsReceipt gReceipt:list){
                Iterator itr = list.iterator();
                while (itr.hasNext()) {

                    Object[] oj = (Object[]) itr.next();
                    String invid = oj[0].toString();
                     JSONObject obj = new JSONObject();
                    //Withoutinventory 0 for normal, 1 for billing
                    boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                    if (withoutinventory) {
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), invid);
                        BillingGoodsReceipt invoice = (BillingGoodsReceipt) objItr.getEntityList().get(0);

                        JournalEntry je = invoice.getJournalEntry();
                        JournalEntryDetail d = invoice.getVendorEntry();
                        Account account = d.getAccount();
                        obj.put("billid", invoice.getID());
                        obj.put("companyid", invoice.getCompany().getCompanyID());
                        obj.put("companyname", invoice.getCompany().getCompanyName());
                        obj.put("withoutinventory", withoutinventory);
                        obj.put("transactionNo", invoice.getBillingGoodsReceiptNumber());
                        obj.put("journalEntryId", je.getID());
                        obj.put("journalEntryNo", je.getEntryNumber());
                        obj.put("date", df.format(je.getEntryDate()));
                        obj.put("personname", invoice.getVendor() == null ? account.getName() : invoice.getVendor().getName());
                        obj.put("mergedCategoryData", "Vendor Invoice");  //type of data
                     
                    } else {
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
                        GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                        Vendor vendor=gReceipt.getVendor();
                        JournalEntry je = gReceipt.getJournalEntry();
                        JournalEntryDetail d = gReceipt.getVendorEntry();
                         Account account = d.getAccount();
                        obj.put("billid", gReceipt.getID());
                        obj.put("companyid", gReceipt.getCompany().getCompanyID());
                        obj.put("gtaapplicable", gReceipt.isGtaapplicable());
                        obj.put("companyname", gReceipt.getCompany().getCompanyName());
                        obj.put("withoutinventory", withoutinventory);
                        obj.put("transactionNo", gReceipt.getGoodsReceiptNumber());
                        obj.put(GoodsReceiptCMNConstants.BILLID, gReceipt.getID());
                        obj.put("isOpeningBalanceTransaction", gReceipt.isIsOpeningBalenceInvoice());
                        obj.put("isNormalTransaction", gReceipt.isNormalInvoice());
                        obj.put("parentinvoiceid", gReceipt.getParentInvoice()!=null?gReceipt.getParentInvoice().getID():"");
                        obj.put("companyid", gReceipt.getCompany().getCompanyID());
                        obj.put("companyname", gReceipt.getCompany().getCompanyName());
                        obj.put(Constants.IsRoundingAdjustmentApplied, gReceipt.isIsRoundingAdjustmentApplied());
                        obj.put("withoutinventory", withoutinventory);
                        obj.put(GoodsReceiptCMNConstants.PERSONID,vendor == null ? account.getID() : vendor.getID());
                        obj.put(GoodsReceiptCMNConstants.ALIASNAME, vendor == null ? "" : vendor.getAliasname());
                        obj.put(GoodsReceiptCMNConstants.PERSONEMAIL, vendor == null ? "" : vendor.getEmail());
                        obj.put(GoodsReceiptCMNConstants.BILLNO, gReceipt.getGoodsReceiptNumber());
                        obj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                        obj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (gReceipt.getCurrency() == null ? kwlcurrency.getSymbol() : gReceipt.getCurrency().getSymbol()));
                        obj.put("currencyCode", (gReceipt.getCurrency() == null ? kwlcurrency.getCurrencyCode() : gReceipt.getCurrency().getCurrencyCode()));
                        obj.put("currencycode", (gReceipt.getCurrency() == null ? kwlcurrency.getCurrencyCode() : gReceipt.getCurrency().getCurrencyCode()));
                        obj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (gReceipt.getCurrency() == null ? kwlcurrency.getName() : gReceipt.getCurrency().getName()));
                        obj.put(GoodsReceiptCMNConstants.COMPANYADDRESS, gReceipt.getCompany().getAddress());
                        obj.put(GoodsReceiptCMNConstants.COMPANYNAME, gReceipt.getCompany().getCompanyName());
                        obj.put(GoodsReceiptCMNConstants.BILLTO, gReceipt.getBillFrom());
                        obj.put(GoodsReceiptCMNConstants.ISEXPENSEINV, gReceipt.isIsExpenseType());
                        obj.put(GoodsReceiptCMNConstants.SHIPTO, gReceipt.getShipFrom());
                        obj.put(GoodsReceiptCMNConstants.JOURNALENTRYID, je != null ? je.getID() : "");
                        obj.put(GoodsReceiptCMNConstants.ENTRYNO, je != null ? je.getEntryNumber() : "");
                        obj.put(GoodsReceiptCMNConstants.SHIPDATE, gReceipt.getShipDate() == null ? "" : df.format(gReceipt.getShipDate()));
                        obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(gReceipt.getDueDate()));
                        obj.put(GoodsReceiptCMNConstants.PERSONNAME, vendor == null ? account.getName() : vendor.getName());
                        obj.put(GoodsReceiptCMNConstants.PERSONINFO, vendor == null ? account.getName() : vendor.getName()+"("+vendor.getAcccode()+")");
                        obj.put("personcode", vendor == null ? (account.getAcccode()==null?"":account.getAcccode()) : (vendor.getAcccode()==null?"":vendor.getAcccode()));
                        obj.put("agent", gReceipt.getMasterAgent() == null ? "" : gReceipt.getMasterAgent().getID());
                        obj.put(GoodsReceiptCMNConstants.MEMO, gReceipt.getMemo());
                        obj.put("posttext", gReceipt.getPostText());
                        obj.put("shiplengthval", gReceipt.getShiplength());
                        obj.put("invoicetype", gReceipt.getInvoicetype());
                        obj.put(GoodsReceiptCMNConstants.TERMNAME, vendor == null ? "" : ((vendor.getDebitTerm() == null) ? "" : vendor.getDebitTerm().getTermname()));
                        obj.put(GoodsReceiptCMNConstants.DELETED, gReceipt.isDeleted());
                        obj.put(GoodsReceiptCMNConstants.TAXINCLUDED, gReceipt.getTax() == null ? false : true);
                        obj.put(GoodsReceiptCMNConstants.TAXID, gReceipt.getTax() == null ? "" : gReceipt.getTax().getID());
                        obj.put(GoodsReceiptCMNConstants.TAXNAME, gReceipt.getTax() == null ? "" : gReceipt.getTax().getName());
                        obj.put(GoodsReceiptCMNConstants.DISCOUNT, gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscountValue());
                        obj.put(GoodsReceiptCMNConstants.ISPERCENTDISCOUNT, gReceipt.getDiscount() == null ? false : gReceipt.getDiscount().isInPercent());
                        obj.put(GoodsReceiptCMNConstants.DISCOUNTVAL, gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscount());
                        obj.put(CCConstants.JSON_costcenterid, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getID()) : "");
                        obj.put(CCConstants.JSON_costcenterName, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getName()) : "");
                        obj.put("isfavourite", gReceipt.isFavourite());
                        obj.put("isprinted", gReceipt.isPrinted());
                        obj.put("cashtransaction", gReceipt.isCashtransaction());
                        obj.put("archieve", 0);
                        obj.put("shipvia", gReceipt.getShipvia() == null ? "" : gReceipt.getShipvia());
                        obj.put("fob", gReceipt.getFob() == null ? "" : gReceipt.getFob());
                        if (gReceipt.getTermsincludegst() != null) {
                            obj.put(Constants.termsincludegst, gReceipt.getTermsincludegst());
                        }
                        obj.put("termdays", gReceipt.getTermid() == null ? 0 : gReceipt.getTermid().getTermdays());
                        obj.put("termid", gReceipt.getTermid() == null ? "" : gReceipt.getTermid().getID());
                        //ERP-20637
                        
                        if (gReceipt.getLandedInvoice() != null) {
                            Set<GoodsReceipt> landInvoiceSet = gReceipt.getLandedInvoice();
                            String landedInvoiceId = "", landedInvoiceNumber = "";
                            for (GoodsReceipt grObj : landInvoiceSet) {
                                if (!(StringUtil.isNullOrEmpty(landedInvoiceId) && StringUtil.isNullOrEmpty(landedInvoiceId))) {
                                    landedInvoiceId += ",";
                                    landedInvoiceNumber += ",";
                                }
                                landedInvoiceId += grObj.getID();
                                landedInvoiceNumber += grObj.getGoodsReceiptNumber();
                            }
                            obj.put("landedInvoiceID", landedInvoiceId);
                            obj.put("landedInvoiceNumber", landedInvoiceNumber);
                        }
                        obj.put("billto", gReceipt.getBillTo() == null ? "" : gReceipt.getBillTo());
                        obj.put("shipto", gReceipt.getShipTo() == null ? "" : gReceipt.getShipTo());
                        obj.put("isCapitalGoodsAcquired", gReceipt.isCapitalGoodsAcquired());
                        obj.put("isRetailPurchase", gReceipt.isRetailPurchase());
                        obj.put("importService", gReceipt.isImportService());
                        obj.put("sequenceformatid", gReceipt.getSeqformat() == null ? "" : gReceipt.getSeqformat().getID());
                        obj=AccountingAddressManager.getTransactionAddressJSON(obj,gReceipt.getBillingShippingAddresses(),true);
                        obj.put("gstIncluded", gReceipt.isGstIncluded());
                        obj.put("selfBilledInvoice", gReceipt.isSelfBilledInvoice());
                        obj.put("RMCDApprovalNo", gReceipt.getRMCDApprovalNo());
                        obj.put("fixedAssetInvoice", gReceipt.isFixedAssetInvoice());
                        obj.put("isConsignment", gReceipt.isIsconsignment());
                        if (gReceipt.getModifiedby() != null) {
                            obj.put("lasteditedby", StringUtil.getFullName(gReceipt.getModifiedby()));
                        }
                        obj.put("journalEntryId", je.getID());
                        obj.put("journalEntryNo", je.getEntryNumber());
                        obj.put("date", df.format(gReceipt.getCreationDate()));
                        obj.put("personname", gReceipt.getVendor() == null ? account.getName() : gReceipt.getVendor().getName());
                        if (gReceipt.isFixedAssetInvoice()) {
                            obj.put("mergedCategoryData", "Fixed Asset Acquired Invoice");  //type of data
                        } else {
                            obj.put("mergedCategoryData", "Vendor Invoice");  //type of data

                        }
                        obj.put("type",1);//PO->PI
                   }  
                    jArr.put(obj);
               }
            }
                KwlReturnObject resultgo = accGoodsReceiptobj.getGoodsReceiptOrdersMerged(requestParams);
                List list1 = resultgo.getEntityList();
     
              Iterator itr1 = list1.iterator();
               double amount1 = 0;
               double quantity1 = 0;
             while (itr1.hasNext()) {
                Object[] oj = (Object[])itr1.next();                
                String orderid = oj[0].toString();
                 JSONObject obj = new JSONObject();
                //Withoutinventory 0 for normal, 1 for billing
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), orderid);
                    GoodsReceiptOrder grOrder = (GoodsReceiptOrder) objItr.getEntityList().get(0);
                    Set<GoodsReceiptOrderDetails> doRows = grOrder.getRows();
                    Vendor vendor=grOrder.getVendor();
                   amount1=0;
                   if (doRows != null && !doRows.isEmpty()) {
                     for (GoodsReceiptOrderDetails temp : doRows) {
                         quantity1 = temp.getInventory().getQuantity();
                         amount1 += temp.getRate() * quantity1;

                     }
                   }
                    obj.put("billid", grOrder.getID());
                    obj.put("companyid", grOrder.getCompany().getCompanyID());
                    obj.put("gtaapplicable", grOrder.isRcmApplicable());
                    obj.put("companyname", grOrder.getCompany().getCompanyName());
                    obj.put("withoutinventory", false);
                    obj.put("personid", vendor.getID());
                    obj.put("transactionNo", grOrder.getGoodsReceiptOrderNumber());
                    obj.put(Constants.IsRoundingAdjustmentApplied, grOrder.isIsRoundingAdjustmentApplied());
                    obj.put("date", authHandler.getDateOnlyFormat().format(grOrder.getOrderDate()));
                    obj.put("personname", vendor.getName());
                    obj.put("externalcurrencyrate", grOrder.getExternalCurrencyRate());
                    obj.put("withoutinventory", false);
                    obj.put("personid", vendor.getID());
                    obj.put("billno", grOrder.getGoodsReceiptOrderNumber());
                    obj.put("date", authHandler.getDateOnlyFormat().format(grOrder.getOrderDate()));
                    obj.put("personname", vendor.getName());
                    obj.put("aliasname", StringUtil.isNullOrEmpty(vendor.getAliasname()) ? "" : vendor.getAliasname());
                    obj.put("personemail", vendor.getEmail());
                    obj.put("memo", grOrder.getMemo());
                    obj.put("agent", grOrder.getMasterAgent() == null ? "" : grOrder.getMasterAgent().getID());
                    obj.put("agentname", grOrder.getMasterAgent() == null ? "" : grOrder.getMasterAgent().getValue());
                    obj.put("posttext", grOrder.getPostText() == null ? "" : grOrder.getPostText());
                    obj.put("costcenterid", grOrder.getCostcenter() == null ? "" : grOrder.getCostcenter().getID());
                    obj.put("costcenterName", grOrder.getCostcenter() == null ? "" : grOrder.getCostcenter().getName());
                    obj.put("statusID", grOrder.getStatus() == null ? "" : grOrder.getStatus().getID());
                    obj.put("status", grOrder.getStatus() == null ? "" : grOrder.getStatus().getValue());
                    obj.put(SHIPDATE, grOrder.getShipdate() == null ? "" : authHandler.getDateOnlyFormat().format(grOrder.getShipdate()));
                    obj.put("shipvia", grOrder.getShipvia() == null ? "" : grOrder.getShipvia());
                    obj.put("fob", grOrder.getFob() == null ? "" : grOrder.getFob());
                    obj.put("permitNumber", grOrder.getPermitNumber() == null ? "" : grOrder.getPermitNumber());
                    obj.put("isfavourite", grOrder.isFavourite());
                    obj.put("isprinted", grOrder.isPrinted());
                    obj.put("isautogenerateddo", grOrder.isIsAutoGeneratedGRO());
                    obj.put("deleted", grOrder.isDeleted());
                    obj.put("currencyid", (grOrder.getCurrency() == null ? "" : grOrder.getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (grOrder.getCurrency() == null ? "" : grOrder.getCurrency().getSymbol()));
                    obj.put(Constants.SEQUENCEFORMATID, grOrder.getSeqformat() != null ? grOrder.getSeqformat().getID() : "");
                    obj.put("isConsignment", grOrder.isIsconsignment());
                    if (grOrder.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(grOrder.getModifiedby()));
                    }
                     double taxPercent = 0;
                    if (grOrder.getTax() != null) {
                        KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request),grOrder.getOrderDate() , grOrder.getTax().getID());
                        taxPercent = (Double) taxresult.getEntityList().get(0);
                        
                    }
                    obj.put( "taxid",grOrder.getTax() != null ? grOrder.getTax().getID() : "");
                    obj.put( TAXPERCENT,taxPercent);
                    Set<GoodsReceiptOrderDetails> goodsReceiptOrderDetails = grOrder.getRows();
                    boolean includeprotax = false;
                    double rowTaxAmt = 0;
                    boolean isTaxRowLvlAndFromTaxGlobalLvl = false;
                    for (GoodsReceiptOrderDetails goodsReceiptOrderDetail : goodsReceiptOrderDetails) {
                        if (goodsReceiptOrderDetail.getPodetails() != null && goodsReceiptOrderDetail.getPodetails().getPurchaseOrder() != null) {
                            if (goodsReceiptOrderDetail.getPodetails().getPurchaseOrder().getTax() != null) {
                                isTaxRowLvlAndFromTaxGlobalLvl = true;
                            }
                        }
                        if (goodsReceiptOrderDetail.getVidetails()!= null && goodsReceiptOrderDetail.getVidetails().getGoodsReceipt()!= null) {
                            if (goodsReceiptOrderDetail.getVidetails().getGoodsReceipt().getTax() != null) {
                                isTaxRowLvlAndFromTaxGlobalLvl = true;
                            }
                        }
                        if (goodsReceiptOrderDetail.getTax() != null) {
                            includeprotax = true;
                            rowTaxAmt += goodsReceiptOrderDetail.getRowTaxAmount();
                        }
                    }
                    obj.put("isTaxRowLvlAndFromTaxGlobalLvl", isTaxRowLvlAndFromTaxGlobalLvl);
                    obj.put("includeprotax", includeprotax);
                    obj.put("rowTaxAmt", rowTaxAmt);
                    obj=AccountingAddressManager.getTransactionAddressJSON(obj, grOrder.getBillingShippingAddresses(), true);

                obj.put("amount", authHandler.round(amount1, companyid));
                if (grOrder.getCurrency() != null) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount1, grOrder.getCurrency().getCurrencyID(), grOrder.getOrderDate(), grOrder.getExternalCurrencyRate());
                    obj.put("amountinbase", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                }
               obj.put("mergedCategoryData", "Goods Receipt");  //type of data
               obj.put("type",4);//PO->GR
                    jArr.put(obj);
                }
             
                //PO Linked in SalesOrder for backward and forward linking
                KwlReturnObject resultso = accGoodsReceiptobj.getSalesOrderMerged(requestParams);//PO->SO type=3
                List listso = resultso.getEntityList();
                
                resultso = accGoodsReceiptobj.getSOLinkedInPO(requestParams);//SO->PO type=2
                List listso1 = resultso.getEntityList();
                if(listso1!=null && listso1.size()>0){
                    listso.addAll(listso1);
                }

                Iterator itrso = listso.iterator();
                while (itrso.hasNext()) {
                    Object[] oj = (Object[]) itrso.next();
                    String orderid = oj[0].toString();
                    JSONObject obj = new JSONObject();
                    BigInteger type = (BigInteger) oj[1];
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), orderid);
                    SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                    KWLCurrency currency = null;
                    if(salesOrder.getCurrency() != null){
                        currency = salesOrder.getCurrency();
                    } else {
                        currency=salesOrder.getCustomer().getAccount().getCurrency()==null?kwlcurrency:salesOrder.getCustomer().getAccount().getCurrency();
                    }
                    Customer customer = salesOrder.getCustomer();
                    obj.put("billid", salesOrder.getID());
                    obj.put(Constants.IsRoundingAdjustmentApplied, salesOrder.isIsRoundingAdjustmentApplied());
                    obj.put("companyid", salesOrder.getCompany().getCompanyID());
                    obj.put("companyname", salesOrder.getCompany().getCompanyName());
                    obj.put("withoutinventory", false);
                    obj.put("externalcurrencyrate", salesOrder.getExternalCurrencyRate());
                    obj.put("personid", customer.getID());
                    obj.put("aliasname", customer.getAliasname());
                    obj.put("customercode", customer.getAcccode()==null? "":customer.getAcccode());
                    obj.put("billno", salesOrder.getSalesOrderNumber());
                    obj.put("lasteditedby", salesOrder.getModifiedby() == null ? "" : (salesOrder.getModifiedby().getFirstName() + " " + salesOrder.getModifiedby().getLastName()));
                    obj.put("duedate", authHandler.getDateOnlyFormat().format(salesOrder.getDueDate()));
                    obj.put("duedateinuserformat", userdf.format(salesOrder.getDueDate()));
                    obj.put("dateinuserformat", userdf.format(salesOrder.getOrderDate()));
                    obj.put("shipdate", salesOrder.getShipdate()==null? "" : authHandler.getDateOnlyFormat().format(salesOrder.getShipdate()));
                    obj.put("shipdateinuserformat", salesOrder.getShipdate()==null? "" :userdf.format(salesOrder.getShipdate()));
                    obj.put("shipvia", salesOrder.getShipvia());
                    obj.put("fob", salesOrder.getFob());
                    obj.put("isOpeningBalanceTransaction", salesOrder.isIsOpeningBalanceSO());
                    obj.put("isConsignment", salesOrder.isIsconsignment());
                    obj.put("parentso", salesOrder.getParentSO()==null?"":salesOrder.getParentSO().getID());
                    if (salesOrder.getCustWarehouse() != null) {
                        obj.put("custWarehouse", salesOrder.getCustWarehouse().getId());
                    }
                    obj.put("date", authHandler.getDateOnlyFormat().format(salesOrder.getOrderDate()));
                    obj.put("isfavourite", salesOrder.isFavourite());
                    obj.put("isprinted", salesOrder.isPrinted());
                    obj.put("billto", salesOrder.getBillTo());
                    obj.put("shipto", salesOrder.getShipTo());
                    obj.put("salesPerson", salesOrder.getSalesperson()==null?"":salesOrder.getSalesperson().getID());
                    obj.put("salespersonname", salesOrder.getSalesperson()==null?"":salesOrder.getSalesperson().getValue());
                    obj.put("salesPersonCode", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getCode());
                    obj.put("createdby",StringUtil.getFullName(salesOrder.getCreatedby()));
                    obj.put("createdbyid",salesOrder.getCreatedby().getUserID());
                    obj.put("deleted", salesOrder.isDeleted());
                    boolean gstIncluded = salesOrder.isGstIncluded();
                    obj.put("gstIncluded", gstIncluded);
                    obj.put("islockQuantityflag", salesOrder.isLockquantityflag());  //for getting locked flag of indivisual so
                    obj.put("leaseOrMaintenanceSo",salesOrder.getLeaseOrMaintenanceSO());
                    obj.put("customerporefno",salesOrder.getCustomerPORefNo());
                    obj.put("totalprofitmargin",salesOrder.getTotalProfitMargin());
                    obj.put("totalprofitmarginpercent",salesOrder.getTotalProfitMarginPercent());
                    obj.put("maintenanceId", salesOrder.getMaintenance()==null?"":salesOrder.getMaintenance().getId());
                    obj.put("termname", salesOrder.getTerm()==null ? "":salesOrder.getTerm().getTermname());
                    BillingShippingAddresses addresses=salesOrder.getBillingShippingAddresses();
                    AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    Iterator itrRow = salesOrder.getRows().iterator();
                    double amount = 0,totalDiscount = 0, discountPrice = 0;
                    double rowTaxAmt = 0d, rowDiscountAmt = 0d;
                    while (itrRow.hasNext()) {
                        SalesOrderDetail sod= (SalesOrderDetail) itrRow.next();
                        if(sod.getTax()!=null){
                            requestParams.put("transactiondate", salesOrder.getOrderDate());
                            requestParams.put("taxid", sod.getTax().getID());
                        }

                        double sorate=authHandler.round(sod.getRate(), companyid);
                        if(gstIncluded) {
                            sorate = sod.getRateincludegst();
                        }
                        double quantity=authHandler.roundQuantity(sod.getQuantity(), companyid);

                        double quotationPrice = authHandler.round(quantity * sorate, companyid);
                        double discountSOD=authHandler.round(sod.getDiscount(), companyid);
                        
                        if(sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountSOD/100), companyid);
                            rowDiscountAmt += authHandler.round((quotationPrice * discountSOD/100), companyid);
                        } else {
                            discountPrice = quotationPrice - discountSOD;
                            rowDiscountAmt += discountSOD;
                        }
                        rowTaxAmt+=sod.getRowTaxAmount();
                        amount += discountPrice;//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                        if(!gstIncluded) {
                            amount += authHandler.round(sod.getRowTaxAmount(), companyid);
                        }
                    }                    
                    double discountSO=authHandler.round(salesOrder.getDiscount(), companyid);
                    if (discountSO != 0) {
                        if (salesOrder.isPerDiscount()) {
                            totalDiscount = authHandler.round(amount * discountSO / 100, companyid);
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - discountSO;
                            totalDiscount = discountSO;
                        }
                        obj.put("discounttotal", discountSO);
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    obj.put("discount", rowDiscountAmt);
                    obj.put("discountispertotal", salesOrder.isPerDiscount());
                    obj.put("amount", amount); 
                   if(salesOrder.isPerDiscount()){
                        obj.put("ispercentdiscount", salesOrder.isPerDiscount());
                        obj.put("discountval", discountSO);
                    }else{
                        obj.put("discountval", totalDiscount);    //obj.put("discountval", salesOrder.getDiscount());
                    }
                    try {
                        obj.put("creditDays", salesOrder.getTerm().getTermdays());
                    } catch(Exception ex) {
                        obj.put("creditDays", 0);
                    }
                    try {
                        obj.put("termid", salesOrder.getTerm().getID());
                    } catch(Exception ex) {
                        obj.put("termid", "");
                    }
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("currencycode", currency.getCurrencyCode());
                    obj.put("taxid", salesOrder.getTax()==null?"":salesOrder.getTax().getID());
                    obj.put("taxname", salesOrder.getTax()==null?"":salesOrder.getTax().getName());
                    obj.put("shiplengthval", salesOrder.getShiplength());
                    obj.put("invoicetype", salesOrder.getInvoicetype());
                    double  taxPercent=0;
                    if(salesOrder.getTax()!=null){
                        requestParams.put("transactiondate", salesOrder.getOrderDate());
                        requestParams.put("taxid", salesOrder.getTax().getID());
                        KwlReturnObject resulttax = accTaxObj.getTax(requestParams);
                        List taxList = resulttax.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        taxPercent=taxObj[1]==null?0:(Double) taxObj[1];

                    }
                    double orderAmount=amount;//(Double) bAmt.getEntityList().get(0);
                    double ordertaxamount=(taxPercent==0?0:authHandler.round((orderAmount*taxPercent/100), companyid));
                    
                    double taxAmt=rowTaxAmt+ordertaxamount;// either row level tax will be avvailable or invoice level
                    
                    obj.put("amountbeforegst", amount-rowTaxAmt); // Amount before both kind of tax row level or transaction level
                    obj.put("taxpercent", taxPercent);
                    obj.put("taxamount",taxAmt );// Tax Amount
                    
                    obj.put("transactionNo", salesOrder.getSalesOrderNumber());
                    obj.put("duedate", df.format(salesOrder.getDueDate()));
                    obj.put("date", df.format(salesOrder.getOrderDate()));
                    obj.put("personname", customer.getName());
                    obj.put("mergedCategoryData", "Sales Order");  //type of data
                    obj.put("type",type.intValue());
                    jArr.put(obj);
                }
                
                //VQ linked in Purchase Order type=5 VQ->PO
                
                KwlReturnObject resultcq = accGoodsReceiptobj.getVQlinkedInPO(requestParams);
                List vendorquotation = resultcq.getEntityList();
                if (vendorquotation != null && vendorquotation.size() > 0) {
                    
                    jArr = accSalesOrderServiceDAOobj.getVendorQuotationJsonForLinking(jArr, vendorquotation, kwlcurrency, df, companyid);
                }
                /*
                PR Linked to PO
                */
                requestParams.clear();
                requestParams.put("poid", poid);
                requestParams.put("companyid", companyid);
                KwlReturnObject poresult = accPurchaseOrderobj.getPurchaseRequisitionsLinkedWithPO(requestParams);
                vendorquotation = poresult.getEntityList();
                if (vendorquotation != null && vendorquotation.size() > 0) {
                    int type = Constants.Link_PR_TO_PO;
                    jArr = accPurchaseOrderServiceDAOobj.getPurchaseRequisitionJsonForLinking(jArr, vendorquotation, kwlcurrency, userdf, type);
                }
                           
             jobj.put("count", jArr.length());
              jobj.put("data", jArr);
       
        }
        }catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    public ModelAndView getPurchaseOrderByProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap requestParams = getPurchaseOrderMap(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            requestParams.put("start", start);
            requestParams.put("limit", limit);
            KwlReturnObject result = accPurchaseOrderobj.getPurchaseOrderByProduct(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();
            JSONArray DataJArr  = getPurchaseOrderByProductJson(request, list).getJSONArray("data");
            jobj.put("data", DataJArr);
            jobj.put("count", count);
            issuccess = true;
        } catch (Exception ex){
            msg = ""+ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView exportPurchaseOrderByProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap requestParams = getPurchaseOrderMap(request);
            KwlReturnObject result = accPurchaseOrderobj.getPurchaseOrderByProduct(requestParams);
            jobj = getPurchaseOrderByProductJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public JSONObject getPurchaseOrderByProductJson(HttpServletRequest request, List<Object[]> list) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            HashMap requestParams = getPurchaseOrderMap(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            for (Object[] oj : list) {
                if(oj[0]!=null){
                    String soid = oj[0].toString();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), soid);
                    PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
                    String currencyid = (purchaseOrder.getCurrency() == null ? currency.getCurrencyID() : purchaseOrder.getCurrency().getCurrencyID());
                    JSONObject obj = new JSONObject();
                    obj.put("billid", purchaseOrder.getID());
                    obj.put("customername", purchaseOrder.getVendor() == null ? "" : purchaseOrder.getVendor().getName());
                    obj.put("customerid", purchaseOrder.getVendor() == null ? "" : purchaseOrder.getVendor().getID());
                    obj.put("personname", purchaseOrder.getVendor() == null ? "" : purchaseOrder.getVendor().getName());
                    obj.put("personid", purchaseOrder.getVendor() == null ? "" : purchaseOrder.getVendor().getID());
                    obj.put(Constants.SEQUENCEFORMATID, purchaseOrder.getSeqformat() == null ? "" : purchaseOrder.getSeqformat().getID());
                    obj.put("lasteditedby", purchaseOrder.getModifiedby() == null ? "" : (purchaseOrder.getModifiedby().getFirstName() + " " + purchaseOrder.getModifiedby().getLastName()));
                    obj.put("shipdate", purchaseOrder.getShipdate() == null ? "" : df.format(purchaseOrder.getShipdate()));
                    obj.put("termid", purchaseOrder.getTerm() == null ? "" : purchaseOrder.getTerm().getID());
                    obj.put("duedate", purchaseOrder.getDueDate() == null ? "" : df.format(purchaseOrder.getDueDate()));
                    obj.put("memo", purchaseOrder.getMemo() == null ? "" : purchaseOrder.getMemo());
                    obj.put("shipvia", purchaseOrder.getShipvia() == null ? "" : purchaseOrder.getShipvia());
                    obj.put("fob", purchaseOrder.getFob() == null ? "" : purchaseOrder.getFob());
                    if (purchaseOrder.getBillingShippingAddresses() != null) {
                        obj = AccountingAddressManager.getTransactionAddressJSON(obj, purchaseOrder.getBillingShippingAddresses(), true);
                    }
                    boolean includeprotax = false;
                    double rowTaxAmt = 0d;
                    double taxAmt = 0d;
                    Set<PurchaseOrderDetail> purchaseOrderDetails = purchaseOrder.getRows();
                    for (PurchaseOrderDetail purchaseOrderDetail : purchaseOrderDetails) {
                        if (purchaseOrderDetail.getTax() != null) {
                            includeprotax = true;
                            rowTaxAmt += purchaseOrderDetail.getRowTaxAmount();
                        }
                    }
                    taxAmt += rowTaxAmt;// either row level tax will be avvailable or invoice level
                    obj.put("taxamount", taxAmt);
                    obj.put("includeprotax", includeprotax);
                    obj.put("taxincluded", purchaseOrder.getTax() != null);
                    obj.put("taxid", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getID());
                    obj.put("taxname", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getName());
                    obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                    obj.put("currencyid", currencyid);
                    obj.put("currencysymbol", (purchaseOrder.getCurrency() == null ? currency.getSymbol() : purchaseOrder.getCurrency().getSymbol()));
                    obj.put("currencycode", (purchaseOrder.getCurrency() == null ? currency.getCurrencyCode() : purchaseOrder.getCurrency().getCurrencyCode()));
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, purchaseOrder.getOrderDate(), 0);
                    obj.put("oldcurrencyrate", (Double) bAmt.getEntityList().get(0));
                    obj.put("date", df.format(purchaseOrder.getOrderDate()));
                    /*
                    * For Product search, add Products details from SO details
                    */
                    String idvString = !StringUtil.isNullOrEmpty(oj[3].toString()) ? oj[3].toString() : ""; //as in list invoiedetail id comes 4th
                    KwlReturnObject objItrID = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), idvString);
                    PurchaseOrderDetail idvObj = (PurchaseOrderDetail) objItrID.getEntityList().get(0);
                    if (idvObj != null) {
                        obj.put("rowproductname", idvObj.getProduct().getName());
                        obj.put("rowproductid", idvObj.getProduct().getProductid());
                        obj.put("rowproductdescription", StringUtil.isNullOrEmpty(idvObj.getDescription()) ? "" : StringUtil.replaceFullHTML(StringUtil.DecodeText(idvObj.getDescription()).replace("<br>","\n")));
                        double quantity = idvObj.getQuantity();
                        double discountPrice = 0;
                        obj.put("rowquantity", authHandler.formattedQuantity(quantity, companyid)); //To show quantity with four decimal point in PDF & Print
                        obj.put("rowrate", idvObj.getRate());
                        obj.put("unitpricecurrency", idvObj.getRate());
                        double sorate = idvObj.getRate();
                        double quotationPrice = quantity * sorate;
                        double discountSOD = idvObj.getDiscount();

                        if (idvObj.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountSOD / 100), companyid);
                        } else {
                            discountPrice = quotationPrice - discountSOD;
                        }
                        double amountWithoutTax = authHandler.round((Double) discountPrice, companyid);
                        obj.put("amount", amountWithoutTax);
                        double amountInBase = (Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountWithoutTax, currencyid, idvObj.getPurchaseOrder().getOrderDate(), idvObj.getPurchaseOrder().getExternalCurrencyRate()).getEntityList().get(0);
                        obj.put("amountinbase", amountInBase);
                        obj.put("amountinbasewithouttax", amountInBase);
                        double rowTaxPercent = 0;
                        double rowTaxAmount = 0;
                        if (idvObj.getTax() != null) {
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, purchaseOrder.getOrderDate(), idvObj.getTax().getID());
                            rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        }
                        obj.put("rowprtaxpercent", rowTaxPercent);
                        rowTaxAmount = amountInBase * rowTaxPercent / 100;
                        if (rowTaxPercent > 0) {
                            obj.put("amountinbasewithtax", amountInBase + (rowTaxAmount));//obj.put("amountinbasewithtax", amountInBase + (amountInBase * rowTaxPercent/100));
                        } else {
                            obj.put("amountinbasewithtax", amountInBase);
                        }
                    }
                    requestParams.put("taxtype", 1);
                    double percent=0.0;
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List<Object[]> listTax = result.getEntityList();
                    if (listTax != null && !listTax.isEmpty()) {
                        for (Object[] row : listTax) {
                            if (row[2] == null) {
                                continue;
                            }
                            Tax tax = (Tax) row[0];
                            if (purchaseOrder.getTax() != null ? tax.getID().equals(purchaseOrder.getTax().getID()) : false) {
                                percent = (Double) row[1];
                            };
                        }
                    }
                    if (purchaseOrder.getTax() != null) {
                        double amountInBaseWithInLineTax = obj.getDouble("amountinbasewithtax");
                        double amount = obj.getDouble("amountinbasewithtax");
                        double taxAmount = amount * percent / 100;
                        if (taxAmount > 0) {
                            obj.put("amountinbasewithtax", amountInBaseWithInLineTax + (taxAmount));//obj.put("amountinbasewithtax", amountInBase + (amountInBase * rowTaxPercent/100));
                        } else {
                            obj.put("amountinbasewithtax", amountInBaseWithInLineTax);
                        }
                    }
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getPurchaseOrderByProductJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    public ModelAndView getVQLinkedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getVQLinkedInTransaction(request);
            issuccess = true;
        }catch (SessionExpiredException | ServiceException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        }  catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderControllerCMN.getVQLinkedInTransaction : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getVQLinkedInTransaction(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String billid = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = (DateFormat) authHandler.getUserDateFormatter(request);
            String currencyid = request.getParameter("gcurrencyid") != null ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            
            if (!StringUtil.isNullOrEmpty(billid)) {   
                //VQ Linked in Purcahse Order
                // KWLCurrency currency = null;                   
                KwlReturnObject resultso = accPurchaseOrderobj.getPurchaseOrderLinkedWithVQ(billid, companyid);
                List listso = resultso.getEntityList();
                Iterator itrso = listso.iterator();
                while (itrso.hasNext()) {
                    String orderid = (String) itrso.next();
                    JSONObject obj = new JSONObject();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), orderid);
                    PurchaseOrder purchaseorder = (PurchaseOrder) objItr.getEntityList().get(0);
                    if (purchaseorder.getCurrency() != null) {
                        currency = purchaseorder.getCurrency();
                    }
                    Vendor vendor = purchaseorder.getVendor();
                    obj.put("billid", purchaseorder.getID());
                    obj.put("companyid", purchaseorder.getCompany().getCompanyID());
                    obj.put("companyname", purchaseorder.getCompany().getCompanyName());
                    obj.put("withoutinventory", "");
                    obj.put("transactionNo", purchaseorder.getPurchaseOrderNumber());   //Purchase order no
                    obj.put("duedate", df.format(purchaseorder.getDueDate()));
                    obj.put("date", purchaseorder.getOrderDate() != null ? df.format(purchaseorder.getOrderDate()) : "");
                    if (purchaseorder.isIsconsignment()) {
                        obj.put("mergedCategoryData", "Consignment Purchase Order");  //type of data
                    } else if (purchaseorder.isFixedAssetPO()) {
                        obj.put("mergedCategoryData", "Fixed Asset Purchase Order");  //type of data
                    } else {
                        obj.put("mergedCategoryData", "Purchase Order");  //type of data
                    }
                    obj.put("personname", vendor.getName());
                    obj.put("personid", vendor.getID());
                    obj.put("companyid", purchaseorder.getCompany().getCompanyID());
                    obj.put("companyname", purchaseorder.getCompany().getCompanyName());
                    obj.put("gtaapplicable", purchaseorder.isGtaapplicable());
                    obj.put("externalcurrencyrate", purchaseorder.getExternalCurrencyRate());
                    obj.put("isOpeningBalanceTransaction", purchaseorder.isIsOpeningBalancePO());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("currencycode", currency.getCurrencyCode() == null ? "" : currency.getCurrencyCode());
                    obj.put("personid", vendor.getID());
                    obj.put("aliasname", vendor.getAliasname());
                    obj.put("personcode", vendor.getAcccode() == null ? "" : vendor.getAcccode());
                    obj.put("createdby", purchaseorder.getCreatedby() == null ? "" : StringUtil.getFullName(purchaseorder.getCreatedby()));
                    obj.put("billtoaddress", purchaseorder.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(purchaseorder.getBillingShippingAddresses(), true));
                    obj.put("shiptoaddress", purchaseorder.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(purchaseorder.getBillingShippingAddresses(), false));
                    obj.put("personemail", vendor.getEmail());
                    obj.put("billno", purchaseorder.getPurchaseOrderNumber());
                    obj.put(Constants.IsRoundingAdjustmentApplied, purchaseorder.isIsRoundingAdjustmentApplied());
                    obj.put("duedate", df.format(purchaseorder.getDueDate()));
                    obj.put("date", df.format(purchaseorder.getOrderDate()));
                    obj.put("dateinuserformat", df.format(purchaseorder.getOrderDate()));
                    obj.put("shipdate", purchaseorder.getShipdate() == null ? "" : df.format(purchaseorder.getShipdate()));
                    obj.put("shipdateinuserformat", purchaseorder.getShipdate() == null ? "" : df.format(purchaseorder.getShipdate()));
                    obj.put("shipvia", purchaseorder.getShipvia() == null ? "" : purchaseorder.getShipvia());
                    obj.put("fob", purchaseorder.getFob() == null ? "" : purchaseorder.getFob());
                    obj.put("isfavourite", purchaseorder.isFavourite());
                    obj.put("isprinted", purchaseorder.isPrinted());
                    obj.put("deleted", purchaseorder.isDeleted());
                    obj.put("billto", purchaseorder.getBillTo() == null ? "" : purchaseorder.getBillTo());
                    obj.put("shipto", purchaseorder.getShipTo() == null ? "" : purchaseorder.getShipTo());
                    obj.put("agent", purchaseorder.getMasteragent() == null ? "" : purchaseorder.getMasteragent().getID());
                    if (purchaseorder.getApprover() != null) {
                        obj.put("approver", StringUtil.getFullName(purchaseorder.getApprover()));
                    }
                    boolean gstIncluded = purchaseorder.isGstIncluded();
                    obj.put("gstIncluded", gstIncluded);
                    obj.put("isConsignment", purchaseorder.isIsconsignment());
                    obj.put("termid", purchaseorder.getTerm() == null ? "" : purchaseorder.getTerm().getID());
                    obj = AccountingAddressManager.getTransactionAddressJSON(obj, purchaseorder.getBillingShippingAddresses(), true);
                    obj.put("termdays", purchaseorder.getTerm() == null ? 0 : purchaseorder.getTerm().getTermdays());
                    obj.put("termname", purchaseorder.getTerm() == null ? 0 : purchaseorder.getTerm().getTermname());
                    obj.put("termdetails", getTermDetails(purchaseorder.getID(), true));
                    if (purchaseorder.getTermsincludegst() != null) {
                        obj.put(Constants.termsincludegst, purchaseorder.getTermsincludegst());
                    }
                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(purchaseorder.getID(), true)));
                    obj.put(Constants.SEQUENCEFORMATID, purchaseorder.getSeqformat() == null ? "" : purchaseorder.getSeqformat().getID());
                    obj.put("memo", purchaseorder.getMemo());
                    obj.put("posttext", purchaseorder.getPostText());
                    obj.put("taxid", purchaseorder.getTax() == null ? "" : purchaseorder.getTax().getID());
                    obj.put("taxname", purchaseorder.getTax() == null ? "" : purchaseorder.getTax().getName());
                    obj.put("costcenterid", purchaseorder.getCostcenter() == null ? "" : purchaseorder.getCostcenter().getID());
                    obj.put("costcenterName", purchaseorder.getCostcenter() == null ? "" : purchaseorder.getCostcenter().getName());
                    obj.put("shiplengthval", purchaseorder.getShiplength());
                    obj.put("invoicetype", purchaseorder.getInvoicetype());
                    obj.put("archieve", 0);
                    obj.put("type", 2);//VQ->PO
                    jArr.put(obj);
                }
                //VQ Linked in Purcahse Invoice
                KwlReturnObject cnresult = accPurchaseOrderobj.getPurchaseInvoiceLinkedWithVQ(billid, companyid);
                List listc = cnresult.getEntityList();
                Iterator itr1 = listc.iterator();
                while (itr1.hasNext()) {
                    String orderid = (String) itr1.next();

                    JSONObject obj = new JSONObject();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), orderid);
                    GoodsReceipt goodsreceipt = (GoodsReceipt) objItr.getEntityList().get(0);

                    Vendor vendor = goodsreceipt.getVendor();
                    obj.put("billid", goodsreceipt.getID());
                    obj.put("companyid", goodsreceipt.getCompany().getCompanyID());
                    obj.put("companyname", goodsreceipt.getCompany().getCompanyName());
                    obj.put("withoutinventory", "");
                    obj.put("transactionNo", goodsreceipt.getGoodsReceiptNumber());   //delivery order no
                    obj.put("duedate", goodsreceipt.getDueDate() != null ? df.format(goodsreceipt.getDueDate()) : "");
                    obj.put("date", df.format(goodsreceipt.getCreationDate()));
                    obj.put("journalEntryId", goodsreceipt.getJournalEntry().getID());
                    obj.put("journalEntryNo", goodsreceipt.getJournalEntry().getEntryNumber());  //journal entry no
                    obj.put("gtaapplicable", goodsreceipt.isGtaapplicable());
                    obj.put(Constants.IsRoundingAdjustmentApplied, goodsreceipt.isIsRoundingAdjustmentApplied());
                    if (goodsreceipt.isIsconsignment()) {
                        obj.put("mergedCategoryData", "Consignment Vendor Invoice");  //type of data
                    } else if (goodsreceipt.isFixedAssetInvoice()) {
                        obj.put("mergedCategoryData", "Fixed Asset Acquired Invoice");  //type of data
                    } else {
                        obj.put("mergedCategoryData", "Vendor Invoice");  //type of data
                    }
                    obj.put("personname", vendor.getName());
                    obj.put("personid", vendor.getID());
                    JournalEntry je = null;
                    JournalEntryDetail d = null;
                    if (goodsreceipt.isNormalInvoice()) {
                        je = goodsreceipt.getJournalEntry();
                        d = goodsreceipt.getVendorEntry();
                    }
                    double externalCurrencyRate = 0d;
                    Date creationDate = null;
                    currencyid = goodsreceipt.getCurrency().getCurrencyID();
                    curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
                    currency = (KWLCurrency) curresult.getEntityList().get(0);
                    Account account = null;
                    creationDate = goodsreceipt.getCreationDate();
                    if (goodsreceipt.isIsOpeningBalenceInvoice() && !goodsreceipt.isNormalInvoice()) {
                        KwlReturnObject accObjItr = accountingHandlerDAOobj.getObject(Account.class.getName(), goodsreceipt.getVendor().getAccount().getID());
                        account = (Account) accObjItr.getEntityList().get(0);
                        externalCurrencyRate = goodsreceipt.getExchangeRateForOpeningTransaction();
                    } else {
                        account = d.getAccount();
                        externalCurrencyRate = je.getExternalCurrencyRate();
                    }
                    obj.put("isOpeningBalanceTransaction", goodsreceipt.isIsOpeningBalenceInvoice());
                    obj.put("isNormalTransaction", goodsreceipt.isNormalInvoice());
                    obj.put("parentinvoiceid", goodsreceipt.getParentInvoice() != null ? goodsreceipt.getParentInvoice().getID() : "");
                    obj.put("companyid", goodsreceipt.getCompany().getCompanyID());
                    obj.put("companyname", goodsreceipt.getCompany().getCompanyName());
                    obj.put(GoodsReceiptCMNConstants.PERSONID, vendor == null ? account.getID() : vendor.getID());
                    obj.put(GoodsReceiptCMNConstants.ALIASNAME, vendor == null ? "" : vendor.getAliasname());
                    obj.put(GoodsReceiptCMNConstants.PERSONEMAIL, vendor == null ? "" : vendor.getEmail());
                    obj.put(GoodsReceiptCMNConstants.BILLNO, goodsreceipt.getGoodsReceiptNumber());
                    obj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                    obj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (goodsreceipt.getCurrency() == null ? currency.getSymbol() : goodsreceipt.getCurrency().getSymbol()));
                    obj.put("currencyCode", (goodsreceipt.getCurrency() == null ? currency.getCurrencyCode() : goodsreceipt.getCurrency().getCurrencyCode()));
                    obj.put("currencycode", (goodsreceipt.getCurrency() == null ? currency.getCurrencyCode() : goodsreceipt.getCurrency().getCurrencyCode()));
                    obj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (goodsreceipt.getCurrency() == null ? currency.getName() : goodsreceipt.getCurrency().getName()));
                    obj.put(GoodsReceiptCMNConstants.COMPANYADDRESS, goodsreceipt.getCompany().getAddress());
                    obj.put(GoodsReceiptCMNConstants.COMPANYNAME, goodsreceipt.getCompany().getCompanyName());
                    obj.put(GoodsReceiptCMNConstants.BILLTO, goodsreceipt.getBillFrom());
                    obj.put(GoodsReceiptCMNConstants.ISEXPENSEINV, goodsreceipt.isIsExpenseType());
                    obj.put(GoodsReceiptCMNConstants.SHIPTO, goodsreceipt.getShipFrom());
                    obj.put(GoodsReceiptCMNConstants.JOURNALENTRYID, je != null ? je.getID() : "");
                    obj.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, externalCurrencyRate);
                    obj.put(GoodsReceiptCMNConstants.ENTRYNO, je != null ? je.getEntryNumber() : "");
                    obj.put(GoodsReceiptCMNConstants.DATE, df.format(creationDate));
                    obj.put(GoodsReceiptCMNConstants.SHIPDATE, goodsreceipt.getShipDate() == null ? "" : df.format(goodsreceipt.getShipDate()));
                    obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(goodsreceipt.getDueDate()));
                    obj.put(GoodsReceiptCMNConstants.PERSONNAME, vendor == null ? account.getName() : vendor.getName());
                    obj.put("personcode", vendor == null ? (account.getAcccode() == null ? "" : account.getAcccode()) : (vendor.getAcccode() == null ? "" : vendor.getAcccode()));
                    obj.put("agent", goodsreceipt.getMasterAgent() == null ? "" : goodsreceipt.getMasterAgent().getID());
                    obj.put(GoodsReceiptCMNConstants.MEMO, goodsreceipt.getMemo());
                    obj.put("posttext", goodsreceipt.getPostText());
                    obj.put("shiplengthval", goodsreceipt.getShiplength());
                    obj.put("invoicetype", goodsreceipt.getInvoicetype());
                    obj.put(GoodsReceiptCMNConstants.TERMNAME, vendor == null ? "" : ((vendor.getDebitTerm() == null) ? "" : vendor.getDebitTerm().getTermname()));
                    obj.put(GoodsReceiptCMNConstants.DELETED, goodsreceipt.isDeleted());
                    obj.put(GoodsReceiptCMNConstants.TAXINCLUDED, goodsreceipt.getTax() == null ? false : true);
                    obj.put(GoodsReceiptCMNConstants.TAXID, goodsreceipt.getTax() == null ? "" : goodsreceipt.getTax().getID());
                    obj.put(GoodsReceiptCMNConstants.TAXNAME, goodsreceipt.getTax() == null ? "" : goodsreceipt.getTax().getName());
                    obj.put(GoodsReceiptCMNConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + externalCurrencyRate + " " + (goodsreceipt.getCurrency() == null ? "" : goodsreceipt.getCurrency().getCurrencyCode()));
                    
                    obj.put(GoodsReceiptCMNConstants.DISCOUNT, goodsreceipt.getDiscount() == null ? 0 : goodsreceipt.getDiscount().getDiscountValue());
                    obj.put(GoodsReceiptCMNConstants.ISPERCENTDISCOUNT, goodsreceipt.getDiscount() == null ? false : goodsreceipt.getDiscount().isInPercent());
                    obj.put(GoodsReceiptCMNConstants.DISCOUNTVAL, goodsreceipt.getDiscount() == null ? 0 : goodsreceipt.getDiscount().getDiscount());
                    obj.put(CCConstants.JSON_costcenterid, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getID()) : "");
                    obj.put(CCConstants.JSON_costcenterName, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getName()) : "");
                    obj.put("isfavourite", goodsreceipt.isFavourite());
                    obj.put("isprinted", goodsreceipt.isPrinted());
                    obj.put("cashtransaction", goodsreceipt.isCashtransaction());
                    obj.put("archieve", 0);
                    obj.put("shipvia", goodsreceipt.getShipvia() == null ? "" : goodsreceipt.getShipvia());
                    obj.put("fob", goodsreceipt.getFob() == null ? "" : goodsreceipt.getFob());
                    if (goodsreceipt.getTermsincludegst() != null) {
                        obj.put(Constants.termsincludegst, goodsreceipt.getTermsincludegst());
                    }
                    obj.put("termdays", goodsreceipt.getTermid() == null ? 0 : goodsreceipt.getTermid().getTermdays());
                    obj.put("termid", goodsreceipt.getTermid() == null ? "" : goodsreceipt.getTermid().getID());
                    //ERP-20637
                    if (goodsreceipt.getLandedInvoice() != null) {
                        Set<GoodsReceipt> landInvoiceSet = goodsreceipt.getLandedInvoice();
                        String landedInvoiceId = "", landedInvoiceNumber = "";
                        for (GoodsReceipt grObj : landInvoiceSet) {
                            if (!(StringUtil.isNullOrEmpty(landedInvoiceId) && StringUtil.isNullOrEmpty(landedInvoiceId))) {
                                landedInvoiceId += ",";
                                landedInvoiceNumber += ",";
                            }
                            landedInvoiceId += grObj.getID();
                            landedInvoiceNumber += grObj.getGoodsReceiptNumber();
                        }
                        obj.put("landedInvoiceID", landedInvoiceId);
                        obj.put("landedInvoiceNumber", landedInvoiceNumber);
                    }
                    obj.put("billto", goodsreceipt.getBillTo() == null ? "" : goodsreceipt.getBillTo());
                    obj.put("shipto", goodsreceipt.getShipTo() == null ? "" : goodsreceipt.getShipTo());
                    obj.put("isCapitalGoodsAcquired", goodsreceipt.isCapitalGoodsAcquired());
                    obj.put("isRetailPurchase", goodsreceipt.isRetailPurchase());
                    obj.put("importService", goodsreceipt.isImportService());
                    obj = AccountingAddressManager.getTransactionAddressJSON(obj, goodsreceipt.getBillingShippingAddresses(), true);
                    Set<GoodsReceiptDetail> goodsReceiptDetails = goodsreceipt.getRows();
                    double rowTaxAmt = 0d;
                    boolean includeprotax = false;
                    double taxAmt = 0d;
                    if (!goodsreceipt.isIsExpenseType() && goodsreceipt.isNormalInvoice()) {
                        for (GoodsReceiptDetail goodsReceiptDetail : goodsReceiptDetails) {
                            if (goodsReceiptDetail.getTax() != null) {
                                includeprotax = true;
                                rowTaxAmt += goodsReceiptDetail.getRowTaxAmount() + goodsReceiptDetail.getRowTermTaxAmount();
                            }
                        }
                    }

                    taxAmt += rowTaxAmt;// either row level tax will be avvailable or invoice level
                    obj.put(GoodsReceiptCMNConstants.TAXAMOUNT, taxAmt);
                    obj.put("includeprotax", includeprotax);
                    obj.put("type", 8);//VQ->PI
                    jArr.put(obj);
                }
             
                /*
                 * Get Customer Quotation linked with Vendor Quotation i.e
                 * VQ->CQ
                 *
                 *
                 * while clicking on Linking/Unlinking information Button
                 * in Vendor Quotation Report
                 */

                Map requestparams = new HashMap();
                requestparams.put("quotationid", billid);
                requestparams.put("companyid", companyid);
                KwlReturnObject vqresult = accPurchaseOrderobj.getCQLinkedWithVQ(requestparams);
                List vendorquotation = vqresult.getEntityList();
                if (vendorquotation != null && vendorquotation.size() > 0) {
                    int type = 6; // VQ->CQ
                    jArr = accSalesOrderServiceDAOobj.getCustomerQuotationJsonForLinking(jArr, vendorquotation, currency, userdf, df, type);
                }

                /*
                 * Get Purchase Requisition linked with Vendor Quotation i.e
                 * PR->VQ
                 *
                 *
                 * while clicking on Linking/Unlinking information Button
                 *
                 * in Vendor Quotation Report
                 */

                requestparams.clear();
                requestparams.put("quotationid", billid);
                requestparams.put("companyid", companyid);
                vqresult = accPurchaseOrderobj.getPurchaseRequisitionsLinkedWithVQ(requestparams);
                vendorquotation = vqresult.getEntityList();
                if (vendorquotation != null && vendorquotation.size() > 0) {
                    int type = 7; // CQ->SO
                    jArr = accPurchaseOrderServiceDAOobj.getPurchaseRequisitionJsonForLinking(jArr, vendorquotation, currency, userdf, type);
                }
                
                /*
                get RFQ Link data
                */
                requestparams.clear();
                requestparams.put("quotationid", billid);
                requestparams.put("companyid", companyid);
                vqresult = accPurchaseOrderobj.getRFQLinkedWithVQ(requestparams);
                vendorquotation = vqresult.getEntityList();
                if (vendorquotation != null && vendorquotation.size() > 0) {
                    int type = 9; // RFQ->VQ
                    jArr = accPurchaseOrderServiceDAOobj.getRFQJsonForLinking(jArr, vendorquotation, currency, userdf, type);
                }

             jobj.put("count", jArr.length());
              jobj.put("data", jArr);
       
        }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    
    public ModelAndView getPRLinkedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getPRLinkedInTransaction(request);
            issuccess = true;
        } catch (SessionExpiredException | ServiceException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        }catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderControllerCMN.getPRLinkedInTransaction : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getPRLinkedInTransaction(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String billid = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat();

            if (!StringUtil.isNullOrEmpty(billid)) {
                //Purchase Requisition Linked in Vendor Quotation
                KwlReturnObject cnresult = accPurchaseOrderobj.getVendorQuotationLinkedWithPR(billid, companyid);
                List listc = cnresult.getEntityList();
                Iterator itr1 = listc.iterator();
                while (itr1.hasNext()) {
                    String orderid = (String) itr1.next();

                    JSONObject obj = new JSONObject();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), orderid);
                    VendorQuotation vendorquotation = (VendorQuotation) objItr.getEntityList().get(0);

                    KWLCurrency currency = null;
                    Vendor vendor = vendorquotation.getVendor();
                    if (vendorquotation.getCurrency() != null) {
                        currency = vendorquotation.getCurrency();
                    } 
                    obj.put("billid", vendorquotation.getID());
                    obj.put("companyid", vendorquotation.getCompany().getCompanyID());
                    obj.put("companyname", vendorquotation.getCompany().getCompanyName());
                    obj.put("withoutinventory", "");
                    obj.put("transactionNo", vendorquotation.getQuotationNumber());   //delivery order no
                    obj.put("duedate", vendorquotation.getDueDate() != null ? df.format(vendorquotation.getDueDate()) : "");
                    obj.put("date", vendorquotation.getQuotationDate() != null ? df.format(vendorquotation.getQuotationDate()) : "");
                    if (vendorquotation.isFixedAssetVQ()) {
                        obj.put("mergedCategoryData", "Fixed Asset Vendor Quotation");  //type of data
                    } else {
                        obj.put("mergedCategoryData", "Vendor Quotation");  //type of data
                    }
                    obj.put("personname", vendor.getName());
                    obj.put("personid", vendor.getID());
                    obj.put("sequenceformatid", vendorquotation.getSeqformat()!=null ? vendorquotation.getSeqformat().getID():null);
                    obj.put("companyid", vendorquotation.getCompany().getCompanyID());
                    obj.put("companyname", vendorquotation.getCompany().getCompanyName());
                    obj.put("externalcurrencyrate", vendorquotation.getExternalCurrencyRate());
                    obj.put("personid", vendor.getID());
                    obj.put("aliasname", vendor.getAliasname());
                    obj.put("personemail", vendor == null ? "" : vendor.getEmail());
                    obj.put("billno", vendorquotation.getQuotationNumber());
                    obj.put("status", vendorquotation.isIsOpen()?"Open":"Closed");
                    
                    obj.put("duedate", authHandler.getDateOnlyFormat().format(vendorquotation.getDueDate()));
                    obj.put("billtoaddress", vendorquotation.getBillingShippingAddresses()==null? "":CommonFunctions.getBillingShippingAddress(vendorquotation.getBillingShippingAddresses(), true));
                    obj.put("shiptoaddress", vendorquotation.getBillingShippingAddresses()==null? "":CommonFunctions.getBillingShippingAddress(vendorquotation.getBillingShippingAddresses(), false));
                    obj.put("createdby", vendorquotation.getCreatedby() == null ? "" : StringUtil.getFullName(vendorquotation.getCreatedby()));
                    obj.put("dateinuserformat", authHandler.getUserDateFormatterWithoutTimeZone(request).format(vendorquotation.getQuotationDate()));
                    obj.put("shipdateinuserformat", vendorquotation.getShipdate() == null ? "" : authHandler.getUserDateFormatterWithoutTimeZone(request).format(vendorquotation.getShipdate()));
                    obj.put("date", authHandler.getDateOnlyFormat().format(vendorquotation.getQuotationDate()));
                    obj.put("shipdate", vendorquotation.getShipdate() == null ? "" : authHandler.getDateOnlyFormat().format(vendorquotation.getShipdate()));
                    obj.put("validdate", vendorquotation.getValiddate() == null ? "" : authHandler.getDateOnlyFormat().format(vendorquotation.getValiddate()));
                    obj.put("shipvia", vendorquotation.getShipvia() == null ? "" : vendorquotation.getShipvia());
                    obj.put("fob", vendorquotation.getFob() == null ? "" : vendorquotation.getFob());
                    obj.put("archieve", vendorquotation.getArchieve());
                    obj.put("isfavourite", vendorquotation.isFavourite());
                    obj.put("isprinted", vendorquotation.isPrinted());
                    obj.put("termdetails", getTermDetails(vendorquotation.getID(), false));
                    if (vendorquotation.getTermsincludegst() != null) {
                        obj.put(Constants.termsincludegst, vendorquotation.getTermsincludegst());
                    }
                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(vendorquotation.getID(), false)));
                    obj.put("termid", vendorquotation.getTerm() != null ? vendorquotation.getTerm().getID() : "");
                    obj.put("termname", vendorquotation.getTerm() != null ? vendorquotation.getTerm().getTermname() : "");
                    obj.put("termdays", vendorquotation.getTerm() != null ? vendorquotation.getTerm().getTermdays() : "");
                    obj.put("discountval", vendorquotation.getDiscount());
                    obj.put("billto", vendorquotation.getBillTo() == null ? "" : vendorquotation.getBillTo());
                    obj.put("shipto", vendorquotation.getShipTo() == null ? "" : vendorquotation.getShipTo());
                    obj.put("deleted", vendorquotation.isDeleted());
                    obj.put("agent", vendorquotation.getMasteragent() != null ? vendorquotation.getMasteragent().getID() : "");
                    obj.put("gstIncluded", vendorquotation.isGstIncluded());
                    obj.put("shiplengthval", vendorquotation.getShiplength());
                    obj.put("invoicetype", vendorquotation.getInvoicetype());
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("currencycode", currency.getCurrencyCode()==null?"":currency.getCurrencyCode());
                    obj.put("taxid", vendorquotation.getTax() == null ? "" : vendorquotation.getTax().getID());
                    obj.put("taxname", vendorquotation.getTax() == null ? "" : vendorquotation.getTax().getName());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("personname", vendor.getName());
                    obj.put("personcode", vendor.getAcccode()==null?"":vendor.getAcccode());
                    obj.put("memo", vendorquotation.getMemo());
                    obj.put("posttext", vendorquotation.getPostText());
                    jArr.put(obj);
                }
                KwlReturnObject resultso = accPurchaseOrderobj.getPurchaseOrderLinkedWithPR(billid, companyid);
                List listso = resultso.getEntityList();
                Iterator itrso = listso.iterator();
                while (itrso.hasNext()) {
                    KWLCurrency currency = null;
                    String orderid = (String) itrso.next();
                    JSONObject obj = new JSONObject();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), orderid);
                    PurchaseOrder purchaseorder = (PurchaseOrder) objItr.getEntityList().get(0);
                    if (purchaseorder.getCurrency() != null) {
                        currency = purchaseorder.getCurrency();
                    }
                    Vendor vendor = purchaseorder.getVendor();
                    obj.put("billid", purchaseorder.getID());
                    obj.put("companyid", purchaseorder.getCompany().getCompanyID());
                    obj.put("companyname", purchaseorder.getCompany().getCompanyName());
                    obj.put("withoutinventory", "");
                    obj.put("transactionNo", purchaseorder.getPurchaseOrderNumber());   //Purchase order no
                    obj.put("duedate", df.format(purchaseorder.getDueDate()));
                    obj.put("date", purchaseorder.getOrderDate() != null ? df.format(purchaseorder.getOrderDate()) : "");
                    if (purchaseorder.isIsconsignment()) {
                        obj.put("mergedCategoryData", "Consignment Purchase Order");  //type of data
                    } else if (purchaseorder.isFixedAssetPO()) {
                        obj.put("mergedCategoryData", "Fixed Asset Purchase Order");  //type of data
                    } else {
                        obj.put("mergedCategoryData", "Purchase Order");  //type of data
                    }
                    obj.put("personname", vendor.getName());
                    obj.put("personid", vendor.getID());
                    obj.put("companyid", purchaseorder.getCompany().getCompanyID());
                    obj.put("companyname", purchaseorder.getCompany().getCompanyName());
                    obj.put("externalcurrencyrate", purchaseorder.getExternalCurrencyRate());
                    obj.put("isOpeningBalanceTransaction", purchaseorder.isIsOpeningBalancePO());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("currencycode", currency.getCurrencyCode() == null ? "" : currency.getCurrencyCode());
                    obj.put("personid", vendor.getID());
                    obj.put("aliasname", vendor.getAliasname());
                    obj.put("personcode", vendor.getAcccode() == null ? "" : vendor.getAcccode());
                    obj.put("createdby", purchaseorder.getCreatedby() == null ? "" : StringUtil.getFullName(purchaseorder.getCreatedby()));
                    obj.put("billtoaddress", purchaseorder.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(purchaseorder.getBillingShippingAddresses(), true));
                    obj.put("shiptoaddress", purchaseorder.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(purchaseorder.getBillingShippingAddresses(), false));
                    obj.put("personemail", vendor.getEmail());
                    obj.put("billno", purchaseorder.getPurchaseOrderNumber());
                    obj.put("duedate", df.format(purchaseorder.getDueDate()));
                    obj.put("date", df.format(purchaseorder.getOrderDate()));
                    obj.put("dateinuserformat", df.format(purchaseorder.getOrderDate()));
                    obj.put("shipdate", purchaseorder.getShipdate() == null ? "" : df.format(purchaseorder.getShipdate()));
                    obj.put("shipdateinuserformat", purchaseorder.getShipdate() == null ? "" : df.format(purchaseorder.getShipdate()));
                    obj.put("shipvia", purchaseorder.getShipvia() == null ? "" : purchaseorder.getShipvia());
                    obj.put("fob", purchaseorder.getFob() == null ? "" : purchaseorder.getFob());
                    obj.put("isfavourite", purchaseorder.isFavourite());
                    obj.put("isprinted", purchaseorder.isPrinted());
                    obj.put("deleted", purchaseorder.isDeleted());
                    obj.put("billto", purchaseorder.getBillTo() == null ? "" : purchaseorder.getBillTo());
                    obj.put("shipto", purchaseorder.getShipTo() == null ? "" : purchaseorder.getShipTo());
                    obj.put("agent", purchaseorder.getMasteragent() == null ? "" : purchaseorder.getMasteragent().getID());
                    if (purchaseorder.getApprover() != null) {
                        obj.put("approver", StringUtil.getFullName(purchaseorder.getApprover()));
                    }
                    boolean gstIncluded = purchaseorder.isGstIncluded();
                    obj.put("gstIncluded", gstIncluded);
                    obj.put("isConsignment", purchaseorder.isIsconsignment());
                    obj.put("termid", purchaseorder.getTerm() == null ? "" : purchaseorder.getTerm().getID());
                    obj = AccountingAddressManager.getTransactionAddressJSON(obj, purchaseorder.getBillingShippingAddresses(), true);
                    obj.put("termdays", purchaseorder.getTerm() == null ? 0 : purchaseorder.getTerm().getTermdays());
                    obj.put("termname", purchaseorder.getTerm() == null ? 0 : purchaseorder.getTerm().getTermname());
                    obj.put("termdetails", getTermDetails(purchaseorder.getID(), true));
                    if (purchaseorder.getTermsincludegst() != null) {
                        obj.put(Constants.termsincludegst, purchaseorder.getTermsincludegst());
                    }
                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(purchaseorder.getID(), true)));
                    obj.put(Constants.SEQUENCEFORMATID, purchaseorder.getSeqformat() == null ? "" : purchaseorder.getSeqformat().getID());
                    obj.put("memo", purchaseorder.getMemo());
                    obj.put("posttext", purchaseorder.getPostText());
                    obj.put("taxid", purchaseorder.getTax() == null ? "" : purchaseorder.getTax().getID());
                    obj.put("taxname", purchaseorder.getTax() == null ? "" : purchaseorder.getTax().getName());
                    obj.put("costcenterid", purchaseorder.getCostcenter() == null ? "" : purchaseorder.getCostcenter().getID());
                    obj.put("costcenterName", purchaseorder.getCostcenter() == null ? "" : purchaseorder.getCostcenter().getName());
                    obj.put("shiplengthval", purchaseorder.getShiplength());
                    obj.put("invoicetype", purchaseorder.getInvoicetype());
                    obj.put("archieve", 0);
                    obj.put("type", Constants.Link_PR_TO_PO);
                    jArr.put(obj);
                }
                DateFormat userdf = (DateFormat) authHandler.getUserDateFormatter(request);
                KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
                KWLCurrency currency = (KWLCurrency) curresult1.getEntityList().get(0);

               /* Get RFQ linked with Purchase Requisition*/
                cnresult = accPurchaseOrderobj.getRFQLinkedWithPR(billid, companyid);
                List invoiceList = cnresult.getEntityList();
                if (invoiceList != null && invoiceList.size() > 0) {
                   int type = 9; // RFQ->PR
                    jArr = accPurchaseOrderServiceDAOobj.getRFQJsonForLinking(jArr, invoiceList, currency, userdf, type);
                }

                jobj.put("count", jArr.length());
                jobj.put("data", jArr);

            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }

//Billing purchase Order
    public ModelAndView getBillingPurchaseOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            KwlReturnObject result = accPurchaseOrderobj.getBillingPurchaseOrders(requestParams);
            jobj = getBillingPurchaseOrdersJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = ""+ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getBillingPurchaseOrdersJson(HttpServletRequest request, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            KwlReturnObject result = accPurchaseOrderobj.getBillingPurchaseOrders(requestParams);
            Iterator itr = result.getEntityList().iterator();
            JSONArray jArr = new JSONArray();
            boolean closeflag = request.getParameter("closeflag") != null ? true : false;
            while (itr.hasNext()) {
                BillingPurchaseOrder purchaseOrder = (BillingPurchaseOrder) itr.next();
                Vendor vendor = purchaseOrder.getVendor();
                KWLCurrency currency = null;
                if(purchaseOrder.getCurrency() != null){
                    currency = purchaseOrder.getCurrency();
                } else {
                    currency=purchaseOrder.getVendor().getAccount().getCurrency()==null?kwlcurrency:purchaseOrder.getVendor().getAccount().getCurrency();
                }
//                KWLCurrency currency = purchaseOrder.getVendor().getAccount().getCurrency() == null ? kwlcurrency : purchaseOrder.getVendor().getAccount().getCurrency();
                JSONObject obj = new JSONObject();
                obj.put("billid", purchaseOrder.getID());
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("personid", vendor.getID());
                obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                obj.put("duedate", authHandler.getDateOnlyFormat().format(purchaseOrder.getDueDate()));
                obj.put("date", authHandler.getDateOnlyFormat().format(purchaseOrder.getOrderDate()));
                obj.put("shipdate", purchaseOrder.getShipdate()==null? "" : authHandler.getDateOnlyFormat().format(purchaseOrder.getShipdate()));
                obj.put("shipvia", purchaseOrder.getShipvia()==null?"" : purchaseOrder.getShipvia());
                obj.put("fob", purchaseOrder.getFob()==null?"" : purchaseOrder.getFob());
                Iterator itrRow = purchaseOrder.getRows().iterator();
                double amount = 0,totalDiscount = 0, discountPrice = 0;
                while (itrRow.hasNext()) {
                    BillingPurchaseOrderDetail pod = (BillingPurchaseOrderDetail) itrRow.next();
                    amount += pod.getQuantity() * pod.getRate();
                    double  rowTaxPercent=0;
                    if(pod.getTax()!=null){
                        requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                        requestParams.put("taxid", pod.getTax().getID());
                        result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }
                    
                    double bpoPrice = pod.getQuantity() * pod.getRate();
                    if(pod.getDiscountispercent() == 1) {
                            discountPrice = (bpoPrice) - (bpoPrice * pod.getDiscount()/100);
                        } else {
                            discountPrice = bpoPrice - pod.getDiscount();
                        }
                    
                    amount += discountPrice + (discountPrice * rowTaxPercent/100);
                    
//                    amount+=pod.getQuantity() *pod.getRate()*rowTaxPercent/100;
                }
                if (purchaseOrder.getDiscount() != 0) {
                    if (purchaseOrder.isPerDiscount()) {
                        totalDiscount = amount * purchaseOrder.getDiscount() / 100;
                        amount = amount - totalDiscount;
                    } else {
                        amount = amount - purchaseOrder.getDiscount();
                        totalDiscount = purchaseOrder.getDiscount();
                    }
                    obj.put("discounttotal", purchaseOrder.getDiscount());
                } else {
                    obj.put("discounttotal", 0);
                }
                obj.put("discount", totalDiscount);
                obj.put("discountispertotal", purchaseOrder.isPerDiscount());
                double  taxPercent=0;
                if(purchaseOrder.getTax()!=null){
                    requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                    requestParams.put("taxid", purchaseOrder.getTax().getID());
                    result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    taxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                }
                double ordertaxamount=(taxPercent==0?0:amount*taxPercent/100);
//              KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), purchaseOrder.getOrderDate(), 0);
//                double orderAmount=(Double) bAmt.getEntityList().get(0);
                obj.put("orderamountwithTax",amount+ordertaxamount);
                obj.put("taxpercent", taxPercent);
                obj.put("taxamount",ordertaxamount );
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("orderamount",amount );
                obj.put("amount", amount);
                obj.put("personname", vendor.getName());
                //obj.put("creditoraccount", purchaseOrder.getDebitFrom() == null ?"" : purchaseOrder.getDebitFrom().getID());
                //obj.put("crdraccid", purchaseOrder.getDebitFrom() == null ?"" : purchaseOrder.getDebitFrom().getID());
                obj.put("memo", purchaseOrder.getMemo());
                obj.put("taxid", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getID());
                obj.put("taxname", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getName());
                obj.put("costcenterid", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getID());
                obj.put("costcenterName", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getName());
                obj.put("billto", purchaseOrder.getBillTo()==null?"":purchaseOrder.getBillTo());
                obj.put("shipto", purchaseOrder.getShipTo()==null?"":purchaseOrder.getShipTo());
                 boolean includeprotax = false;
                    Set<BillingPurchaseOrderDetail> billingPurchaseOrderDetails = purchaseOrder.getRows();
                    for (BillingPurchaseOrderDetail billingPurchaseOrderDetail : billingPurchaseOrderDetails) {
                         if (billingPurchaseOrderDetail.getTax() != null) {
                            includeprotax = true;
                            break;
                        }
                    }
                    obj.put("includeprotax", includeprotax);
                String status = accPurchaseOrderServiceDAOobj.getBillingPurchaseOrderStatus(purchaseOrder);
                obj.put("status", status);
                if (!closeflag || (closeflag && status.equalsIgnoreCase("open"))) {
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException see) {
            throw ServiceException.FAILURE("getBillingPurchaseOrdersJson : "+see.getMessage(), see);
        } catch (NumberFormatException nfe) {
            throw ServiceException.FAILURE("getBillingPurchaseOrdersJson : "+nfe.getMessage(), nfe);
        } catch (JSONException jse) {
            throw ServiceException.FAILURE("getBillingPurchaseOrdersJson : "+jse.getMessage(), jse);
        }
        return jobj;
    }
    
    public ModelAndView getBillingPurchaseOrderRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getBillingPurchaseOrderRows(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getBillingPurchaseOrderRows(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
//            KWLCurrency kwlcurrency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            HashMap<String,Object> requestParams = AccountingManager.getGlobalParams(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            String[] pos = (String[])request.getParameter("bills").split(",");
            int i = 0;
            JSONArray jArr = new JSONArray();
            double addobj = 1;
            String closeflag = request.getParameter("closeflag");

            HashMap<String, Object> poRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("purchaseOrder.ID");
            order_by.add("srno");
            order_type.add("asc");
            poRequestParams.put("filter_names", filter_names);
            poRequestParams.put("filter_params", filter_params);
            poRequestParams.put("order_by", order_by);
            poRequestParams.put("order_type", order_type);

            while (pos != null && i < pos.length) {
//                BillingPurchaseOrder po = (BillingPurchaseOrder) session.get(BillingPurchaseOrder.class, pos[i]);
                KwlReturnObject poresult = accountingHandlerDAOobj.getObject(BillingPurchaseOrder.class.getName(), pos[i]);
                BillingPurchaseOrder po = (BillingPurchaseOrder) poresult.getEntityList().get(0);
                
                KWLCurrency currency = null;
                if(po.getCurrency() != null){
                    currency = po.getCurrency();
                } else {
                    currency=po.getVendor().getAccount().getCurrency()==null?kwlcurrency:po.getVendor().getAccount().getCurrency();
                }
//                KWLCurrency currency = po.getVendor().getAccount().getCurrency() == null ? kwlcurrency : po.getVendor().getAccount().getCurrency();

//                Iterator itr = po.getRows().iterator();
                filter_params.clear();
                filter_params.add(po.getID());
                KwlReturnObject podresult = accPurchaseOrderobj.getBillingPurchaseOrderDetails(poRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                while (itr.hasNext()) {
                    BillingPurchaseOrderDetail row = (BillingPurchaseOrderDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", po.getID());
                    obj.put("billno", po.getPurchaseOrderNumber());
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productdetail", row.getProductDetail());
                  //  obj.put("unitname", row.g);
                    obj.put("memo", row.getRemark());
                    obj.put("rate", row.getRate());
                    obj.put("creditoraccount", (row.getDebitFrom()== null)?"":row.getDebitFrom().getID());
                    //Provided default value to resolved blank issue of discount type if purchase order used for Invoice.
//                    obj.put("discountispercent", 1); 
//                    obj.put("prdiscount", 0);
//                    obj.put("rate", row.getRate());
                    obj.put("discountispercent", row.getDiscountispercent());
                    obj.put("prdiscount", row.getDiscount());
//                    obj.put("discount", row.getDiscount());
//                    obj.put("discountispertotal", row.isPerDiscount());
                    
                    double rowTaxPercent = 0;
                    double rowTaxAmount=0;
                    boolean isRowTaxApplicable=false;
                    if (row.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), po.getOrderDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                        if (isRowTaxApplicable) {
                            rowTaxAmount = row.getRowTaxAmount();
                        }
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("rowTaxAmount", rowTaxAmount);
                    obj.put("taxamount", rowTaxAmount);
                    obj.put("prtaxid", row.getTax()==null?"":row.getTax().getID());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), po.getOrderDate(), 0);
                    obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                    if (closeflag != null) {
                        addobj = getBillingPurchaseOrderDetailStatus(row);
                        obj.put("quantity", addobj);
                    } else {
                        obj.put("quantity", row.getQuantity());
                    }                    

                    if (addobj > 0) {
                        jArr.put(obj);
                    }
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE("getBillingPurchaseOrderRows : "+je.getMessage(), je);
        }
        return jobj;
    }

    public double getBillingPurchaseOrderDetailStatus(BillingPurchaseOrderDetail pod) throws ServiceException{
        double result = pod.getQuantity();
//        String query = "from BillingGoodsReceiptDetail ge where ge.purchaseOrderDetail.ID = ?";
//        List list =  HibernateUtil.executeQuery(session, query,pod.getID());
        KwlReturnObject bgrresult  = accGoodsReceiptobj.getBRDFromBPOD(pod.getID());
        Iterator ite1 = bgrresult.getEntityList().iterator();
        double qua = 0;
        while(ite1.hasNext()){
            BillingGoodsReceiptDetail ge = (BillingGoodsReceiptDetail)ite1.next();
            qua += ge.getQuantity();
        }
        result = pod.getQuantity()-qua;
        return result;
    }

    public ModelAndView exportPurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;            
            String[] companyids = (consolidateFlag && request.getParameter("companyids")!=null)?request.getParameter("companyids").split(","):sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);            
            boolean isJobWorkOrderReciever = request.getParameter("isJobWorkOrderReciever")!=null?Boolean.parseBoolean(request.getParameter("isJobWorkOrderReciever")):false;
            boolean eliminateflag = consolidateFlag;
            if(consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
             ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE is true then user has permission to view all vendors documents,so at that time there is need to filter record according to user&agent. 
                     */
                    String userId = sessionHandlerImpl.getUserid(request);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
            }
            
         // get TradingFlow indicator to determine which tables to be used for Outstanding Purchase Orders report
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            Boolean isTradingFlow = false;
            requestParams.put("isExport", true);
            requestParams.put("isJobWorkOrderReciever", isJobWorkOrderReciever);
            if (pref!=null && pref.isWithInvUpdate())
            	isTradingFlow = true;            
            requestParams.put("isTradingFlow", isTradingFlow);
            KwlReturnObject result = null;
            String companyid = "";
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];                
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
        
//                result = accPurchaseOrderobj.getPurchaseOrdersMerged(requestParams);
                boolean isOutstanding = request.getParameter("isOutstanding")!=null?Boolean.parseBoolean(request.getParameter("isOutstanding")):false;
                requestParams.put("isOutstanding", isOutstanding);
                int orderLinkedWithDocType = Integer.parseInt(request.getParameter("orderLinkedWithDocType"));
                requestParams.put("orderLinkedWithDocType", orderLinkedWithDocType);
                if (orderLinkedWithDocType != 0) {
                    result = accPurchaseOrderobj.getRelevantPurchaseOrderLinkingWise(requestParams);
                } else if (!isOutstanding) {
                    result = accPurchaseOrderobj.getPurchaseOrdersMerged(requestParams);
                } else {
                    result = accPurchaseOrderobj.getOutstandingPurchaseOrders(requestParams);
                }
                
               
                
                DataJArr = accPurchaseOrderServiceDAOobj.getPurchaseOrdersJsonMerged(requestParams, result.getEntityList(), DataJArr);
                if (request.getParameter("type")!=null && request.getParameter("type").equals("detailedXls")) {
                    DataJArr = getDetailExcelJsonPurchaseOrder(request, response, requestParams, DataJArr);
                }
            }
            int cnt = consolidateFlag?DataJArr.length():result.getRecordTotalCount();            
            jobj.put("data", DataJArr);
            jobj.put("count", cnt);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }else if(fileType.equals("xls")){
             request.setAttribute("isSummaryXls", true);
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    /**
     * Description to export Security gate entry report 
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView exportSecurityGateEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    String userId = sessionHandlerImpl.getUserid(request);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
            }

            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            Boolean isTradingFlow = false;
            requestParams.put("isExport", true);
            if (pref != null && pref.isWithInvUpdate()) {
                isTradingFlow = true;
            }
            requestParams.put("isTradingFlow", isTradingFlow);
            KwlReturnObject result = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                boolean isOutstanding = request.getParameter("isOutstanding") != null ? Boolean.parseBoolean(request.getParameter("isOutstanding")) : false;
                int orderLinkedWithDocType = Integer.parseInt(request.getParameter("orderLinkedWithDocType"));
                requestParams.put("orderLinkedWithDocType", orderLinkedWithDocType);
                result = accPurchaseOrderobj.getSecurityGateEntryMerged(requestParams);
                DataJArr = accPurchaseOrderServiceDAOobj.getSecurityGateEntryJsonMerged(requestParams, result.getEntityList(), DataJArr);
            }
            
            int cnt = consolidateFlag ? DataJArr.length() : result.getRecordTotalCount();
            jobj.put("data", DataJArr);
            jobj.put("count", cnt);
            String fileType = request.getParameter("filetype");
            
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            } else if (fileType.equals("xls")) {
                request.setAttribute("isSummaryXls", true);
            }
            exportDaoObj.processRequest(request, response, jobj);
            
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public JSONArray getDetailExcelJsonPurchaseOrder(HttpServletRequest request, HttpServletResponse response, HashMap<String, Object> requestParams, JSONArray DataJArr) throws JSONException, SessionExpiredException, ServiceException {
        boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
        String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
        String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
        String companyid = companyids[0];
        JSONArray tempArray = new JSONArray();
        for (int i = 0; i < DataJArr.length(); i++) {
            HashSet hs = new HashSet();
            String dono = "", doDate = "";
            JSONObject rowjobj = new JSONObject();
            rowjobj = DataJArr.getJSONObject(i);

            String billid = rowjobj.optString("billid", "");   //Invoice ID 
            String invoiceNumber = rowjobj.optString("billno", "");    //Invoice Number
            String customername = rowjobj.optString("personname", "");
            String customercode = rowjobj.optString("personcode", "");
            String creationDate = rowjobj.optString("date", "");
            String dueDate = rowjobj.optString("duedate", "");
            String docClass = rowjobj.optString("Custom_Document Class", "");

            requestParams.put("billId", billid);
            requestParams.put("bills", billid.split(","));
            request.setAttribute("companyid", companyid);
            request.setAttribute("gcurrencyid", gcurrencyid);
            request.setAttribute("billid", billid);
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", gcurrencyid);
            requestParams.put("closeflag", null);
            requestParams.put("dateFormatValue", authHandler.getDateOnlyFormat());
            requestParams.put("isForReport",true);
            requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(request));

            int srno = 1;
            JSONArray DataRowsArr = accPurchaseOrderServiceDAOobj.getPurchaseOrderRows(requestParams);
            tempArray.put(rowjobj);
            for (int j = 0; j < DataRowsArr.length(); j++) {
                JSONObject tempjobj = new JSONObject();
                tempjobj = DataRowsArr.getJSONObject(j);
                exportDaoObj.editJsonKeyForExcelFile(tempjobj, Constants.Acc_Purchase_Order_ModuleId);
                tempArray.put(tempjobj);
            }
        }
        return tempArray;
    }
    public ModelAndView exportBillingPurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            KwlReturnObject result = accPurchaseOrderobj.getBillingPurchaseOrders(requestParams);
            jobj = getBillingPurchaseOrdersJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView getPurchaseOrdersMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject requestJson = StringUtil.convertRequestToJsonObject(request);
            int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
            requestJson.put("permCode",permCode);
            jobj = accPurchaseOrderServiceDAOobj.getPurchaseOrdersMerged(requestJson);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Description model and view for to get date in security gate entry form
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getSecurityGateEntryMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            Boolean isTradingFlow = false;
            
            if (pref != null && pref.isWithInvUpdate()) {
                isTradingFlow = true;
            }
            requestParams.put("isTradingFlow", isTradingFlow);
            if (!StringUtil.isNullOrEmpty(request.getParameter("linknumber"))) {
                requestParams.put("linknumber", request.getParameter("linknumber"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bulkInv"))) {
                requestParams.put("bulkInv", request.getParameter("bulkInv"));
            }
            
            int orderLinkedWithDocType = request.getParameter("orderLinkedWithDocType") != null ? Integer.parseInt(request.getParameter("orderLinkedWithDocType")) : 0;
            requestParams.put("orderLinkedWithDocType", orderLinkedWithDocType);
            String userId = sessionHandlerImpl.getUserid(request);
            requestParams.put("userid", userId);

            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
                Map<String, Object> salesPersonParams = new HashMap<>();

                salesPersonParams.put("userid", sessionHandlerImpl.getUserid(request));
                salesPersonParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                salesPersonParams.put("grID", "20");
                KwlReturnObject masterItemByUserList = accountingHandlerDAOobj.getMasterItemByUserID(salesPersonParams);
                List<MasterItem> masterItems = masterItemByUserList.getEntityList();
                String salesPersons = "";
                StringBuffer salesPersonids = new StringBuffer();
                for (Object obj : masterItems) {
                    if (obj != null) {
                        salesPersonids.append(obj.toString() + ",");
                    }
                }
                if (salesPersonids.length() > 0) {
                    salesPersons = salesPersonids.substring(0, (salesPersonids.length() - 1));
                    requestParams.put("salesPersonid", salesPersons);
                }
            }
            
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }

            KwlReturnObject result = null;
            String companyid = "";
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                boolean isForTemplate = false;
                String billId = "";
                if (!StringUtil.isNullOrEmpty(request.getParameter("isForTemplate"))) {
                    isForTemplate = Boolean.parseBoolean(request.getParameter("isForTemplate"));
                    requestParams.put("isForTemplate", isForTemplate);
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) {
                    billId = request.getParameter("billid");
                    requestParams.put("billId", billId);
                }
                boolean isOutstanding = false;
                isOutstanding = request.getParameter("isOutstanding") != null ? Boolean.parseBoolean(request.getParameter("isOutstanding")) : false;
                requestParams.put("isOutstanding", isOutstanding);
                result = accPurchaseOrderobj.getSecurityGateEntryMerged(requestParams);
                DataJArr = accPurchaseOrderServiceDAOobj.getSecurityGateEntryJsonMerged(requestParams, result.getEntityList(), DataJArr);
            }
            int cnt = consolidateFlag ? DataJArr.length() : result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            if (consolidateFlag) {
                String start = request.getParameter(Constants.start);
                String limit = request.getParameter(Constants.limit);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            jobj.put("data", pagedJson);
            jobj.put("count", cnt);
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getSecurityGateEntryMerged : " + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    
    public ModelAndView getQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            HashMap<String, Object> requestParams = accPurchaseOrderServiceDAOobj.getPurchaseOrderMap(paramJobj);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            if(StringUtil.isNullOrEmpty(request.getParameter("archieve"))){
                requestParams.put("archieve", 0);
            } else {
                requestParams.put("archieve", Integer.parseInt(request.getParameter("archieve")));
            }    
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;            
            String[] companyids = (consolidateFlag && request.getParameter("companyids")!=null)?request.getParameter("companyids").split(","):sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);            
            boolean eliminateflag = consolidateFlag;
            if(consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
              ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE is true then user has permission to view all vendors documents,so at that time there is need to filter record according to user&agent. 
                     */
                    String userId = sessionHandlerImpl.getUserid(request);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
                Map<String, Object> salesPersonParams = new HashMap<>();

                salesPersonParams.put("userid", sessionHandlerImpl.getUserid(request));
                salesPersonParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                salesPersonParams.put("grID", "20");  //Agent groupID 
                KwlReturnObject masterItemByUserList = accountingHandlerDAOobj.getMasterItemByUserID(salesPersonParams);
                List<MasterItem> masterItems = masterItemByUserList.getEntityList();
                String salesPersons = "";
                StringBuffer salesPersonids = new StringBuffer();
                for (Object obj : masterItems) {
                    if (obj != null) {
                        salesPersonids.append(obj.toString() + ",");
                    }
                }
                if (salesPersonids.length() > 0) {
                    salesPersons = salesPersonids.substring(0, (salesPersonids.length() - 1));
                    requestParams.put("salesPersonid", salesPersons);
               } 
            
            }
            String dir = "";
            String sort = "";
             if(!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))){
                dir = request.getParameter("dir");
                 sort = request.getParameter("sort");
                   requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("linknumber"))){
                requestParams.put("linknumber", request.getParameter("linknumber"));
            }
             
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            
            KwlReturnObject result = null;
            String companyid = "";
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                
                result = accPurchaseOrderobj.getQuotations(requestParams);
                DataJArr = accPurchaseOrderServiceDAOobj.getQuotationsJson(paramJobj, result.getEntityList(), DataJArr);
            }
            int cnt = consolidateFlag?DataJArr.length():result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            if (request.getParameter("requestModuleid") != null && !StringUtil.isNullOrEmpty(request.getParameter("requestModuleid"))) {
                int requestModuleID=Integer.parseInt(request.getParameter("requestModuleid"));
                if(extraCompanyPreferences.isEnableLinkToSelWin()){
                    requestParams.put("requestModuleid", requestModuleID);    
                }
                if(extraCompanyPreferences.isEnableLinkToSelWin() && !Boolean.parseBoolean(request.getParameter("isGrid")) && (requestModuleID == Constants.Acc_Customer_Quotation_ModuleId || requestModuleID== Constants.Acc_FixedAssets_Purchase_Order_ModuleId ||  requestModuleID==Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId)){
                     pagedJson = StringUtil.getPagedJSON(pagedJson,0,10); // drop we have to show only 10 number
                }
            } else if(consolidateFlag) {
                String start = request.getParameter(Constants.start);
                String limit = request.getParameter(Constants.limit);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            jobj.put("data", pagedJson);
            jobj.put("count", cnt);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public JSONArray getDetailExcelJsonQuotation(HttpServletRequest request, HttpServletResponse response, HashMap<String, Object> requestParams, JSONArray DataJArr) throws JSONException, SessionExpiredException, ServiceException, SessionExpiredException, SessionExpiredException {
        boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
        String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
        String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
        String companyid = companyids[0];
        JSONArray tempArray = new JSONArray();
        for (int i = 0; i < DataJArr.length(); i++) {
            HashSet hs = new HashSet();
            String dono = "", doDate = "";
            JSONObject rowjobj = new JSONObject();
            rowjobj = DataJArr.getJSONObject(i);
            String billid = rowjobj.optString("billid", "");  
            request.setAttribute("companyid", companyid);
            request.setAttribute("gcurrencyid", gcurrencyid);
            request.setAttribute("billid", billid);
            JSONArray DataRowsArr = null;
            try {
                DataRowsArr = accPurchaseOrderServiceDAOobj.getQuotationRows(request).getJSONArray("data");
            } catch (ParseException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
            tempArray.put(rowjobj);
            for (int j = 0; j < DataRowsArr.length(); j++) {
                JSONObject tempjobj = new JSONObject();
                tempjobj = DataRowsArr.getJSONObject(j);
                exportDaoObj.editJsonKeyForExcelFile(tempjobj, Constants.Acc_Vendor_Quotation_ModuleId);
                tempArray.put(tempjobj);
            }
        }
        return tempArray;
    }
    public ModelAndView getQuotationRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = accPurchaseOrderServiceDAOobj.getQuotationRows(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accPurchaseOrderControllerCMN.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accPurchaseOrderControllerCMN.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView exportQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";
        try{
            request.setAttribute("isExport", true);
            JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
//            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            HashMap<String, Object> requestParams = accPurchaseOrderServiceDAOobj.getPurchaseOrderMap(paramJobj);
            if(StringUtil.isNullOrEmpty(request.getParameter("archieve"))){
                requestParams.put("archieve", 0);
            } else {
                requestParams.put("archieve", Integer.parseInt(request.getParameter("archieve")));
            }
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;            
            String[] companyids = (consolidateFlag && request.getParameter("companyids")!=null)?request.getParameter("companyids").split(","):sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);            
            boolean eliminateflag = consolidateFlag;
            if(consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE is true then user has permission to view all vendors documents,so at that time there is need to filter record according to user&agent. 
                     */
                    String userId = sessionHandlerImpl.getUserid(request);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
            }
            
            KwlReturnObject result = null;
            String companyid = "";
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
//                request.setAttribute("isExport", true);
                paramJobj.put("isExport", true);
                result = accPurchaseOrderobj.getQuotations(requestParams);
                DataJArr = accPurchaseOrderServiceDAOobj.getQuotationsJson(paramJobj, result.getEntityList(), DataJArr);
                if (request.getParameter("type")!=null && request.getParameter("type").equals("detailedXls")) {
                    DataJArr = getDetailExcelJsonQuotation(request, response, requestParams, DataJArr);
                }     
            }
            jobj.put("data", DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }else if(fileType.equals("xls")){
             request.setAttribute("isSummaryXls", true);
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView getRequisitions(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accPurchaseOrderServiceDAOobj.getRequisitions(paramJobj);
        } catch (JSONException | SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView exportRequisition(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";
        try{
           HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            boolean pendingapproval = (request.getParameter("pendingapproval") != null)? Boolean.parseBoolean(request.getParameter("pendingapproval")): false;
            if(StringUtil.isNullOrEmpty(request.getParameter("archieve"))){
                requestParams.put("archieve", 0);
            } else {
                requestParams.put("archieve", Integer.parseInt(request.getParameter("archieve")));
            }            
            requestParams.put("isExport", true);
            request.setAttribute("isExport", true);     //required when details excel export request
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;            
            String[] companyids = (consolidateFlag && request.getParameter("companyids")!=null)?request.getParameter("companyids").split(","):sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);            
            if(consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            KwlReturnObject result = null;
            String companyid = "";
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                boolean ispending=false; 
                if(!pendingapproval) {
                    result = accPurchaseOrderobj.getPurchaseRequisition(requestParams);
                } else {
                    ispending=true; 
                    requestParams.put("userid", sessionHandlerImpl.getUserid(request));
                    result = accPurchaseOrderobj.getPendingPurchaseRequisition(requestParams);
                }
                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                DataJArr = accPurchaseOrderServiceDAOobj.getRequisitionJson(paramJobj, result.getEntityList(), DataJArr,ispending,requestParams);
            }
            int cnt = consolidateFlag ? DataJArr.length() : result.getRecordTotalCount();
            if (request.getParameter("type") != null && request.getParameter("type").equals("detailedXls")) {
                DataJArr = getDetailJsonPurchaseRequisition(request, response, requestParams, DataJArr);
            }
            jobj.put("data", DataJArr);
            jobj.put("count", cnt);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }else if(fileType.equals("xls")){
             request.setAttribute("isSummaryXls", true);
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
      public JSONArray getDetailJsonPurchaseRequisition(HttpServletRequest request, HttpServletResponse response, HashMap<String, Object> requestParams, JSONArray DataJArr) throws JSONException, SessionExpiredException, ServiceException, SessionExpiredException, SessionExpiredException, SessionExpiredException, UnsupportedEncodingException {
        boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
        String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
        String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
        String companyid = companyids[0];
        JSONArray tempArray = new JSONArray();
         for (int i = 0; i < DataJArr.length(); i++) {
             JSONObject rowjobj = new JSONObject();
             rowjobj = DataJArr.getJSONObject(i);
             String billid = rowjobj.optString("billid", "");   //Invoice ID 
             request.setAttribute("companyid", companyid);
             request.setAttribute("gcurrencyid", gcurrencyid);
             request.setAttribute("billid", billid);
             JSONArray DataRowsArr = null;
             DataRowsArr = getRequisitionRows(request).getJSONArray("data");
             tempArray.put(rowjobj);
             rowjobj.put("type","");
             for (int j = 0; j < DataRowsArr.length(); j++) {
                 JSONObject tempjobj = new JSONObject();
                 tempjobj = DataRowsArr.getJSONObject(j);
                 exportDaoObj.editJsonKeyForExcelFile(tempjobj, Constants.Acc_Sales_Return_ModuleId);
                 tempArray.put(tempjobj);
             }
         }
        return tempArray;
    }
    public ModelAndView getRequisitionRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getRequisitionRows(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accPurchaseOrderControllerCMN.getRequisitionRows:" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accPurchaseOrderControllerCMN.getRequisitionRows:" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getRequisitionRows(HttpServletRequest request) throws SessionExpiredException, ServiceException, UnsupportedEncodingException {
        JSONObject jobj=new JSONObject();
        JSONObject data =new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> requiredRequestParams = AccountingManager.getGlobalParams(request);
            boolean closeflag = (request.getParameter("closeflag") != null) ? true : false;
            boolean isFixedAsset = Boolean.FALSE.parseBoolean(request.getParameter("isFixedAsset"));
            boolean isRFQfromFA_PR = (request.getParameter("isFixedAssetRFQ") != null) ? Boolean.parseBoolean(request.getParameter("isFixedAssetRFQ")) : false;
            boolean isRFQfromPR = (request.getParameter("isRFQ") != null) ? Boolean.parseBoolean(request.getParameter("isRFQ")) : false;
            boolean copyInv = Boolean.FALSE.parseBoolean(request.getParameter("copyInv"));
            boolean prvqlinkflag=(!StringUtil.isNullOrEmpty(request.getParameter("prvqlinkflag")))?Boolean.parseBoolean(request.getParameter("prvqlinkflag")):false;
            boolean prpolinkflag=(!StringUtil.isNullOrEmpty(request.getParameter("prpolinkflag")))?Boolean.parseBoolean(request.getParameter("prpolinkflag")):false;
            String companyid=sessionHandlerImpl.getCompanyid(request);
            HashMap<String,Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            DateFormat userDateFormat=null;
            boolean isUnitPriceHiddenInPR = accCommonTablesDAO.isUnitPriceHiddenForPR(companyid);
            if(paramJobj.has(Constants.userdateformat) && (paramJobj.get(Constants.userdateformat) != null) ){
                userDateFormat=new SimpleDateFormat((String)paramJobj.get(Constants.userdateformat));
            }

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            Country country = extraCompanyPreferences.getCompany().getCountry();
            String[] sos = request.getParameterValues("bills");
            if(request.getParameter("bills")!=null && request.getParameter("bills").contains(",")){
                sos = request.getParameter("bills").split(",");
            }
            if(request.getAttribute("billid")!=null){
                sos =(String[]) (request.getAttribute("billid").toString()).split(",");
            }
            String dType=request.getParameter("dtype");
            boolean isReport = false;
            boolean isExport = false;
            if(!StringUtil.isNullOrEmpty(dType) && StringUtil.equal(dType, "report")){
                isReport = true;
            }
            if (request.getAttribute("isExport") != null) {
                isExport = (boolean) request.getAttribute("isExport");
            }
            boolean isLinkRFQ=(!StringUtil.isNullOrEmpty(request.getParameter("isRFQ")))?Boolean.parseBoolean(request.getParameter("isRFQ")):false;
            boolean isPRlinktoVQ=(!StringUtil.isNullOrEmpty(request.getParameter("isPRlinktoVQ")))?Boolean.parseBoolean(request.getParameter("isPRlinktoVQ")):false;
            int i=0;
            JSONArray jArr=new JSONArray();
            double addobj = 1;
            
            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("purchaserequisition.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);
            
            // Get Line-level CUSTOMFIELDS For Purchase Requisition
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), !isFixedAsset?Constants.Acc_Purchase_Requisition_ModuleId:Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
            
            while(sos!=null&&i<sos.length){
                KwlReturnObject result = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), sos[i]);
                PurchaseRequisition so = (PurchaseRequisition) result.getEntityList().get(0);
                KWLCurrency currency = null;
                
                if(so.getCurrency() != null){
                    currency = so.getCurrency();
                } else {
                    currency=so.getVendor().getAccount().getCurrency()==null?kwlcurrency:so.getVendor().getAccount().getCurrency();
                }
                //KWLCurrency currency=so.getCustomer().getAccount().getCurrency()==null?kwlcurrency:so.getCustomer().getAccount().getCurrency();
                filter_params.clear();
                filter_params.add(so.getID());
                KwlReturnObject podresult = accPurchaseOrderobj.getPurchaseRequisitionDetails(soRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                
                while(itr.hasNext()) {
                    PurchaseRequisitionDetail row=(PurchaseRequisitionDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", so.getID());
                    obj.put("billno", so.getPrNumber());
                    obj.put("currencysymbol",currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("hasAccess", row.getProduct().isIsActive());
                    obj.put("productname",row.getProduct().getName());
//                    obj.put("unitname", row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getName());
                    obj.put("unitname", row.getUom()!=null?row.getUom().getNameEmptyforNA():row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("uomname", row.getUom()!=null?row.getUom().getNameEmptyforNA():row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("baseuomname", row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("multiuom", row.getProduct().isMultiuom());
                    obj.put("desc",StringUtil.DecodeText(row.getProductdescription()==null?"":row.getProductdescription().replaceAll("\\+", "%2b")));
                    obj.put("type",row.getProduct().getProducttype()==null?"":row.getProduct().getProducttype().getName());
                    obj.put("pid",row.getProduct().getProductid());
                    obj.put("memo", row.getRemark());
                    obj.put("approverremark", row.getApproverremark());
                    /*No required here 
                     Bcoz we put parent document id in linkid key
                     */
                   // obj.put("linkid",row.getPurchaserequisition().getID());
                    //obj.put("linkto",row.getPurchaserequisition().getPrNumber());
//                    obj.put("discountispercent", row.getDiscountispercent());
//                    obj.put("prdiscount", row.getDiscount());
                    
                    if(extraCompanyPreferences!=null && extraCompanyPreferences.getUomSchemaType()==Constants.PackagingUOM && row.getProduct() !=null){
                        Product product=row.getProduct();
                        obj.put("caseuom", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null)?product.getPackaging().getCasingUoM().getID():"");
                        obj.put("caseuomvalue", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null)?product.getPackaging().getCasingUomValue():1);
                        obj.put("inneruom", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null)?product.getPackaging().getInnerUoM().getID():"");
                        obj.put("inneruomvalue", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null)?product.getPackaging().getInnerUomValue():1);
                        obj.put("stockuom", (product.getUnitOfMeasure()!=null)?product.getUnitOfMeasure().getID():"");
                    }
                    if (so.isFixedAssetPurchaseRequisition()) {
                        HashMap<String, Object> assetDetailsParams = new HashMap<String, Object>();
                        assetDetailsParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                        assetDetailsParams.put("purchaseRequisitionDetailID", row.getID());
                        assetDetailsParams.put("moduleId", Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
                        
                        if (!copyInv) {
                            JSONArray aDetailsJArr = getFixedAssetPurchaseRequisitionDetails(request, assetDetailsParams);
                            obj.put("assetDetails", aDetailsJArr.toString());
                        }
                    }
                    
                    double rowTaxPercent = 0;
                    if (row.getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), so.getRequisitionDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("prtaxid",row.getTax()==null?"": row.getTax().getID());
                    obj.put("priceSource", row.getPriceSource() != null? row.getPriceSource() : "");
                     if (row.getPricingBandMasterid() != null) {
                        KwlReturnObject PricebandResult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), row.getPricingBandMasterid());
                        PricingBandMaster pricingBandMaster = PricebandResult != null ? (PricingBandMaster) PricebandResult.getEntityList().get(0) : null;
                        obj.put("pricingbandmasterid", pricingBandMaster != null ? pricingBandMaster.getID() : "");
                        obj.put("pricingbandmastername", pricingBandMaster != null ? pricingBandMaster.getName() : "");
                    }
                    
                    if(!isReport && row.getDiscount() > 0){//In Sales order creation, we need to display Unit Price including row discount
                        double discount = (row.getDiscountispercent() == 1)?(row.getRate() * (row.getDiscount()/100)) : row.getDiscount();
                        obj.put("rate", isUnitPriceHiddenInPR ? "0" : (row.getRate() - discount));
                        obj.put("discountispercent", 1);
                        obj.put("prdiscount", 0);
                    } else {
                        obj.put("rate", isUnitPriceHiddenInPR ? "0": row.getRate());
                        obj.put("discountispercent", row.getDiscountispercent());
                        obj.put("prdiscount", row.getDiscount());
                    }
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    PurchaseRequisitionDetailCustomData purchaseRequisitionDetailCustomData = (PurchaseRequisitionDetailCustomData) row.getPurchaseRequisitionDetailCustomData();
                    AccountingManager.setCustomColumnValues(purchaseRequisitionDetailCustomData, FieldMap, replaceFieldMap,variableMap);
                   
                    if (purchaseRequisitionDetailCustomData != null) {
                        JSONObject params = new JSONObject();
//                        boolean isExport = false;
//                        if (isReport) {
//                            isExport = true;
//                        }
                        params.put("isExport", isExport);
                        params.put("isForReport", isReport);
                        params.put(Constants.userdf, userDateFormat);
                        if ((isLinkRFQ || isPRlinktoVQ || prvqlinkflag || isRFQfromFA_PR || isRFQfromPR || prpolinkflag) && !isReport) {
                            int moduleId = isPRlinktoVQ ? Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId : prvqlinkflag ? Constants.Acc_Vendor_Quotation_ModuleId : isRFQfromFA_PR ? Constants.Acc_FixedAssets_RFQ_ModuleId : Constants.Acc_RFQ_ModuleId;
                            if (prpolinkflag) {
                                moduleId = Constants.Acc_Purchase_Order_ModuleId;
                            }
                            params.put("linkModuleId", moduleId);
                            params.put("isLink", true);
                            params.put("companyid", companyid);
                        }
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
//                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
//                        String coldata = varEntry.getValue().toString();
//                        String valueForReport = "";
//                        if (customFieldMap.containsKey(varEntry.getKey()) && (isReport || isLinkRFQ) && coldata != null) {
//                            try {
//                                String[] valueData = coldata.split(",");
//                                for (String value : valueData) {
//                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
//                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
//                                    if (fieldComboData != null) {
//                                        valueForReport += fieldComboData.getValue() + ",";
//                        }
//                        }
//                                if (valueForReport.length() > 1) {
//                                    valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
//                    }
//                                if (isLinkRFQ && !isReport) {
//                                    int moduleId = isFixedAsset ? Constants.Acc_FixedAssets_RFQ_ModuleId : Constants.Acc_RFQ_ModuleId;
//                                    valueForReport = fieldDataManagercntrl.getValuesForLinkRecords(moduleId, companyid, varEntry.getKey(), valueForReport,1);
//                                }
//                                obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
//                            } catch (Exception ex) {
//                                obj.put(varEntry.getKey(), coldata);
//                            }
//                        } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
//                            DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
//                            long milliSeconds = Long.parseLong(coldata);
//                            coldata = df2.format(milliSeconds);
//                            obj.put(varEntry.getKey(), coldata);
//                        } else {
//                            if (!StringUtil.isNullOrEmpty(coldata)) {
//                                obj.put(varEntry.getKey(), coldata);
//                            }
//                        }
//                    }
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getRequisitionDate(), 0);
                    obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                    
                    /*prpolinkflag ->True , If Requisition is linked with PO
                    
                     Fetching only unused quantity of Requisition in PO 
                    */
                    JSONObject columnprefObj = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                        columnprefObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                    }
                    boolean statusOfRequisitionForPO = false;
                    if (columnprefObj.has("statusOfRequisitionForPO") && columnprefObj.get("statusOfRequisitionForPO") != null && (Boolean) columnprefObj.get("statusOfRequisitionForPO") != false) {
                        statusOfRequisitionForPO = true;
                    }
                    
                    if (statusOfRequisitionForPO && prpolinkflag && !prvqlinkflag) {
                        HashMap requestParameter = new HashMap();
                        requestParameter.put("requisitionDetailId", row.getID());
                        requestParameter.put("quantityOfRequisitionDetail", row.getQuantity());
                        double quantity = accPurchaseOrderModuleServiceObj.calCulateBalanceQtyOfRequisitionForPO(requestParameter);
                        addobj = quantity;
                        obj.put("quantity", quantity);
                    } else {
                        obj.put("quantity", row.getQuantity());
                    }                    
                        if(row.getUom()!=null) {
                            obj.put("uomid", row.getUom().getID());
                            obj.put("baseuomquantity", row.getBaseuomquantity());
                            obj.put("baseuomrate", row.getBaseuomrate());
                        } else {
                            obj.put("uomid", row.getProduct().getUnitOfMeasure()!=null?row.getProduct().getUnitOfMeasure().getID():"");
                            obj.put("baseuomquantity", row.getBaseuomquantity());
                            obj.put("baseuomrate", row.getBaseuomrate());
                        }
                        
                    double quantity = 0.0;
//                    double invoiceRowProductQty = authHandler.calculateBaseUOMQuatity(row.getQuantity(), row.getBaseuomrate());
//                    double remainedQty = invoiceRowProductQty; // which has not been linked yet
                    if (isFixedAsset && closeflag) {
                        addobj = getPurchaseRequisitionDetailStatus(row);
                        quantity = addobj;
                        obj.put("quantity", quantity);
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, row.getBaseuomrate(), companyid));
                        obj.put("baseuomrate", row.getBaseuomrate());

//                        remainedQty = authHandler.calculateBaseUOMQuatity(quantity, row.getBaseuomrate());

                    } else {
                         //obj.put("quantity", row.getQuantity());//Unwanted Code , As quantity is already put above in json object
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(row.getQuantity(), row.getBaseuomrate(), companyid));
                        obj.put("baseuomrate", row.getBaseuomrate());
                    }   
                    
                    if (isExport && !statusOfRequisitionForPO) {
                        obj.put("balanceQuantity", "NA");
                    } else {
                        obj.put("balanceQuantity", row.getBalanceqty());
                    }
                   
                    JSONObject jObj = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                        jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                    }
                    if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                        obj = accProductObj.getProductDisplayUOM(row.getProduct(),row.getQuantity(), row.getBaseuomrate(), true, obj);
                    }
                    //Code not Used ERP-35989 - ERP-35976
                    /*if (extraCompanyPreferences.getLineLevelTermFlag()==1) { // Fetch Vat term details of Product
                        String salesOrPurchase = request.getParameter("termSalesOrPurchaseCheck") != null ? request.getParameter("termSalesOrPurchaseCheck").toString() : "false";
               
                        /  While requisition is linking with PO or VQ
                         prvqlinkflag->True, if Requisition->VQ 
                         prpolinkflag->True, if Requisition->PO
                         ------------/
                         if (extraCompanyPreferences.isIsNewGST() && (prvqlinkflag  || prpolinkflag)) {

                            //------Applying Taxes on Line level as per Vendor Address, If Requisition is linking with PO or VQ----------- 
                            paramJobj.put("productids", row.getProduct().getID() + ",");
                            data = accEntityGstService.getGSTForProduct(paramJobj, requiredRequestParams);
                            obj.put("LineTermdetails", (data.getJSONArray("prodTermArray") != null && data.getJSONArray("prodTermArray").getJSONObject(0) != null) ? data.getJSONArray("prodTermArray").getJSONObject(0).getString("LineTermdetails") : "[]");

                        } else {
                            Map<String, Object> mapData = new HashMap<String, Object>();
                            mapData.put("productid", row.getProduct().getID());
                            mapData.put("salesOrPurchase", salesOrPurchase);
                            mapData.put("isDefault", true);
                            KwlReturnObject result6 = accProductObj.getProductTermDetails(mapData);
                            if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                                ArrayList<ProductTermsMap> productTermDetail = (ArrayList<ProductTermsMap>) result6.getEntityList();
                                JSONArray productTermJsonArry = new JSONArray();
                                for (ProductTermsMap productTermsMapObj : productTermDetail) {
                                    JSONObject productTermJsonObj = new JSONObject();
                                    productTermJsonObj.put("productid", productTermsMapObj.getProduct().getID());
                                    productTermJsonObj.put("termid", productTermsMapObj.getTerm().getId());
                                    productTermJsonObj.put("term", productTermsMapObj.getTerm().getTerm());
                                    productTermJsonObj.put("formula", productTermsMapObj.getTerm().getFormula());
                                    productTermJsonObj.put("formulaids", productTermsMapObj.getTerm().getFormula());
                                    productTermJsonObj.put("termpercentage", productTermsMapObj.getTerm().getPercentage());
                                    productTermJsonObj.put("originalTermPercentage", productTermsMapObj.getTerm().getPercentage()); // For service abatement calculation
                                    productTermJsonObj.put("glaccountname", productTermsMapObj.getAccount().getAccountName());
                                    productTermJsonObj.put("accountid", productTermsMapObj.getAccount().getID());
                                    productTermJsonObj.put("glaccount", productTermsMapObj.getAccount().getID());
                                    productTermJsonObj.put("IsOtherTermTaxable", productTermsMapObj.getTerm().isOtherTermTaxable());
                                    productTermJsonObj.put("sign", productTermsMapObj.getTerm().getSign());
                                    productTermJsonObj.put("taxtype", productTermsMapObj.getTaxType());
                                    productTermJsonObj.put("taxvalue", productTermsMapObj.getTaxType() == 0 ? productTermsMapObj.getTermAmount() : productTermsMapObj.getPercentage());
                                    productTermJsonObj.put("termtype", productTermsMapObj.getTerm().getTermType());
                                    productTermJsonObj.put("termsequence", productTermsMapObj.getTerm().getTermSequence());
                                    productTermJsonObj.put("purchasevalueorsalevalue", productTermsMapObj.getPurchaseValueOrSaleValue());
                                    productTermJsonObj.put("deductionorabatementpercent", productTermsMapObj.getDeductionOrAbatementPercent());
                                    productTermJsonObj.put("payableaccountid", productTermsMapObj.getTerm().getPayableAccount() != null ? productTermsMapObj.getTerm().getPayableAccount().getID() : "");
                                    productTermJsonArry.put(productTermJsonObj);
                                }
                                obj.put("LineTermdetails", productTermJsonArry.toString());
                            }
                        }
                    }*/
                 
                    if (addobj > 0) {
                        jArr.put(obj);
                    }
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }/* catch (ParseException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return jobj;
    }
    
    public double getPurchaseRequisitionDetailStatus(PurchaseRequisitionDetail pReqDetail) throws ServiceException {
        double result = pReqDetail.getQuantity();

        KwlReturnObject idresult = accPurchaseOrderobj.getIDFromPurchaseRequisitionDetails(pReqDetail.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0;
        while (ite1.hasNext()) {
            VendorQuotationDetail vqDetail = (VendorQuotationDetail) ite1.next();
            qua += vqDetail.getQuantity();
        }
        result = result - qua;
        return result;
    }
    
    public JSONArray getFixedAssetPurchaseRequisitionDetails(HttpServletRequest request, HashMap<String, Object> assetDetailsParams) throws SessionExpiredException, ServiceException, JSONException {
        JSONArray assetDetailsJArr = new JSONArray();
        DateFormat df = authHandler.getDateOnlyFormat();
        boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag"))) ? false : Boolean.parseBoolean(request.getParameter("linkingFlag"));
        HashMap<String, Object> assParams = new HashMap<String, Object>();
        assParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
        assParams.put("invrecord", false);
        KwlReturnObject assResult = accProductObj.getPurchaseRequisitionAssetDetails(assParams);

        List assetList = assResult.getEntityList();

        List<String> assetNameList = new ArrayList<String>();

        Iterator it = assetList.iterator();
        while (it.hasNext()) {
            PurchaseRequisitionAssetDetails ad = (PurchaseRequisitionAssetDetails) it.next();
            assetNameList.add(ad.getAssetId());
        }
        
        KwlReturnObject assetInvMapObj = accProductObj.getAssetPurchaseRequisitionDetailMapping(assetDetailsParams);
        List assetInvMapList = assetInvMapObj.getEntityList();
        Iterator assetInvMapListIt = assetInvMapList.iterator();

        while (assetInvMapListIt.hasNext()) {
            AssetPurchaseRequisitionDetailMapping invoiceDetailMapping = (AssetPurchaseRequisitionDetailMapping) assetInvMapListIt.next();
            PurchaseRequisitionAssetDetails assetDetails = invoiceDetailMapping.getPurchaseRequisitionAssetDetails();

            JSONObject assetDetailsJOBJ = new JSONObject();
            if (linkingFlag) { // in case of edit linkingFlag comes false so manualy checking that it is linked with transactions or not
//                if (linkingFlag && assetNameList.contains(assetDetails.getAssetId())) { // don't put assets which are included in Invoice
//                    continue;
//                }
                assetDetailsJOBJ.put("assetId", assetDetails.getId());
            } else {
                assetDetailsJOBJ.put("assetId", assetDetails.getAssetId());
            }
            assetDetailsJOBJ.put("assetdetailId", assetDetails.getId());
            assetDetailsJOBJ.put("assetName", assetDetails.getAssetId());
            assetDetailsJOBJ.put("location", (assetDetails.getLocation() != null) ? assetDetails.getLocation().getId() : "");
            assetDetailsJOBJ.put("department", (assetDetails.getDepartment() != null) ? assetDetails.getDepartment().getId() : "");
            assetDetailsJOBJ.put("assetdescription", (assetDetails.getAssetDescription() != null) ? assetDetails.getAssetDescription() : "");
            assetDetailsJOBJ.put("assetUser", (assetDetails.getAssetUser() != null) ? assetDetails.getAssetUser().getUserID() : "");
            assetDetailsJOBJ.put("cost", assetDetails.getCost());
            assetDetailsJOBJ.put("costInForeignCurrency", assetDetails.getCostInForeignCurrency());
            assetDetailsJOBJ.put("salvageRate", assetDetails.getSalvageRate());
            assetDetailsJOBJ.put("salvageValue", assetDetails.getSalvageValue());
            assetDetailsJOBJ.put("salvageValueInForeignCurrency", assetDetails.getSalvageValueInForeignCurrency());
            assetDetailsJOBJ.put("accumulatedDepreciation", assetDetails.getAccumulatedDepreciation());
            assetDetailsJOBJ.put("wdv", assetDetails.getWdv());
            assetDetailsJOBJ.put("assetLife", assetDetails.getAssetLife());
            assetDetailsJOBJ.put("elapsedLife", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("nominalValue", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("batch", assetDetails.getBatch());
            assetDetailsJOBJ.put("installationDate", df.format(assetDetails.getInstallationDate()));
            assetDetailsJOBJ.put("purchaseDate", df.format(assetDetails.getPurchaseDate()));
            assetDetailsJArr.put(assetDetailsJOBJ);
        }
        return assetDetailsJArr;
    }
    
    public ModelAndView getRFQs(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accPurchaseOrderServiceDAOobj.getRFQs(paramJobj);
        } catch (JSONException | SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView exportRFQs(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            boolean pendingapproval = (request.getParameter("pendingapproval") != null)? Boolean.parseBoolean(request.getParameter("pendingapproval")): false;
            if(StringUtil.isNullOrEmpty(request.getParameter("archieve"))){
                requestParams.put("archieve", 0);
            } else {
                requestParams.put("archieve", Integer.parseInt(request.getParameter("archieve")));
            } 
            requestParams.put("isExport", true);
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;            
            String[] companyids = (consolidateFlag && request.getParameter("companyids")!=null)?request.getParameter("companyids").split(","):sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);            
            if(consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            KwlReturnObject result = null;
            String companyid = "";
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                if(!pendingapproval) {
                    result = accPurchaseOrderobj.getRequestForQuotations(requestParams);
                } else {
                    requestParams.put("userid", sessionHandlerImpl.getUserid(request));
                    result = accPurchaseOrderobj.getPendingPurchaseRequisition(requestParams);
                }
//                DataJArr = getRFQJson(request, result.getEntityList(), DataJArr, requestParams);
                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                DataJArr =accPurchaseOrderServiceDAOobj.getRFQJson(paramJobj, result.getEntityList(), DataJArr, requestParams); 
            }
            int cnt = consolidateFlag?DataJArr.length():result.getRecordTotalCount();
            if (request.getParameter("type") != null && request.getParameter("type").equals("detailedXls")) {
                DataJArr = getDetailJsonRFQs(request, response, requestParams, DataJArr);
            }
            jobj.put("data", DataJArr);
            jobj.put("count", cnt);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }else if(fileType.equals("xls")){
             request.setAttribute("isSummaryXls", true);
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public JSONArray getDetailJsonRFQs(HttpServletRequest request, HttpServletResponse response, HashMap<String, Object> requestParams, JSONArray DataJArr) throws JSONException, SessionExpiredException, ServiceException, SessionExpiredException, SessionExpiredException, SessionExpiredException, UnsupportedEncodingException {
        boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
        String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
        String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
        String companyid = companyids[0];
        JSONArray tempArray = new JSONArray();
        for (int i = 0; i < DataJArr.length(); i++) {
            JSONObject rowjobj = new JSONObject();
            rowjobj = DataJArr.getJSONObject(i);
            String billid = rowjobj.optString("billid", "");   //Invoice ID 
            request.setAttribute("companyid", companyid);
            request.setAttribute("gcurrencyid", gcurrencyid);
            request.setAttribute("billid", billid);
            JSONArray DataRowsArr = null;
            DataRowsArr = getRFQRows(request,requestParams).getJSONArray("data");
            tempArray.put(rowjobj);
            rowjobj.put("type", "");
            for (int j = 0; j < DataRowsArr.length(); j++) {
                JSONObject tempjobj = new JSONObject();
                tempjobj = DataRowsArr.getJSONObject(j);
                exportDaoObj.editJsonKeyForExcelFile(tempjobj, Constants.Acc_RFQ_ModuleId);
                tempArray.put(tempjobj);
            }
        }
        return tempArray;
    }

    public ModelAndView getRFQRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getRFQRows(request,new HashMap());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accPurchaseOrderControllerCMN.getRFQRows:" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accPurchaseOrderControllerCMN.getRFQRows:" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getRFQRows(HttpServletRequest request,HashMap<String, Object> requestParams1) throws SessionExpiredException, ServiceException {
        JSONObject jobj=new JSONObject();
        try {
            boolean isFixedAsset = (request.getParameter("isFixedAsset") != null) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean copyInv = Boolean.FALSE.parseBoolean(request.getParameter("copyInv"));
            String companyid=sessionHandlerImpl.getCompanyid(request);
            boolean isForReport = false;
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("dtype")) && request.getParameter("dtype").equals("report")){
                isForReport = true;
            }
            boolean isExport = false;
            if (requestParams1.containsKey("isExport") && requestParams1.get("isExport") != null) { // True in case of Export
                isExport = (Boolean) requestParams1.get("isExport");
            }
            
            HashMap<String,Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));

//            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
//            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            Country country = extraCompanyPreferences.getCompany().getCountry();
            String bills = request.getParameter("bills");
            String sos[]=null;
            if(!StringUtil.isNullOrEmpty(bills)){
                sos = bills.split(",");
            }
            if (request.getAttribute("billid") != null) {
                sos = (String[]) (request.getAttribute("billid").toString()).split(",");
            }
            String dType = request.getParameter("dtype");
            boolean isReport = false;
            if(!StringUtil.isNullOrEmpty(dType) && StringUtil.equal(dType, "report")){
                isReport = true;
            }
            int i=0;
            JSONArray jArr=new JSONArray();
            int addobj = 1;
            boolean isForLinking = Boolean.FALSE.parseBoolean(request.getParameter("isForLinking"));
            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("requestforquotation.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);

            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), isFixedAsset?Constants.Acc_FixedAssets_RFQ_ModuleId:Constants.Acc_RFQ_ModuleId,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
            
            while(sos!=null&&i<sos.length){
                KwlReturnObject result = accountingHandlerDAOobj.getObject(RequestForQuotation.class.getName(), sos[i]);
                RequestForQuotation so = (RequestForQuotation) result.getEntityList().get(0);
//                KWLCurrency currency = null;
                
//                if(so.getCurrency() != null){
//                    currency = so.getCurrency();
//                } else {
//                    currency=so.getVendor().getAccount().getCurrency()==null?kwlcurrency:so.getVendor().getAccount().getCurrency();
//                }
                //KWLCurrency currency=so.getCustomer().getAccount().getCurrency()==null?kwlcurrency:so.getCustomer().getAccount().getCurrency();
                filter_params.clear();
                filter_params.add(so.getID());
                KwlReturnObject podresult = accPurchaseOrderobj.getRFQDetails(soRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                
                while(itr.hasNext()) {
                    RequestForQuotationDetail row=(RequestForQuotationDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", so.getID());
                    obj.put("billno", so.getRfqNumber());
//                    obj.put("currencysymbol",currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("productname",row.getProduct().getName());
//                    obj.put("unitname", row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getName());
                    obj.put("unitname", row.getUom()!=null?row.getUom().getNameEmptyforNA():row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("uomname", row.getUom()!=null?row.getUom().getNameEmptyforNA():row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("baseuomname", row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("multiuom", row.getProduct().isMultiuom());
                    obj.put("desc",StringUtil.DecodeText(row.getRemark()));
                    obj.put("type",row.getProduct().getProducttype()==null?"":row.getProduct().getProducttype().getName());
                    obj.put("pid",row.getProduct().getProductid());
                    obj.put("memo", row.getRemark());
                    obj.put("linkid", row.getPrid() == null ? "" : row.getPrid().getID());
                    obj.put("discountispercent", 1);
                    obj.put("prdiscount", 0);
                    obj.put("rate", row.getRate());
                    obj.put("linkto", row.getPrid() == null ? "" : row.getPrid().getPrNumber());
                    obj.put("priceSource", row.getPriceSource() != null? row.getPriceSource() : "");                    
                    if (row.getPricingBandMasterid() != null) {
                        KwlReturnObject PricebandResult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), row.getPricingBandMasterid());
                        PricingBandMaster pricingBandMaster = PricebandResult != null ? (PricingBandMaster) PricebandResult.getEntityList().get(0) : null;
                        obj.put("pricingbandmasterid", pricingBandMaster != null ? pricingBandMaster.getID() : "");
                        obj.put("pricingbandmastername", pricingBandMaster != null ? pricingBandMaster.getName() : "");
                    }
                    /*
                     * commented below code as the type of prid in RequestForQuotationDetail 
                     * is changed to reference type
                     */
//                    if(row.getPrid() != null){
//                        KwlReturnObject pr_result = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), row.getPrid());
//                        List pr_List = pr_result.getEntityList();
//                        if(pr_List.size() > 0 && pr_List.get(0) != null){
//                            PurchaseRequisition purchaseRequisition = (PurchaseRequisition) pr_List.get(0);
//                            if(purchaseRequisition != null && purchaseRequisition.getPrNumber()!=null){
//                                obj.put("linkto",  purchaseRequisition.getPrNumber());
//                            }
//                        }
//                    }
//                    obj.put("discountispercent", row.getDiscountispercent());
//                    obj.put("prdiscount", row.getDiscount());
                    double rowTaxPercent = 0;
//                    if (row.getTax() != null) {
//                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), so.getRequisitionDate(), row.getTax().getID());
//                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
//                    }
                    obj.put("prtaxpercent", rowTaxPercent);
//                    obj.put("prtaxid",row.getTax()==null?"": row.getTax().getID());
                    
//                    if(!isReport && row.getDiscount() > 0){//In Sales order creation, we need to display Unit Price including row discount
//                        double discount = (row.getDiscountispercent() == 1)?(row.getRate() * (row.getDiscount()/100)) : row.getDiscount();
//                        obj.put("rate", (row.getRate() - discount));
//                        obj.put("discountispercent", 1);
//                        obj.put("prdiscount", 0);
//                    } else {
//                        obj.put("rate", row.getRate());
//                        obj.put("discountispercent", row.getDiscountispercent());
//                        obj.put("prdiscount", row.getDiscount());
//                    }
                    
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getRequisitionDate(), 0);
//                    obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                    
                    if(extraCompanyPreferences!=null && extraCompanyPreferences.getUomSchemaType()==Constants.PackagingUOM && row.getProduct() !=null){
                        Product product=row.getProduct();
                        obj.put("caseuom", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null)?product.getPackaging().getCasingUoM().getID():"");
                        obj.put("caseuomvalue", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null)?product.getPackaging().getCasingUomValue():1);
                        obj.put("inneruom", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null)?product.getPackaging().getInnerUoM().getID():"");
                        obj.put("inneruomvalue", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null)?product.getPackaging().getInnerUomValue():1);
                        obj.put("stockuom", (product.getUnitOfMeasure()!=null)?product.getUnitOfMeasure().getID():"");
                    }
                    
                    if (so.isFixedAssetRFQ()) {
                        HashMap<String, Object> assetDetailsParams = new HashMap<String, Object>();
                        assetDetailsParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                        assetDetailsParams.put("purchaseRequisitionDetailID", row.getID());
                        assetDetailsParams.put("moduleId", Constants.Acc_FixedAssets_RFQ_ModuleId);
                        if(!copyInv){
                            JSONArray aDetailsJArr = getFixedAssetPurchaseRequisitionDetails(request, assetDetailsParams);
                            obj.put("assetDetails", aDetailsJArr.toString());
                        }
                    }
                    
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    RequestForQuotationDetailCustomData requestForQuotationDetailCustomData = (RequestForQuotationDetailCustomData) row.getRequestForQuotationDetailCustomData();
                    HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
                    AccountingManager.setCustomColumnValues(requestForQuotationDetailCustomData, FieldMap, replaceFieldMap1, variableMap);
                    
                     if (requestForQuotationDetailCustomData != null) {
                        JSONObject params = new JSONObject();
                        
//                        if (isForReport) {
//                            isExport = true;
//                                }
                        params.put("isExport", isExport);
                        params.put("isForReport", isReport);
                         if (isForLinking) {
                             params.put("linkModuleId", Constants.Acc_Vendor_Quotation_ModuleId);
                             params.put("isLink", true);
                             params.put("companyid", companyid);
                         }
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                                }
                    
                    
//                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
//                        String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
//                        String valueForReport = "";
//                        if (customFieldMap.containsKey(varEntry.getKey()) && isReport && coldata != null) {
//                            try {
//                                String[] valueData = coldata.split(",");
//                                for (String value : valueData) {
//                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
//                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
//                            if (fieldComboData != null) {
//                                        valueForReport += fieldComboData.getValue() + ",";
//                                }
//                                }
//                                if (valueForReport.length() > 1) {
//                                    valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
//                                }
//                                obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
//                            } catch (Exception ex) {
//                                obj.put(varEntry.getKey(), coldata);
//                            }
//                        } else if (customDateFieldMap.containsKey(varEntry.getKey()) && isReport) {
//                            DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
//                            long milliSeconds = Long.parseLong(coldata);
//                            coldata = df2.format(milliSeconds);
//                            obj.put(varEntry.getKey(), coldata);
//                            } else {
//                            if (!StringUtil.isNullOrEmpty(coldata)) {
//                                obj.put(varEntry.getKey(), coldata);
//                            }
//                        }
//                    }
                    
                    obj.put("quantity", row.getQuantity());
                    if(row.getUom()!=null) {
                        obj.put("uomid", row.getUom().getID());
                        obj.put("baseuomquantity", row.getBaseuomquantity());
                        obj.put("baseuomrate", row.getBaseuomrate());
                    } else {
                        obj.put("uomid", row.getProduct().getUnitOfMeasure()!=null?row.getProduct().getUnitOfMeasure().getID():"");
                        obj.put("baseuomquantity", row.getBaseuomquantity());
                        obj.put("baseuomrate", row.getBaseuomrate());
                    }
                    /*if (extraCompanyPreferences.getLineLevelTermFlag()==1) { // Fetch Vat term details of Product
                       String salesOrPurchase = request.getParameter("termSalesOrPurchaseCheck") != null ? request.getParameter("termSalesOrPurchaseCheck").toString() : "false";
                        Map<String, Object> mapData = new HashMap<String, Object>();
                        mapData.put("productid", row.getProduct().getID());
                        mapData.put("salesOrPurchase", salesOrPurchase);
                        mapData.put("isDefault", true);
                        KwlReturnObject result6 = accProductObj.getProductTermDetails(mapData);
                        if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                            ArrayList<ProductTermsMap> productTermDetail = (ArrayList<ProductTermsMap>) result6.getEntityList();
                            JSONArray productTermJsonArry = new JSONArray();
                            for (ProductTermsMap productTermsMapObj : productTermDetail) {
                                JSONObject productTermJsonObj = new JSONObject();
                                productTermJsonObj.put("productid", productTermsMapObj.getProduct().getID());
                                productTermJsonObj.put("termid", productTermsMapObj.getTerm().getId());
                                productTermJsonObj.put("term", productTermsMapObj.getTerm().getTerm());
                                productTermJsonObj.put("formula", productTermsMapObj.getTerm().getFormula());
                                productTermJsonObj.put("formulaids", productTermsMapObj.getTerm().getFormula());
                                productTermJsonObj.put("termpercentage", productTermsMapObj.getTerm().getPercentage());
                                productTermJsonObj.put("glaccountname", productTermsMapObj.getAccount().getAccountName());
                                productTermJsonObj.put("accountid", productTermsMapObj.getAccount().getID());
                                productTermJsonObj.put("glaccount", productTermsMapObj.getAccount().getID());
                                productTermJsonObj.put("IsOtherTermTaxable", productTermsMapObj.getTerm().isOtherTermTaxable());
                                productTermJsonObj.put("sign", productTermsMapObj.getTerm().getSign());
                                productTermJsonObj.put("taxtype", productTermsMapObj.getTaxType());
                                productTermJsonObj.put("taxvalue", productTermsMapObj.getTaxType()==0 ? productTermsMapObj.getTermAmount() : productTermsMapObj.getPercentage());
                                
                                //Added missing values in json while linking it to the other documents
                                productTermJsonObj.put("originalTermPercentage", productTermsMapObj.getTerm().getPercentage()); // For service abatement calculation
                                productTermJsonObj.put("termtype", productTermsMapObj.getTerm().getTermType());
                                productTermJsonObj.put("termsequence", productTermsMapObj.getTerm().getTermSequence());
                                productTermJsonObj.put("purchasevalueorsalevalue", productTermsMapObj.getPurchaseValueOrSaleValue());
                                productTermJsonObj.put("deductionorabatementpercent", productTermsMapObj.getDeductionOrAbatementPercent());
                                productTermJsonObj.put("formType", !StringUtil.isNullOrEmpty(productTermsMapObj.getFormType())?productTermsMapObj.getFormType():"1");
                                productTermJsonObj.put("accode", !StringUtil.isNullOrEmpty(productTermsMapObj.getAccount().getAcccode()) ? productTermsMapObj.getAccount().getAcccode() : "");
                                productTermJsonObj.put("isDefault", productTermsMapObj.isIsDefault());
                                productTermJsonObj.put("producttermmapid", productTermsMapObj.getId());
                                productTermJsonObj.put("payableaccountid", productTermsMapObj.getTerm().getPayableAccount() != null ? productTermsMapObj.getTerm().getPayableAccount().getID() : "");
                                productTermJsonArry.put(productTermJsonObj); 
                            }
                            obj.put("LineTermdetails", productTermJsonArry.toString());
                        }
                    }*/
                    if (addobj > 0) {
                        jArr.put(obj);
                    }
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    
public void exportSinglePO(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException, JSONException {
        
        
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        HashMap<String, Object> otherconfigrequestParams = new HashMap();
        String POID = paramJobj.optString("bills");
        String companyid =paramJobj.getString(Constants.companyKey);
        int moduleid = 0;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.moduleid,null))) {
            moduleid = Integer.parseInt(paramJobj.optString(Constants.moduleid));
        }
        //change module id for asset module
        boolean isFixedAsset = false;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isFixedAsset,null))) {
            isFixedAsset = paramJobj.optBoolean(Constants.isFixedAsset, false);
        }
        if(isFixedAsset){
            moduleid = Constants.Acc_FixedAssets_Purchase_Order_ModuleId;
        }
        
        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), POID);
        PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
        AccCustomData accCustomData = null;
        if (purchaseOrder.getPoCustomData() != null) {
            accCustomData = purchaseOrder.getPoCustomData();
        }
        String recordids = "";
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("recordids",null))) {
            recordids = paramJobj.optString("recordids");
        }
        ArrayList<String> POIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
        replaceFieldMap = new HashMap<String, String>();
        /*
         * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
         */
        fieldrequestParams.clear();
        HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
        HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

        fieldrequestParams.clear();
        HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
        HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);
        //reset module id
        if(isFixedAsset){
            moduleid = Constants.Acc_Purchase_Order_ModuleId;
        }
        
        //For product custom field
        fieldrequestParams.clear();
        HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
        HashMap<String, Integer> ProductLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);
        
        HashMap<String, Object> paramMap = new HashMap();
        paramMap.put(Constants.fieldMap, FieldMap);
        paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
        paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
        paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
        paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);

        HashMap<String, JSONArray> itemDataPO = new HashMap<String, JSONArray>();
        for (int count = 0; count < POIDList.size(); count++) {
            JSONArray lineItemsArr = accPurchaseOrderServiceDAOobj.getPODetailsItemJSON(paramJobj, POIDList.get(count), paramMap);
            itemDataPO.put(POIDList.get(count), lineItemsArr);
            // Below Function called to update print flag for PO Report
            accCommonTablesDAO.updatePrintFlag(moduleid, POIDList.get(count), companyid);
        }
        otherconfigrequestParams.put(Constants.moduleid, moduleid);
        String invoicePostText = purchaseOrder.getPostText() == null ? "" : purchaseOrder.getPostText();
        ExportRecordHandler.exportSingleGeneric(request, response, itemDataPO, accCustomData, customDesignDAOObj, accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj, velocityEngine, invoicePostText, otherconfigrequestParams, accInvoiceServiceDAOObj, accGoodsReceiptServiceDAOObj);
    }
    
       public ModelAndView getQAApprovalItems(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(Constants.start, request.getParameter(Constants.start));
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            boolean isPending = (StringUtil.isNullOrEmpty(request.getParameter("isPending"))) ? false : Boolean.parseBoolean(request.getParameter("isPending"));
            requestParams.put("isPending", isPending);
            KwlReturnObject result = accPurchaseOrderobj.getQAApprovalItems(requestParams);
            jobj = getQAApprovalItemsJson(requestParams, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getQAApprovalItems : " + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getQAApprovalItemsJson(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            DateFormat df = (DateFormat) requestParams.get("df");
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                PurchaseOrderDetail purchaseOrderDetail = (PurchaseOrderDetail) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", purchaseOrderDetail.getID());
                obj.put("qastatusremark", purchaseOrderDetail.getQastatusremark());
                obj.put("quantity", purchaseOrderDetail.getQuantity());
                obj.put("productid", purchaseOrderDetail.getProduct().getID());
                obj.put("productcode", purchaseOrderDetail.getProduct().getProductid());
                obj.put("productname", purchaseOrderDetail.getProduct().getName());
                obj.put("company", purchaseOrderDetail.getCompany());
                obj.put("description", purchaseOrderDetail.getDescription());
                obj.put("qastatus", purchaseOrderDetail.getQastatus());
                obj.put("vendorid", purchaseOrderDetail.getPurchaseOrder().getVendor().getID());
                obj.put("vendorname", purchaseOrderDetail.getPurchaseOrder().getVendor().getName());
                obj.put("billno", purchaseOrderDetail.getPurchaseOrder().getPurchaseOrderNumber());
                obj.put("duedate", df.format(purchaseOrderDetail.getPurchaseOrder().getDueDate()));
                obj.put("date", df.format(purchaseOrderDetail.getPurchaseOrder().getOrderDate()));
                obj.put("memo", purchaseOrderDetail.getPurchaseOrder().getMemo());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getQAApprovalItemsJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView updateQAApprovalItems(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            if(!StringUtil.isNullOrEmpty(request.getParameter("jsondata")) && !StringUtil.isNullOrEmpty(request.getParameter("isApproved"))){
                 
                JSONArray itemArray=new JSONArray(request.getParameter("jsondata"));
                
                boolean isApproved=Boolean.parseBoolean(request.getParameter("isApproved"));
                 int qaStatus=Constants.Pending_QA_Approval;
                 if(isApproved){
                     qaStatus=Constants.APPROVED;
                 }else{
                     qaStatus=Constants.QA_Rejected;
                 } 
                 
                 String qastatusremark="";
                 if(!StringUtil.isNullOrEmpty(request.getParameter("qastatusremark"))){
                        qastatusremark=request.getParameter("qastatusremark");
                 }
                
                 for (int count = 0; count < itemArray.length(); count++) {
                    JSONObject itemObject=itemArray.getJSONObject(count);
                    
                    if(!itemObject.isNull("id") && !StringUtil.isNullOrEmpty(itemObject.optString("id"))){
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(),itemObject.getString("id"));
                        PurchaseOrderDetail purchaseOrderDetail=(PurchaseOrderDetail) rdresult.getEntityList().get(0);
                        HashMap hMap = new HashMap();
                        hMap.put("purchaseOrderDetail", purchaseOrderDetail);
                        hMap.put("quantity", itemObject.optDouble("quantity",0));
                        hMap.put("qastatus", qaStatus);
                        if(!StringUtil.isNullOrEmpty(qastatusremark)){
                            hMap.put("qastatusremark", qastatusremark);
                        }                        
                        accPurchaseOrderobj.updateQAApprovalItems(hMap);
                    }                    
                }
                 if(itemArray.length()>0){
                     msg="Selected items are updated successfully ";
                 }                
            }             
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.updateQAApprovalItems : " + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }  
  
     public void exportPurchaseRequisition(HttpServletRequest request, HttpServletResponse response){
        try {
            List jasperPrint = accExportReportsServiceDAOobj.exportPurchaseRequisition(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
      public void exportGCBPurchaseRequisition(HttpServletRequest request, HttpServletResponse response){
        try {
            List jasperPrint = accExportReportsServiceDAOobj.exportGCBPurchaseRequisition(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    public void exportDefaultRFQ(HttpServletRequest request, HttpServletResponse response ) {
        try {
            List jasperPrint = accExportOtherReportsServiceDAOobj.exportDefaultRFQ(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public ModelAndView isRuleExistsForRequisition(HttpServletRequest request, HttpServletResponse response) {
        boolean ruleExist = false;
        boolean success = false;
        JSONObject jObj = new JSONObject();
        try {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            double amount = request.getParameter("totalAmount") != null ? Double.parseDouble(request.getParameter("totalAmount").toString()) : 0;
            int approvestatuslevel = request.getParameter("approvestatuslevel") != null? Integer.parseInt(request.getParameter("approvestatuslevel"))+1 : 1;
            String amountInStringFormat = String.valueOf(amount);
            
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyId);
            qdDataMap.put("level", approvestatuslevel);
            qdDataMap.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
            
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                String rule = row[2].toString();
                int appliedUpon = Integer.parseInt(row[5].toString());
                if(appliedUpon == Constants.Specific_Products_Category){
                    /*
                     * Check If Rule is apply on product
                     * category from multiapproverule window
                     */
                    ruleExist = true;
                    break;
                } else{
                    rule = rule.replaceAll("[$$]+", amountInStringFormat);
                    if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon != Constants.Specific_Products_Category && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                        ruleExist = true;
                        break;
                    }
                }
            }
            success = true;
        } catch (SessionExpiredException ex) {
            success = false;
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            success = false;
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put("isRuleExist", ruleExist);
                jObj.put("success", success);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }

    public ModelAndView checkIfBugetLimitExceeding(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        boolean success = false;
        boolean isBudgetExceeding = false;
        try {
            int budgetingType = StringUtil.isNullOrEmpty(request.getParameter("budgetingType")) ? -1 : Integer.parseInt(request.getParameter("budgetingType"));

            isBudgetExceeding = checkIfBugetLimitExceeding(request, budgetingType);
            success = true;
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                jObj.put("isBudgetExceeding", isBudgetExceeding);
                jObj.put("success", success);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }

    public boolean checkIfBugetLimitExceeding(HttpServletRequest request, int budgetingType) {
        boolean isBudgetExceeding = false;
        try {
            boolean isApprove = !StringUtil.isNullOrEmpty(request.getParameter("isApprove")) ? Boolean.parseBoolean(request.getParameter("isApprove")) : false;
            if (budgetingType == Constants.BudgetingTypes.get(Constants.BudgetForDepartment)) {
                if (isApprove) {
                    isBudgetExceeding = checkIfBugetLimitExceedingForDepartmentForApprove(request);
                } else {
                    isBudgetExceeding = checkIfBugetLimitExceedingForDepartment(request);
                }
            } else if (budgetingType == Constants.BudgetingTypes.get(Constants.BudgetForDepartmentAndProduct)) {
                if (isApprove) {
                    isBudgetExceeding = checkIfBugetLimitExceedingForDepartmentAndProductForApprove(request);
                } else {
                    isBudgetExceeding = checkIfBugetLimitExceedingForDepartmentAndProduct(request);
                }
            } else if (budgetingType == Constants.BudgetingTypes.get(Constants.BudgetForDepartmentAndProductCategory)) {
                // future enhancement - P2
            }
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return isBudgetExceeding;
    }

    public Object[] getParametersForCheckingIfBudgetingAmountExceeding(Date requisitionDate, int budgetingfrequencyType) {
        Object[] parameters = new Object[5];
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(requisitionDate);
            int month = calendar.get(Calendar.MONTH); // Get month of the requisition date
            int year = calendar.get(Calendar.YEAR); // Get year of the requisition date

            /*
             * Here, value of variable 'month' lies between 0 and 11 0 indicates
             * month January , 1 indicates month February ...... and 11
             * indicates month December
             */
            String calString="";
            Date calDate=null;

            if (budgetingfrequencyType == Constants.BudgetingFrequencyTypes.get(Constants.MonthlyBudgeting)) { // Frequency Type=0

                parameters[0] = Budgeting.FREQUENCY_TYPE_MONTHLY; // Frequency Type=0
                parameters[1] = Integer.toString(month);
                parameters[2] = Integer.toString(-1);

                calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                calDate = authHandler.getDateOnlyFormat().parse(calString);
                parameters[3] = calDate;

                calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                calDate = authHandler.getDateOnlyFormat().parse(calString);
                parameters[4] = calDate;


            } else if (budgetingfrequencyType == Constants.BudgetingFrequencyTypes.get(Constants.BiMonthlyBudgeting)) { // Frequency Type=1

                parameters[0] = Budgeting.FREQUENCY_TYPE_BIMONTHLY; // Frequency Type = 1

                if (month == Calendar.JANUARY || month == Calendar.FEBRUARY) { // If month is January or February
                    parameters[1] = Integer.toString(0);

                    calendar.set(Calendar.MONTH, Calendar.JANUARY);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                } else if ((month == Calendar.MARCH || month == Calendar.APRIL)) { // If month is March or April
                    parameters[1] = Integer.toString(1);

                    calendar.set(Calendar.MONTH, Calendar.MARCH);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.APRIL);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                } else if ((month == Calendar.MAY || month == Calendar.JUNE)) { // If month is May or June
                    parameters[1] = Integer.toString(2);

                    calendar.set(Calendar.MONTH, Calendar.MAY);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.JUNE);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                } else if ((month == Calendar.JULY || month == Calendar.AUGUST)) { // If month is July or August
                    parameters[1] = Integer.toString(3);

                    calendar.set(Calendar.MONTH, Calendar.JULY);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.AUGUST);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                } else if ((month == Calendar.SEPTEMBER || month == Calendar.OCTOBER)) { // If month is September or October
                    parameters[1] = Integer.toString(4);

                    calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.OCTOBER);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                } else if ((month == Calendar.NOVEMBER || month == Calendar.DECEMBER)) { // If month is November or December
                    parameters[1] = Integer.toString(5);

                    calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                }

                parameters[2] = Integer.toString(-1);

            } else if (budgetingfrequencyType == Constants.BudgetingFrequencyTypes.get(Constants.QuarteryBudgeting)) { // Frequency Type=2

                parameters[0] = Budgeting.FREQUENCY_TYPE_QUARTERLY; // Frequency Type = 2

                if (month >= Calendar.JANUARY && month <= Calendar.MARCH) { // If month is Jan or Feb or March
                    parameters[1] = Integer.toString(0);

                    calendar.set(Calendar.MONTH, Calendar.JANUARY);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.MARCH);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                } else if (month >= Calendar.APRIL && month <= Calendar.JUNE) { // If month is April or May or June
                    parameters[1] = Integer.toString(1);

                    calendar.set(Calendar.MONTH, Calendar.APRIL);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.JUNE);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                } else if (month >= Calendar.JULY && month <= Calendar.SEPTEMBER) { // If month is July or August or September
                    parameters[1] = Integer.toString(2);

                    calendar.set(Calendar.MONTH, Calendar.JULY);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                } else if (month >= Calendar.OCTOBER && month <= Calendar.DECEMBER) { // If month is Oct or Nov or December
                    parameters[1] = Integer.toString(3);

                    calendar.set(Calendar.MONTH, Calendar.OCTOBER);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                }

                parameters[2] = Integer.toString(-1);

            } else if (budgetingfrequencyType == Constants.BudgetingFrequencyTypes.get(Constants.HalfYearlyBudgeting)) { // Frequency Type=3

                parameters[0] = Budgeting.FREQUENCY_TYPE_HALF_YEARLY; // Frequency Type = 3

                if (month >= Calendar.JANUARY && month <= Calendar.JUNE) { // If month is between Jan and June 
                    parameters[1] = Integer.toString(0);

                    calendar.set(Calendar.MONTH, Calendar.JANUARY);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.JUNE);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                } else if (month >= Calendar.JULY && month <= Calendar.DECEMBER) { // If month is between July and december 
                    parameters[1] = Integer.toString(1);

                    calendar.set(Calendar.MONTH, Calendar.JULY);
                    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[3] = calDate;

                    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                    calDate = authHandler.getDateOnlyFormat().parse(calString);
                    parameters[4] = calDate;

                }

                parameters[2] = Integer.toString(-1);

            } else if (budgetingfrequencyType == Constants.BudgetingFrequencyTypes.get(Constants.YearlyBudgeting)) { // Frequency Type=4

                
                parameters[0] = Budgeting.FREQUENCY_TYPE_YEARLY; // Frequency Type = 4
                parameters[1] = Integer.toString(-1); // For yearly budgeting, month will not be considered, so it is taken as -1
                parameters[2] = Integer.toString(year);

                calendar.set(Calendar.MONTH, Calendar.JANUARY);
                calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                calDate = authHandler.getDateOnlyFormat().parse(calString);
                parameters[3] = calDate;

                calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calString = authHandler.getDateOnlyFormat().format(calendar.getTime());
                calDate = authHandler.getDateOnlyFormat().parse(calString);
                parameters[4] = calDate;
            }
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return parameters;
    }
    
    public boolean checkIfBugetLimitExceedingForDepartmentAndProduct(HttpServletRequest request) {
        boolean isBudgetExceeding = false;
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            Date requisitionDate = df.parse(request.getParameter("requisitionDate"));
            String currencyID = request.getParameter("currencyID");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject ecpResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);

            ExtraCompanyPreferences ecp = (ExtraCompanyPreferences) ecpResult.getEntityList().get(0);
            int budgetingfrequencyType = ecp.getBudgetFreqType();
            Object[] parametersArray = getParametersForCheckingIfBudgetingAmountExceeding(requisitionDate, budgetingfrequencyType);
            String frequencyType = (String) parametersArray[0];

            JSONArray preqDetailArr = new JSONArray(request.getParameter("detail"));

            Date startDate = (Date) parametersArray[3];
            Date endDate = (Date) parametersArray[4];
            String frequencyColumn = "";
            String year = "";
            if (frequencyType.equalsIgnoreCase("4")) {
                year = (String) parametersArray[2];
            } else {
                frequencyColumn = (String) parametersArray[1];
            }

            outerloop:
            for (int i = 0; i < preqDetailArr.length(); i++) {
                JSONObject preqDetailJobj = preqDetailArr.getJSONObject(i);
                String productid = preqDetailJobj.getString("productid");
                String lineLevelCustomfield = preqDetailJobj.getString("customfield");

                if (!StringUtil.isNullOrEmpty(lineLevelCustomfield)) {
                    JSONArray lineLevelCustomfieldArr = new JSONArray(lineLevelCustomfield);

                    for (int lineLevelCustCnt = 0; lineLevelCustCnt < lineLevelCustomfieldArr.length(); lineLevelCustCnt++) {
                        JSONObject globleCustObj = lineLevelCustomfieldArr.getJSONObject(lineLevelCustCnt);

                        String fieldid = globleCustObj.getString("filedid");
                        KwlReturnObject fieldParamsResult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), fieldid);
                        FieldParams fieldParams = (FieldParams) fieldParamsResult.getEntityList().get(0);

                        if (fieldParams.getCustomfield() == 0) { // check for dimension
                            String fieldComboDataKey = globleCustObj.getString(fieldParams.getFieldname());
                            String fieldComboDataID = globleCustObj.getString(fieldComboDataKey);

                            KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), fieldComboDataID);
                            FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);

                            if (fieldComboData != null) {
                                double budgetedAmount;
                                double invoiceAmount = 0;
                                double approvedPurchaseRequisitionAmount = 0;

                                HashMap<String, Object> budgetingParams = new HashMap<>();
                                budgetingParams.put("companyID", companyid);
                                budgetingParams.put("dimensionValue", fieldComboData.getId());
                                budgetingParams.put("frequencyType", frequencyType);
                                budgetingParams.put("product", productid);
                                if (!StringUtil.isNullOrEmpty(frequencyColumn)) {
                                    budgetingParams.put("frequencyColumn", frequencyColumn);
                                }

                                if (!StringUtil.isNullOrEmpty(year)) {
                                    budgetingParams.put("year", year);
                                }

                                KwlReturnObject result = accPurchaseOrderobj.getBudgeting(budgetingParams);

                                if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                    Budgeting budgeting = (Budgeting) result.getEntityList().get(0);
                                    budgetedAmount = budgeting.getAmount();

                                    HashMap<String, Object> invoiceAmountParams = new HashMap<>();
                                    invoiceAmountParams.put("companyID", companyid);
                                    invoiceAmountParams.put("startdate", startDate);
                                    invoiceAmountParams.put("enddate", endDate);
                                    invoiceAmountParams.put("productID", productid);
                                    invoiceAmountParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);

                                    HashMap<String, Object> invFieldParamsMap = new HashMap<>();
                                    invFieldParamsMap.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
                                    invFieldParamsMap.put(Constants.filter_values, Arrays.asList(fieldParams.getFieldlabel(), companyid, Constants.Acc_Vendor_Invoice_ModuleId));
                                    KwlReturnObject invFieldParamsIDResult = accAccountDAOobj.getFieldParamsIds(invFieldParamsMap);
                                    if (invFieldParamsIDResult.getEntityList() != null && !invFieldParamsIDResult.getEntityList().isEmpty() && invFieldParamsIDResult.getEntityList().get(0) != null) {
                                        String invFieldParamsID = (String) invFieldParamsIDResult.getEntityList().get(0);
                                        KwlReturnObject invFieldParamsResult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), invFieldParamsID);
                                        FieldParams invFieldParams = (FieldParams) invFieldParamsResult.getEntityList().get(0);

                                        String invFieldComboDataID = fieldManagerDAOobj.getIdsUsingParamsValue(invFieldParams.getId(), fieldComboData.getValue());
                                        KwlReturnObject invFieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), invFieldComboDataID);
                                        FieldComboData invFieldComboData = (FieldComboData) invFieldComboDataResult.getEntityList().get(0);

                                        // for creating invoice search json
                                        JSONObject searchJson = new JSONObject();
                                        JSONArray dimSearchJsonArr = new JSONArray();
                                        JSONObject dimJson = new JSONObject();
                                        dimJson.put("column", invFieldParams.getId());
                                        dimJson.put("refdbname", "Col" + invFieldParams.getColnum());
                                        dimJson.put("xfield", "Col" + invFieldParams.getColnum());
                                        dimJson.put("iscustomcolumn", true);
                                        dimJson.put("iscustomcolumndata", true);
                                        dimJson.put("isfrmpmproduct", false);
                                        dimJson.put("fieldtype", invFieldParams.getFieldtype());
                                        dimJson.put("searchText", invFieldComboData.getId());
                                        dimJson.put("columnheader", URLEncoder.encode(invFieldParams.getFieldlabel()));
                                        dimJson.put("search", invFieldComboData.getId());
                                        dimJson.put("xtype", "select");
                                        dimJson.put("combosearch", invFieldComboData.getValue());
                                        dimJson.put("isinterval", false);
                                        dimJson.put("interval", "");
                                        dimJson.put("isbefore", "");
                                        dimJson.put("isdefaultfield", false);
                                        dimJson.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                                        dimJson.put("isForProductMasterOnly", "");
                                        dimJson.put("transactionSearch", "1");
                                        dimJson.put("includingTax", false);
                                        dimJson.put("includingDiscount", false);
                                        dimSearchJsonArr.put(dimJson);
                                        searchJson.put("root", dimSearchJsonArr);

                                        invoiceAmountParams.put("searchJson", searchJson);

                                        KwlReturnObject invoiceAmountResult = accPurchaseOrderobj.getPuchaseRequisitionInvoiceAmount(invoiceAmountParams);
                                        List<String> invRows = invoiceAmountResult.getEntityList();

                                        for (String rowID : invRows) {
                                            KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), rowID);
                                            GoodsReceiptDetail temp = (GoodsReceiptDetail) rowResult.getEntityList().get(0);

                                            double quantity = temp.getInventory().getQuantity();
                                            double amount = authHandler.round(temp.getRate() * quantity, companyid);
                                            double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                                            double rowTaxPercent = 0;
                                            double rowTaxAmount = 0;
                                            boolean isRowTaxApplicable = false;
                                            if (temp.getTax() != null) {
//                                                KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getJournalEntry().getEntryDate(), temp.getTax().getID());
                                                KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getCreationDate(), temp.getTax().getID());
                                                rowTaxPercent = (Double) perresult.getEntityList().get(0);
                                                isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                                            }

                                            if (temp.isWasRowTaxFieldEditable()) { // After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                                if (isRowTaxApplicable) {
                                                    rowTaxAmount = temp.getRowTaxAmount() + temp.getRowTermTaxAmount();
                                                }
                                            } else { // for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                                rowTaxAmount = (amount - rdisc) * rowTaxPercent / 100;
                                            }

                                            double ramount = amount - rdisc;
                                            ramount += rowTaxAmount;

                                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), ramount, temp.getGoodsReceipt().getCurrency().getCurrencyID(), requisitionDate, 0);
                                            ramount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                            invoiceAmount += ramount;
                                        }
                                    }

                                    HashMap<String, Object> pReqAmountParams = new HashMap<>();
                                    pReqAmountParams.put("companyID", companyid);
                                    pReqAmountParams.put("startdate", startDate);
                                    pReqAmountParams.put("enddate", endDate);
                                    pReqAmountParams.put("df", df);
                                    pReqAmountParams.put("productID", productid);

                                    // for creating search json
                                    JSONObject preqSearchJson = new JSONObject();
                                    JSONArray preqDimSearchJsonArr = new JSONArray();
                                    JSONObject preqDimJson = new JSONObject();
                                    preqDimJson.put("column", fieldParams.getId());
                                    preqDimJson.put("refdbname", "Col" + fieldParams.getColnum());
                                    preqDimJson.put("xfield", "Col" + fieldParams.getColnum());
                                    preqDimJson.put("iscustomcolumn", true);
                                    preqDimJson.put("iscustomcolumndata", true);
                                    preqDimJson.put("isfrmpmproduct", false);
                                    preqDimJson.put("fieldtype", fieldParams.getFieldtype());
                                    preqDimJson.put("searchText", fieldComboData.getId());
                                    preqDimJson.put("columnheader", URLEncoder.encode(fieldParams.getFieldlabel()));
                                    preqDimJson.put("search", fieldComboData.getId());
                                    preqDimJson.put("xtype", "select");
                                    preqDimJson.put("combosearch", fieldComboData.getValue());
                                    preqDimJson.put("isinterval", false);
                                    preqDimJson.put("interval", "");
                                    preqDimJson.put("isbefore", "");
                                    preqDimJson.put("isdefaultfield", false);
                                    preqDimJson.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                                    preqDimJson.put("isForProductMasterOnly", "");
                                    preqDimJson.put("transactionSearch", "1");
                                    preqDimJson.put("includingTax", false);
                                    preqDimJson.put("includingDiscount", false);
                                    preqDimSearchJsonArr.put(preqDimJson);
                                    preqSearchJson.put("root", preqDimSearchJsonArr);

                                    pReqAmountParams.put("searchJson", preqSearchJson);
                                    pReqAmountParams.put(Constants.moduleid, Constants.Acc_Purchase_Requisition_ModuleId);

                                    KwlReturnObject pReqAmountResult = accPurchaseOrderobj.getApprovedPurchaseRequisitionAmountWhoseInvoiceIsNotCreated(pReqAmountParams);
                                    List<String> pReqRows = pReqAmountResult.getEntityList();

                                    for (String rowID : pReqRows) {
                                        KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), rowID);
                                        PurchaseRequisitionDetail row = (PurchaseRequisitionDetail) rowResult.getEntityList().get(0);

                                        double quantity = row.getBaseuomquantity();
                                        double rate = row.getRate();
                                        double tempAmount = quantity * rate;

                                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), tempAmount, row.getPurchaserequisition().getCurrency().getCurrencyID(), requisitionDate, 0);
                                        tempAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                        approvedPurchaseRequisitionAmount += tempAmount;
                                    }

                                    double preqProductTotalAmount = Double.parseDouble(preqDetailJobj.getString("amount"));
                                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), preqProductTotalAmount, currencyID, requisitionDate, 0);
                                    preqProductTotalAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                    // check for budget is exceeding or not
                                    if (budgetedAmount < (invoiceAmount + approvedPurchaseRequisitionAmount + preqProductTotalAmount)) {
                                        isBudgetExceeding = true;
                                        break outerloop;
                                    } else {
                                        isBudgetExceeding = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isBudgetExceeding;
    }

    public boolean checkIfBugetLimitExceedingForDepartmentAndProductForApprove(HttpServletRequest request) {
        boolean isBudgetExceeding = false;
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            Date requisitionDate = df.parse(request.getParameter("requisitionDate"));
            String currencyID = request.getParameter("currencyID");
            String billid = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);

            KwlReturnObject ecpResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences ecp = (ExtraCompanyPreferences) ecpResult.getEntityList().get(0);
            int budgetingfrequencyType = ecp.getBudgetFreqType();
            Object[] parametersArray = getParametersForCheckingIfBudgetingAmountExceeding(requisitionDate, budgetingfrequencyType);
            String frequencyType = (String) parametersArray[0];

            Date startDate = (Date) parametersArray[3];
            Date endDate = (Date) parametersArray[4];
            String frequencyColumn = "";
            String year = "";
            if (frequencyType.equalsIgnoreCase("4")) {
                year = (String) parametersArray[2];
            } else {
                frequencyColumn = (String) parametersArray[1];
            }

            // check for linelevel dim budget
            KwlReturnObject preqResult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), billid);
            PurchaseRequisition purchaseRequisition = (PurchaseRequisition) preqResult.getEntityList().get(0);

            Set<PurchaseRequisitionDetail> rows = purchaseRequisition.getRows();
            outerloop:
            for (PurchaseRequisitionDetail detail : rows) {
                String productid = detail.getProduct().getID();

                HashMap<String, Object> requestParams = new HashMap<>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.customfield, "customcolumn"));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, 0, 1));
                requestParams.put(Constants.moduleid, Constants.Acc_Purchase_Requisition_ModuleId);
                requestParams.put("isActivated", 1); // For Activated Field Params
                KwlReturnObject preqFieldParamsResult = accAccountDAOobj.getFieldParams(requestParams);
                List<FieldParams> fieldParamsList = preqFieldParamsResult.getEntityList();

                for (FieldParams fieldParams : fieldParamsList) {
                    KwlReturnObject preqDetailResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetailCustomData.class.getName(), detail.getID());
                    PurchaseRequisitionDetailCustomData detailCustData = (PurchaseRequisitionDetailCustomData) preqDetailResult.getEntityList().get(0);
                    int colnum = fieldParams.getColnum();
                    Class cl = Class.forName(PurchaseRequisitionDetailCustomData.class.getName());
                    Method getter = cl.getMethod("getCol" + colnum);
                    String colValue = (String) getter.invoke(detailCustData);

                    if (!StringUtil.isNullOrEmpty(colValue)) {
                        KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), colValue);
                        FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);

                        if (fieldComboData != null) {
                            double budgetedAmount;
                            double invoiceAmount = 0;
                            double approvedPurchaseRequisitionAmount = 0;

                            HashMap<String, Object> budgetingParams = new HashMap<>();
                            budgetingParams.put("companyID", companyid);
                            budgetingParams.put("dimensionValue", fieldComboData.getId());
                            budgetingParams.put("frequencyType", frequencyType);
                            budgetingParams.put("product", productid);
                            if (!StringUtil.isNullOrEmpty(frequencyColumn)) {
                                budgetingParams.put("frequencyColumn", frequencyColumn);
                            }

                            if (!StringUtil.isNullOrEmpty(year)) {
                                budgetingParams.put("year", year);
                            }

                            KwlReturnObject result = accPurchaseOrderobj.getBudgeting(budgetingParams);

                            if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                Budgeting budgeting = (Budgeting) result.getEntityList().get(0);
                                budgetedAmount = budgeting.getAmount();

                                HashMap<String, Object> invoiceAmountParams = new HashMap<>();
                                invoiceAmountParams.put("companyID", companyid);
                                invoiceAmountParams.put("startdate", startDate);
                                invoiceAmountParams.put("enddate", endDate);
                                invoiceAmountParams.put("productID", productid);
                                invoiceAmountParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);

                                HashMap<String, Object> invFieldParamsMap = new HashMap<>();
                                invFieldParamsMap.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
                                invFieldParamsMap.put(Constants.filter_values, Arrays.asList(fieldParams.getFieldlabel(), companyid, Constants.Acc_Vendor_Invoice_ModuleId));
                                KwlReturnObject invFieldParamsIDResult = accAccountDAOobj.getFieldParamsIds(invFieldParamsMap);
                                if (invFieldParamsIDResult.getEntityList() != null && !invFieldParamsIDResult.getEntityList().isEmpty() && invFieldParamsIDResult.getEntityList().get(0) != null) {
                                    String invFieldParamsID = (String) invFieldParamsIDResult.getEntityList().get(0);
                                    KwlReturnObject invFieldParamsResult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), invFieldParamsID);
                                    FieldParams invFieldParams = (FieldParams) invFieldParamsResult.getEntityList().get(0);

                                    String invFieldComboDataID = fieldManagerDAOobj.getIdsUsingParamsValue(invFieldParams.getId(), fieldComboData.getValue());
                                    KwlReturnObject invFieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), invFieldComboDataID);
                                    FieldComboData invFieldComboData = (FieldComboData) invFieldComboDataResult.getEntityList().get(0);

                                    // for creating invoice search json
                                    JSONObject searchJson = new JSONObject();
                                    JSONArray dimSearchJsonArr = new JSONArray();
                                    JSONObject dimJson = new JSONObject();
                                    dimJson.put("column", invFieldParams.getId());
                                    dimJson.put("refdbname", "Col" + invFieldParams.getColnum());
                                    dimJson.put("xfield", "Col" + invFieldParams.getColnum());
                                    dimJson.put("iscustomcolumn", true);
                                    dimJson.put("iscustomcolumndata", true);
                                    dimJson.put("isfrmpmproduct", false);
                                    dimJson.put("fieldtype", invFieldParams.getFieldtype());
                                    dimJson.put("searchText", invFieldComboData.getId());
                                    dimJson.put("columnheader", URLEncoder.encode(invFieldParams.getFieldlabel()));
                                    dimJson.put("search", invFieldComboData.getId());
                                    dimJson.put("xtype", "select");
                                    dimJson.put("combosearch", invFieldComboData.getValue());
                                    dimJson.put("isinterval", false);
                                    dimJson.put("interval", "");
                                    dimJson.put("isbefore", "");
                                    dimJson.put("isdefaultfield", false);
                                    dimJson.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                                    dimJson.put("isForProductMasterOnly", "");
                                    dimJson.put("transactionSearch", "1");
                                    dimJson.put("includingTax", false);
                                    dimJson.put("includingDiscount", false);
                                    dimSearchJsonArr.put(dimJson);
                                    searchJson.put("root", dimSearchJsonArr);

                                    invoiceAmountParams.put("searchJson", searchJson);

                                    KwlReturnObject invoiceAmountResult = accPurchaseOrderobj.getPuchaseRequisitionInvoiceAmount(invoiceAmountParams);
                                    List<String> invRows = invoiceAmountResult.getEntityList();

                                    for (String rowID : invRows) {
                                        KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), rowID);
                                        GoodsReceiptDetail temp = (GoodsReceiptDetail) rowResult.getEntityList().get(0);

                                        double quantity = temp.getInventory().getQuantity();
                                        double amount = authHandler.round(temp.getRate() * quantity, companyid);
                                        double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                                        double rowTaxPercent = 0;
                                        double rowTaxAmount = 0;
                                        boolean isRowTaxApplicable = false;
                                        if (temp.getTax() != null) {
//                                            KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getJournalEntry().getEntryDate(), temp.getTax().getID());
                                            KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getCreationDate(), temp.getTax().getID());
                                            rowTaxPercent = (Double) perresult.getEntityList().get(0);
                                            isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                                        }

                                        if (temp.isWasRowTaxFieldEditable()) { // After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                            if (isRowTaxApplicable) {
                                                rowTaxAmount = temp.getRowTaxAmount() + temp.getRowTermTaxAmount();
                                            }
                                        } else { // for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                            rowTaxAmount = (amount - rdisc) * rowTaxPercent / 100;
                                        }

                                        double ramount = amount - rdisc;
                                        ramount += rowTaxAmount;

                                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), ramount, temp.getGoodsReceipt().getCurrency().getCurrencyID(), requisitionDate, 0);
                                        ramount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                        invoiceAmount += ramount;
                                    }
                                }

                                HashMap<String, Object> pReqAmountParams = new HashMap<>();
                                pReqAmountParams.put("companyID", companyid);
                                pReqAmountParams.put("startdate", startDate);
                                pReqAmountParams.put("enddate", endDate);
                                pReqAmountParams.put("df", df);
                                pReqAmountParams.put("productID", productid);

                                // for creating search json
                                JSONObject preqSearchJson = new JSONObject();
                                JSONArray preqDimSearchJsonArr = new JSONArray();
                                JSONObject preqDimJson = new JSONObject();
                                preqDimJson.put("column", fieldParams.getId());
                                preqDimJson.put("refdbname", "Col" + fieldParams.getColnum());
                                preqDimJson.put("xfield", "Col" + fieldParams.getColnum());
                                preqDimJson.put("iscustomcolumn", true);
                                preqDimJson.put("iscustomcolumndata", true);
                                preqDimJson.put("isfrmpmproduct", false);
                                preqDimJson.put("fieldtype", fieldParams.getFieldtype());
                                preqDimJson.put("searchText", fieldComboData.getId());
                                preqDimJson.put("columnheader", URLEncoder.encode(fieldParams.getFieldlabel()));
                                preqDimJson.put("search", fieldComboData.getId());
                                preqDimJson.put("xtype", "select");
                                preqDimJson.put("combosearch", fieldComboData.getValue());
                                preqDimJson.put("isinterval", false);
                                preqDimJson.put("interval", "");
                                preqDimJson.put("isbefore", "");
                                preqDimJson.put("isdefaultfield", false);
                                preqDimJson.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                                preqDimJson.put("isForProductMasterOnly", "");
                                preqDimJson.put("transactionSearch", "1");
                                preqDimJson.put("includingTax", false);
                                preqDimJson.put("includingDiscount", false);
                                preqDimSearchJsonArr.put(preqDimJson);
                                preqSearchJson.put("root", preqDimSearchJsonArr);

                                pReqAmountParams.put("searchJson", preqSearchJson);
                                pReqAmountParams.put(Constants.moduleid, Constants.Acc_Purchase_Requisition_ModuleId);

                                KwlReturnObject pReqAmountResult = accPurchaseOrderobj.getApprovedPurchaseRequisitionAmountWhoseInvoiceIsNotCreated(pReqAmountParams);
                                List<String> pReqRows = pReqAmountResult.getEntityList();

                                for (String rowID : pReqRows) {
                                    KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), rowID);
                                    PurchaseRequisitionDetail row = (PurchaseRequisitionDetail) rowResult.getEntityList().get(0);

                                    double quantity = row.getBaseuomquantity();
                                    double rate = row.getRate();
                                    double tempAmount = quantity * rate;

                                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), tempAmount, row.getPurchaserequisition().getCurrency().getCurrencyID(), requisitionDate, 0);
                                    tempAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                    approvedPurchaseRequisitionAmount += tempAmount;
                                }

                                double preqProductTotalAmount = detail.getBaseuomquantity() * detail.getRate();
                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), preqProductTotalAmount, currencyID, requisitionDate, 0);
                                preqProductTotalAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                // check for budget is exceeding or not
                                if (budgetedAmount < (invoiceAmount + approvedPurchaseRequisitionAmount + preqProductTotalAmount)) {
                                    isBudgetExceeding = true;
                                    break outerloop;
                                } else {
                                    isBudgetExceeding = false;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isBudgetExceeding;
    }

    public boolean checkIfBugetLimitExceedingForDepartment(HttpServletRequest request) {
        boolean isBudgetExceeding = false;
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            Date requisitionDate = df.parse(request.getParameter("requisitionDate"));
            String currencyID = request.getParameter("currencyID");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject ecpResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);

            ExtraCompanyPreferences ecp = (ExtraCompanyPreferences) ecpResult.getEntityList().get(0);
            int budgetingfrequencyType = ecp.getBudgetFreqType();
            Object[] parametersArray = getParametersForCheckingIfBudgetingAmountExceeding(requisitionDate, budgetingfrequencyType);
            String frequencyType = (String) parametersArray[0];

            String globleCustomfield = request.getParameter("globleCustomfield");
            JSONArray preqDetailArr = new JSONArray(request.getParameter("detail"));

            Date startDate = (Date) parametersArray[3];
            Date endDate = (Date) parametersArray[4];
            String frequencyColumn = "";
            String year = "";
            if (frequencyType.equalsIgnoreCase("4")) {
                year = (String) parametersArray[2];
            } else {
                frequencyColumn = (String) parametersArray[1];
            }
            double requisitionTotalAmount = request.getParameter("requisitionTotalAmount") != null ? Double.parseDouble(request.getParameter("requisitionTotalAmount")) : 0;

            if (!StringUtil.isNullOrEmpty(globleCustomfield)) {
                JSONArray globleCustomfieldArray = new JSONArray(globleCustomfield);

                for (int globleCustCnt = 0; globleCustCnt < globleCustomfieldArray.length(); globleCustCnt++) {
                    JSONObject globleCustObj = globleCustomfieldArray.getJSONObject(globleCustCnt);

                    String fieldid = globleCustObj.optString("fieldid", "");
                    KwlReturnObject fieldParamsResult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), fieldid);
                    FieldParams fieldParams = (FieldParams) fieldParamsResult.getEntityList().get(0);

                    if (fieldParams.getCustomfield() == 0) { // check for dimension
                        String fieldComboDataKey = globleCustObj.getString(fieldParams.getFieldname());
                        String fieldComboDataID = globleCustObj.getString(fieldComboDataKey);

                        KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), fieldComboDataID);
                        FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);

                        if (fieldComboData != null) {
                            double budgetedAmount;
                            double invoiceAmount = 0;
                            double approvedPurchaseRequisitionAmount = 0;

                            HashMap<String, Object> budgetingParams = new HashMap<>();
                            budgetingParams.put("companyID", companyid);
                            budgetingParams.put("dimensionValue", fieldComboData.getId());
                            budgetingParams.put("frequencyType", frequencyType);
                            if (!StringUtil.isNullOrEmpty(frequencyColumn)) {
                                budgetingParams.put("frequencyColumn", frequencyColumn);
                            }

                            if (!StringUtil.isNullOrEmpty(year)) {
                                budgetingParams.put("year", year);
                            }

                            KwlReturnObject result = accPurchaseOrderobj.getBudgeting(budgetingParams);

                            if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                Budgeting budgeting = (Budgeting) result.getEntityList().get(0);
                                budgetedAmount = budgeting.getAmount();

                                HashMap<String, Object> invoiceAmountParams = new HashMap<>();
                                invoiceAmountParams.put("companyID", companyid);
                                invoiceAmountParams.put("startdate", startDate);
                                invoiceAmountParams.put("enddate", endDate);
                                invoiceAmountParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);

                                HashMap<String, Object> invFieldParamsMap = new HashMap<>();
                                invFieldParamsMap.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
                                invFieldParamsMap.put(Constants.filter_values, Arrays.asList(fieldParams.getFieldlabel(), companyid, Constants.Acc_Vendor_Invoice_ModuleId));
                                KwlReturnObject invFieldParamsIDResult = accAccountDAOobj.getFieldParamsIds(invFieldParamsMap);
                                if (invFieldParamsIDResult.getEntityList() != null && !invFieldParamsIDResult.getEntityList().isEmpty() && invFieldParamsIDResult.getEntityList().get(0) != null) {
                                    String invFieldParamsID = (String) invFieldParamsIDResult.getEntityList().get(0);
                                    KwlReturnObject invFieldParamsResult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), invFieldParamsID);
                                    FieldParams invFieldParams = (FieldParams) invFieldParamsResult.getEntityList().get(0);

                                    String invFieldComboDataID = fieldManagerDAOobj.getIdsUsingParamsValue(invFieldParams.getId(), fieldComboData.getValue());
                                    KwlReturnObject invFieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), invFieldComboDataID);
                                    FieldComboData invFieldComboData = (FieldComboData) invFieldComboDataResult.getEntityList().get(0);

                                    // for creating invoice search json
                                    JSONObject searchJson = new JSONObject();
                                    JSONArray dimSearchJsonArr = new JSONArray();
                                    JSONObject dimJson = new JSONObject();
                                    dimJson.put("column", invFieldParams.getId());
                                    dimJson.put("refdbname", "Col" + invFieldParams.getColnum());
                                    dimJson.put("xfield", "Col" + invFieldParams.getColnum());
                                    dimJson.put("iscustomcolumn", true);
                                    dimJson.put("iscustomcolumndata", false);
                                    dimJson.put("isfrmpmproduct", false);
                                    dimJson.put("fieldtype", invFieldParams.getFieldtype());
                                    dimJson.put("searchText", invFieldComboData.getId());
                                    dimJson.put("columnheader", URLEncoder.encode(invFieldParams.getFieldlabel()));
                                    dimJson.put("search", invFieldComboData.getId());
                                    dimJson.put("xtype", "select");
                                    dimJson.put("combosearch", invFieldComboData.getValue());
                                    dimJson.put("isinterval", false);
                                    dimJson.put("interval", "");
                                    dimJson.put("isbefore", "");
                                    dimJson.put("isdefaultfield", false);
                                    dimJson.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                                    dimJson.put("isForProductMasterOnly", "");
                                    dimJson.put("transactionSearch", "1");
                                    dimJson.put("includingTax", false);
                                    dimJson.put("includingDiscount", false);
                                    dimSearchJsonArr.put(dimJson);
                                    searchJson.put("root", dimSearchJsonArr);

                                    invoiceAmountParams.put("searchJson", searchJson);

                                    KwlReturnObject invoiceAmountResult = accPurchaseOrderobj.getPuchaseRequisitionInvoiceAmount(invoiceAmountParams);
                                    List<String> invRows = invoiceAmountResult.getEntityList();

                                    for (String rowID : invRows) {
                                        KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), rowID);
                                        GoodsReceiptDetail temp = (GoodsReceiptDetail) rowResult.getEntityList().get(0);

                                        double quantity = temp.getInventory().getQuantity();
                                        double amount = authHandler.round(temp.getRate() * quantity, companyid);
                                        double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                                        double rowTaxPercent = 0;
                                        double rowTaxAmount = 0;
                                        boolean isRowTaxApplicable = false;
                                        if (temp.getTax() != null) {
//                                            KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getJournalEntry().getEntryDate(), temp.getTax().getID());
                                            KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getCreationDate(), temp.getTax().getID());
                                            rowTaxPercent = (Double) perresult.getEntityList().get(0);
                                            isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                                        }

                                        if (temp.isWasRowTaxFieldEditable()) { // After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                            if (isRowTaxApplicable) {
                                                rowTaxAmount = temp.getRowTaxAmount() + temp.getRowTermTaxAmount();
                                            }
                                        } else { // for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                            rowTaxAmount = (amount - rdisc) * rowTaxPercent / 100;
                                        }

                                        double ramount = amount - rdisc;
                                        ramount += rowTaxAmount;

                                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), ramount, temp.getGoodsReceipt().getCurrency().getCurrencyID(), requisitionDate, 0);
                                        ramount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                        invoiceAmount += ramount;
                                    }
                                }

                                HashMap<String, Object> pReqAmountParams = new HashMap<>();
                                pReqAmountParams.put("companyID", companyid);
                                pReqAmountParams.put("startdate", startDate);
                                pReqAmountParams.put("enddate", endDate);
                                pReqAmountParams.put("df", df);

                                // for creating search json
                                JSONObject preqSearchJson = new JSONObject();
                                JSONArray preqDimSearchJsonArr = new JSONArray();
                                JSONObject preqDimJson = new JSONObject();
                                preqDimJson.put("column", fieldParams.getId());
                                preqDimJson.put("refdbname", "Col" + fieldParams.getColnum());
                                preqDimJson.put("xfield", "Col" + fieldParams.getColnum());
                                preqDimJson.put("iscustomcolumn", true);
                                preqDimJson.put("iscustomcolumndata", false);
                                preqDimJson.put("isfrmpmproduct", false);
                                preqDimJson.put("fieldtype", fieldParams.getFieldtype());
                                preqDimJson.put("searchText", fieldComboData.getId());
                                preqDimJson.put("columnheader", URLEncoder.encode(fieldParams.getFieldlabel()));
                                preqDimJson.put("search", fieldComboData.getId());
                                preqDimJson.put("xtype", "select");
                                preqDimJson.put("combosearch", fieldComboData.getValue());
                                preqDimJson.put("isinterval", false);
                                preqDimJson.put("interval", "");
                                preqDimJson.put("isbefore", "");
                                preqDimJson.put("isdefaultfield", false);
                                preqDimJson.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                                preqDimJson.put("isForProductMasterOnly", "");
                                preqDimJson.put("transactionSearch", "1");
                                preqDimJson.put("includingTax", false);
                                preqDimJson.put("includingDiscount", false);
                                preqDimSearchJsonArr.put(preqDimJson);
                                preqSearchJson.put("root", preqDimSearchJsonArr);

                                pReqAmountParams.put("searchJson", preqSearchJson);
                                pReqAmountParams.put(Constants.moduleid, Constants.Acc_Purchase_Requisition_ModuleId);

                                KwlReturnObject pReqAmountResult = accPurchaseOrderobj.getApprovedPurchaseRequisitionAmountWhoseInvoiceIsNotCreated(pReqAmountParams);
                                List<String> pReqRows = pReqAmountResult.getEntityList();

                                for (String rowID : pReqRows) {
                                    KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), rowID);
                                    PurchaseRequisitionDetail row = (PurchaseRequisitionDetail) rowResult.getEntityList().get(0);

                                    double quantity = row.getBaseuomquantity();
                                    double rate = row.getRate();
                                    double tempAmount = quantity * rate;

                                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), tempAmount, row.getPurchaserequisition().getCurrency().getCurrencyID(), requisitionDate, 0);
                                    tempAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                    approvedPurchaseRequisitionAmount += tempAmount;
                                }

                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), requisitionTotalAmount, currencyID, requisitionDate, 0);
                                requisitionTotalAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                // check for budget is exceeding or not
                                if (budgetedAmount < (invoiceAmount + approvedPurchaseRequisitionAmount + requisitionTotalAmount)) {
                                    isBudgetExceeding = true;
                                } else {
                                    isBudgetExceeding = false;
                                }
                            }
                        }
                    }
                }
            }

            if (!isBudgetExceeding) {
                outerloop:
                for (int i = 0; i < preqDetailArr.length(); i++) {
                    JSONObject preqDetailJobj = preqDetailArr.getJSONObject(i);
                    String productid = preqDetailJobj.getString("productid");
                    String lineLevelCustomfield = preqDetailJobj.getString("customfield");

                    if (!StringUtil.isNullOrEmpty(lineLevelCustomfield)) {
                        JSONArray lineLevelCustomfieldArr = new JSONArray(lineLevelCustomfield);

                        for (int lineLevelCustCnt = 0; lineLevelCustCnt < lineLevelCustomfieldArr.length(); lineLevelCustCnt++) {
                            JSONObject globleCustObj = lineLevelCustomfieldArr.getJSONObject(lineLevelCustCnt);

                            String fieldid = globleCustObj.optString("filedid", "");
                            KwlReturnObject fieldParamsResult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), fieldid);
                            FieldParams fieldParams = (FieldParams) fieldParamsResult.getEntityList().get(0);

                            if (fieldParams.getCustomfield() == 0) { // check for dimension
                                String fieldComboDataKey = globleCustObj.getString(fieldParams.getFieldname());
                                String fieldComboDataID = globleCustObj.getString(fieldComboDataKey);

                                KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), fieldComboDataID);
                                FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);

                                if (fieldComboData != null) {
                                    double budgetedAmount;
                                    double invoiceAmount = 0;
                                    double approvedPurchaseRequisitionAmount = 0;

                                    HashMap<String, Object> budgetingParams = new HashMap<>();
                                    budgetingParams.put("companyID", companyid);
                                    budgetingParams.put("dimensionValue", fieldComboData.getId());
                                    budgetingParams.put("frequencyType", frequencyType);
                                    if (!StringUtil.isNullOrEmpty(frequencyColumn)) {
                                        budgetingParams.put("frequencyColumn", frequencyColumn);
                                    }

                                    if (!StringUtil.isNullOrEmpty(year)) {
                                        budgetingParams.put("year", year);
                                    }

                                    KwlReturnObject result = accPurchaseOrderobj.getBudgeting(budgetingParams);

                                    if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                        Budgeting budgeting = (Budgeting) result.getEntityList().get(0);
                                        budgetedAmount = budgeting.getAmount();

                                        HashMap<String, Object> invoiceAmountParams = new HashMap<>();
                                        invoiceAmountParams.put("companyID", companyid);
                                        invoiceAmountParams.put("startdate", startDate);
                                        invoiceAmountParams.put("enddate", endDate);
                                        invoiceAmountParams.put("productID", productid);
                                        invoiceAmountParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);

                                        HashMap<String, Object> invFieldParamsMap = new HashMap<>();
                                        invFieldParamsMap.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
                                        invFieldParamsMap.put(Constants.filter_values, Arrays.asList(fieldParams.getFieldlabel(), companyid, Constants.Acc_Vendor_Invoice_ModuleId));
                                        KwlReturnObject invFieldParamsIDResult = accAccountDAOobj.getFieldParamsIds(invFieldParamsMap);
                                        if (invFieldParamsIDResult.getEntityList() != null && !invFieldParamsIDResult.getEntityList().isEmpty() && invFieldParamsIDResult.getEntityList().get(0) != null) {
                                            String invFieldParamsID = (String) invFieldParamsIDResult.getEntityList().get(0);
                                            KwlReturnObject invFieldParamsResult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), invFieldParamsID);
                                            FieldParams invFieldParams = (FieldParams) invFieldParamsResult.getEntityList().get(0);

                                            String invFieldComboDataID = fieldManagerDAOobj.getIdsUsingParamsValue(invFieldParams.getId(), fieldComboData.getValue());
                                            KwlReturnObject invFieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), invFieldComboDataID);
                                            FieldComboData invFieldComboData = (FieldComboData) invFieldComboDataResult.getEntityList().get(0);

                                            // for creating invoice search json
                                            JSONObject searchJson = new JSONObject();
                                            JSONArray dimSearchJsonArr = new JSONArray();
                                            JSONObject dimJson = new JSONObject();
                                            dimJson.put("column", invFieldParams.getId());
                                            dimJson.put("refdbname", "Col" + invFieldParams.getColnum());
                                            dimJson.put("xfield", "Col" + invFieldParams.getColnum());
                                            dimJson.put("iscustomcolumn", true);
                                            dimJson.put("iscustomcolumndata", true);
                                            dimJson.put("isfrmpmproduct", false);
                                            dimJson.put("fieldtype", invFieldParams.getFieldtype());
                                            dimJson.put("searchText", invFieldComboData.getId());
                                            dimJson.put("columnheader", URLEncoder.encode(invFieldParams.getFieldlabel()));
                                            dimJson.put("search", invFieldComboData.getId());
                                            dimJson.put("xtype", "select");
                                            dimJson.put("combosearch", invFieldComboData.getValue());
                                            dimJson.put("isinterval", false);
                                            dimJson.put("interval", "");
                                            dimJson.put("isbefore", "");
                                            dimJson.put("isdefaultfield", false);
                                            dimJson.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                                            dimJson.put("isForProductMasterOnly", "");
                                            dimJson.put("transactionSearch", "1");
                                            dimJson.put("includingTax", false);
                                            dimJson.put("includingDiscount", false);
                                            dimSearchJsonArr.put(dimJson);
                                            searchJson.put("root", dimSearchJsonArr);

                                            invoiceAmountParams.put("searchJson", searchJson);

                                            KwlReturnObject invoiceAmountResult = accPurchaseOrderobj.getPuchaseRequisitionInvoiceAmount(invoiceAmountParams);
                                            List<String> invRows = invoiceAmountResult.getEntityList();

                                            for (String rowID : invRows) {
                                                KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), rowID);
                                                GoodsReceiptDetail temp = (GoodsReceiptDetail) rowResult.getEntityList().get(0);

                                                double quantity = temp.getInventory().getQuantity();
                                                double amount = authHandler.round(temp.getRate() * quantity, companyid);
                                                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                                                double rowTaxPercent = 0;
                                                double rowTaxAmount = 0;
                                                boolean isRowTaxApplicable = false;
                                                if (temp.getTax() != null) {
//                                                    KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getJournalEntry().getEntryDate(), temp.getTax().getID());
                                                    KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getCreationDate(), temp.getTax().getID());
                                                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                                                    isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                                                }

                                                if (temp.isWasRowTaxFieldEditable()) { // After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                                    if (isRowTaxApplicable) {
                                                        rowTaxAmount = temp.getRowTaxAmount() + temp.getRowTermTaxAmount();
                                                    }
                                                } else { // for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                                    rowTaxAmount = (amount - rdisc) * rowTaxPercent / 100;
                                                }

                                                double ramount = amount - rdisc;
                                                ramount += rowTaxAmount;

                                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), ramount, temp.getGoodsReceipt().getCurrency().getCurrencyID(), requisitionDate, 0);
                                                ramount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                                invoiceAmount += ramount;
                                            }
                                        }

                                        HashMap<String, Object> pReqAmountParams = new HashMap<>();
                                        pReqAmountParams.put("companyID", companyid);
                                        pReqAmountParams.put("startdate", startDate);
                                        pReqAmountParams.put("enddate", endDate);
                                        pReqAmountParams.put("df", df);
                                        pReqAmountParams.put("productID", productid);

                                        // for creating search json
                                        JSONObject preqSearchJson = new JSONObject();
                                        JSONArray preqDimSearchJsonArr = new JSONArray();
                                        JSONObject preqDimJson = new JSONObject();
                                        preqDimJson.put("column", fieldParams.getId());
                                        preqDimJson.put("refdbname", "Col" + fieldParams.getColnum());
                                        preqDimJson.put("xfield", "Col" + fieldParams.getColnum());
                                        preqDimJson.put("iscustomcolumn", true);
                                        preqDimJson.put("iscustomcolumndata", true);
                                        preqDimJson.put("isfrmpmproduct", false);
                                        preqDimJson.put("fieldtype", fieldParams.getFieldtype());
                                        preqDimJson.put("searchText", fieldComboData.getId());
                                        preqDimJson.put("columnheader", URLEncoder.encode(fieldParams.getFieldlabel()));
                                        preqDimJson.put("search", fieldComboData.getId());
                                        preqDimJson.put("xtype", "select");
                                        preqDimJson.put("combosearch", fieldComboData.getValue());
                                        preqDimJson.put("isinterval", false);
                                        preqDimJson.put("interval", "");
                                        preqDimJson.put("isbefore", "");
                                        preqDimJson.put("isdefaultfield", false);
                                        preqDimJson.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                                        preqDimJson.put("isForProductMasterOnly", "");
                                        preqDimJson.put("transactionSearch", "1");
                                        preqDimJson.put("includingTax", false);
                                        preqDimJson.put("includingDiscount", false);
                                        preqDimSearchJsonArr.put(preqDimJson);
                                        preqSearchJson.put("root", preqDimSearchJsonArr);

                                        pReqAmountParams.put("searchJson", preqSearchJson);
                                        pReqAmountParams.put(Constants.moduleid, Constants.Acc_Purchase_Requisition_ModuleId);

                                        KwlReturnObject pReqAmountResult = accPurchaseOrderobj.getApprovedPurchaseRequisitionAmountWhoseInvoiceIsNotCreated(pReqAmountParams);
                                        List<String> pReqRows = pReqAmountResult.getEntityList();

                                        for (String rowID : pReqRows) {
                                            KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), rowID);
                                            PurchaseRequisitionDetail row = (PurchaseRequisitionDetail) rowResult.getEntityList().get(0);

                                            double quantity = row.getBaseuomquantity();
                                            double rate = row.getRate();
                                            double tempAmount = quantity * rate;

                                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), tempAmount, row.getPurchaserequisition().getCurrency().getCurrencyID(), requisitionDate, 0);
                                            tempAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                            approvedPurchaseRequisitionAmount += tempAmount;
                                        }

                                        double preqProductTotalAmount = Double.parseDouble(preqDetailJobj.getString("amount"));
                                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), preqProductTotalAmount, currencyID, requisitionDate, 0);
                                        preqProductTotalAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                        // check for budget is exceeding or not
                                        if (budgetedAmount < (invoiceAmount + approvedPurchaseRequisitionAmount + preqProductTotalAmount)) {
                                            isBudgetExceeding = true;
                                            break outerloop;
                                        } else {
                                            isBudgetExceeding = false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isBudgetExceeding;
    }

    public boolean checkIfBugetLimitExceedingForDepartmentForApprove(HttpServletRequest request) {
        boolean isBudgetExceeding = false;
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            Date requisitionDate = df.parse(request.getParameter("requisitionDate"));
            String currencyID = request.getParameter("currencyID");
            String billid = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);

            KwlReturnObject ecpResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences ecp = (ExtraCompanyPreferences) ecpResult.getEntityList().get(0);
            int budgetingfrequencyType = ecp.getBudgetFreqType();
            Object[] parametersArray = getParametersForCheckingIfBudgetingAmountExceeding(requisitionDate, budgetingfrequencyType);
            String frequencyType = (String) parametersArray[0];

            Date startDate = (Date) parametersArray[3];
            Date endDate = (Date) parametersArray[4];
            String frequencyColumn = "";
            String year = "";
            if (frequencyType.equalsIgnoreCase("4")) {
                year = (String) parametersArray[2];
            } else {
                frequencyColumn = (String) parametersArray[1];
            }
            double requisitionTotalAmount = request.getParameter("requisitionTotalAmount") != null ? Double.parseDouble(request.getParameter("requisitionTotalAmount")) : 0;

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.customfield, "customcolumn"));
            requestParams.put(Constants.filter_values, Arrays.asList(companyid, 0, 0));
            requestParams.put(Constants.moduleid, Constants.Acc_Purchase_Requisition_ModuleId);
            requestParams.put("isActivated", 1); // For Activated Field Params
            KwlReturnObject preqFieldParamsResult = accAccountDAOobj.getFieldParams(requestParams);
            List<FieldParams> fieldParamsList = preqFieldParamsResult.getEntityList();

            for (FieldParams fieldParams : fieldParamsList) {
                KwlReturnObject preqResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionCustomData.class.getName(), billid);
                PurchaseRequisitionCustomData preqcustData = (PurchaseRequisitionCustomData) preqResult.getEntityList().get(0);
                int colnum = fieldParams.getColnum();
                Class cl = Class.forName(PurchaseRequisitionCustomData.class.getName());
                Method getter = cl.getMethod("getCol" + colnum);
                String colValue = (String) getter.invoke(preqcustData);

                if (!StringUtil.isNullOrEmpty(colValue)) {
                    KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), colValue);
                    FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);

                    if (fieldComboData != null) {
                        double budgetedAmount;
                        double invoiceAmount = 0;
                        double approvedPurchaseRequisitionAmount = 0;

                        HashMap<String, Object> budgetingParams = new HashMap<>();
                        budgetingParams.put("companyID", companyid);
                        budgetingParams.put("dimensionValue", fieldComboData.getId());
                        budgetingParams.put("frequencyType", frequencyType);
                        if (!StringUtil.isNullOrEmpty(frequencyColumn)) {
                            budgetingParams.put("frequencyColumn", frequencyColumn);
                        }

                        if (!StringUtil.isNullOrEmpty(year)) {
                            budgetingParams.put("year", year);
                        }

                        KwlReturnObject result = accPurchaseOrderobj.getBudgeting(budgetingParams);

                        if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            Budgeting budgeting = (Budgeting) result.getEntityList().get(0);
                            budgetedAmount = budgeting.getAmount();

                            HashMap<String, Object> invoiceAmountParams = new HashMap<>();
                            invoiceAmountParams.put("companyID", companyid);
                            invoiceAmountParams.put("startdate", startDate);
                            invoiceAmountParams.put("enddate", endDate);
                            invoiceAmountParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);

                            HashMap<String, Object> invFieldParamsMap = new HashMap<>();
                            invFieldParamsMap.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
                            invFieldParamsMap.put(Constants.filter_values, Arrays.asList(fieldParams.getFieldlabel(), companyid, Constants.Acc_Vendor_Invoice_ModuleId));
                            KwlReturnObject invFieldParamsIDResult = accAccountDAOobj.getFieldParamsIds(invFieldParamsMap);
                            if (invFieldParamsIDResult.getEntityList() != null && !invFieldParamsIDResult.getEntityList().isEmpty() && invFieldParamsIDResult.getEntityList().get(0) != null) {
                                String invFieldParamsID = (String) invFieldParamsIDResult.getEntityList().get(0);
                                KwlReturnObject invFieldParamsResult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), invFieldParamsID);
                                FieldParams invFieldParams = (FieldParams) invFieldParamsResult.getEntityList().get(0);

                                String invFieldComboDataID = fieldManagerDAOobj.getIdsUsingParamsValue(invFieldParams.getId(), fieldComboData.getValue());
                                KwlReturnObject invFieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), invFieldComboDataID);
                                FieldComboData invFieldComboData = (FieldComboData) invFieldComboDataResult.getEntityList().get(0);

                                // for creating invoice search json
                                JSONObject searchJson = new JSONObject();
                                JSONArray dimSearchJsonArr = new JSONArray();
                                JSONObject dimJson = new JSONObject();
                                dimJson.put("column", invFieldParams.getId());
                                dimJson.put("refdbname", "Col" + invFieldParams.getColnum());
                                dimJson.put("xfield", "Col" + invFieldParams.getColnum());
                                dimJson.put("iscustomcolumn", true);
                                dimJson.put("iscustomcolumndata", false);
                                dimJson.put("isfrmpmproduct", false);
                                dimJson.put("fieldtype", invFieldParams.getFieldtype());
                                dimJson.put("searchText", invFieldComboData.getId());
                                dimJson.put("columnheader", URLEncoder.encode(invFieldParams.getFieldlabel()));
                                dimJson.put("search", invFieldComboData.getId());
                                dimJson.put("xtype", "select");
                                dimJson.put("combosearch", invFieldComboData.getValue());
                                dimJson.put("isinterval", false);
                                dimJson.put("interval", "");
                                dimJson.put("isbefore", "");
                                dimJson.put("isdefaultfield", false);
                                dimJson.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                                dimJson.put("isForProductMasterOnly", "");
                                dimJson.put("transactionSearch", "1");
                                dimJson.put("includingTax", false);
                                dimJson.put("includingDiscount", false);
                                dimSearchJsonArr.put(dimJson);
                                searchJson.put("root", dimSearchJsonArr);

                                invoiceAmountParams.put("searchJson", searchJson);

                                KwlReturnObject invoiceAmountResult = accPurchaseOrderobj.getPuchaseRequisitionInvoiceAmount(invoiceAmountParams);
                                List<String> invRows = invoiceAmountResult.getEntityList();

                                for (String rowID : invRows) {
                                    KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), rowID);
                                    GoodsReceiptDetail temp = (GoodsReceiptDetail) rowResult.getEntityList().get(0);

                                    double quantity = temp.getInventory().getQuantity();
                                    double amount = authHandler.round(temp.getRate() * quantity, companyid);
                                    double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                                    double rowTaxPercent = 0;
                                    double rowTaxAmount = 0;
                                    boolean isRowTaxApplicable = false;
                                    if (temp.getTax() != null) {
//                                        KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getJournalEntry().getEntryDate(), temp.getTax().getID());
                                        KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getCreationDate(), temp.getTax().getID());
                                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                                        isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                                    }

                                    if (temp.isWasRowTaxFieldEditable()) { // After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                        if (isRowTaxApplicable) {
                                            rowTaxAmount = temp.getRowTaxAmount() + temp.getRowTermTaxAmount();
                                        }
                                    } else { // for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                        rowTaxAmount = (amount - rdisc) * rowTaxPercent / 100;
                                    }

                                    double ramount = amount - rdisc;
                                    ramount += rowTaxAmount;

                                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), ramount, temp.getGoodsReceipt().getCurrency().getCurrencyID(), requisitionDate, 0);
                                    ramount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                    invoiceAmount += ramount;
                                }
                            }

                            HashMap<String, Object> pReqAmountParams = new HashMap<>();
                            pReqAmountParams.put("companyID", companyid);
                            pReqAmountParams.put("startdate", startDate);
                            pReqAmountParams.put("enddate", endDate);
                            pReqAmountParams.put("df", df);

                            // for creating search json
                            JSONObject preqSearchJson = new JSONObject();
                            JSONArray preqDimSearchJsonArr = new JSONArray();
                            JSONObject preqDimJson = new JSONObject();
                            preqDimJson.put("column", fieldParams.getId());
                            preqDimJson.put("refdbname", "Col" + fieldParams.getColnum());
                            preqDimJson.put("xfield", "Col" + fieldParams.getColnum());
                            preqDimJson.put("iscustomcolumn", true);
                            preqDimJson.put("iscustomcolumndata", false);
                            preqDimJson.put("isfrmpmproduct", false);
                            preqDimJson.put("fieldtype", fieldParams.getFieldtype());
                            preqDimJson.put("searchText", fieldComboData.getId());
                            preqDimJson.put("columnheader", URLEncoder.encode(fieldParams.getFieldlabel()));
                            preqDimJson.put("search", fieldComboData.getId());
                            preqDimJson.put("xtype", "select");
                            preqDimJson.put("combosearch", fieldComboData.getValue());
                            preqDimJson.put("isinterval", false);
                            preqDimJson.put("interval", "");
                            preqDimJson.put("isbefore", "");
                            preqDimJson.put("isdefaultfield", false);
                            preqDimJson.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                            preqDimJson.put("isForProductMasterOnly", "");
                            preqDimJson.put("transactionSearch", "1");
                            preqDimJson.put("includingTax", false);
                            preqDimJson.put("includingDiscount", false);
                            preqDimSearchJsonArr.put(preqDimJson);
                            preqSearchJson.put("root", preqDimSearchJsonArr);

                            pReqAmountParams.put("searchJson", preqSearchJson);
                            pReqAmountParams.put(Constants.moduleid, Constants.Acc_Purchase_Requisition_ModuleId);

                            KwlReturnObject pReqAmountResult = accPurchaseOrderobj.getApprovedPurchaseRequisitionAmountWhoseInvoiceIsNotCreated(pReqAmountParams);
                            List<String> pReqRows = pReqAmountResult.getEntityList();

                            for (String rowID : pReqRows) {
                                KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), rowID);
                                PurchaseRequisitionDetail row = (PurchaseRequisitionDetail) rowResult.getEntityList().get(0);

                                double quantity = row.getBaseuomquantity();
                                double rate = row.getRate();
                                double tempAmount = quantity * rate;

                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), tempAmount, row.getPurchaserequisition().getCurrency().getCurrencyID(), requisitionDate, 0);
                                tempAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                approvedPurchaseRequisitionAmount += tempAmount;
                            }

                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), requisitionTotalAmount, currencyID, requisitionDate, 0);
                            requisitionTotalAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                            // check for budget is exceeding or not
                            if (budgetedAmount < (invoiceAmount + approvedPurchaseRequisitionAmount + requisitionTotalAmount)) {
                                isBudgetExceeding = true;
                            } else {
                                isBudgetExceeding = false;
                            }
                        }
                    }
                }
            }

            // check for linelevel dim budget
            if (!isBudgetExceeding) {
                KwlReturnObject preqResult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), billid);
                PurchaseRequisition purchaseRequisition = (PurchaseRequisition) preqResult.getEntityList().get(0);

                Set<PurchaseRequisitionDetail> rows = purchaseRequisition.getRows();
                outerloop:
                for (PurchaseRequisitionDetail detail : rows) {
                    String productid = detail.getProduct().getID();

                    requestParams.clear();
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.customfield, "customcolumn"));
                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, 0, 1));
                    requestParams.put(Constants.moduleid, Constants.Acc_Purchase_Requisition_ModuleId);
                    requestParams.put("isActivated", 1); // For Activated Field Params
                    preqFieldParamsResult = accAccountDAOobj.getFieldParams(requestParams);
                    fieldParamsList = preqFieldParamsResult.getEntityList();

                    for (FieldParams fieldParams : fieldParamsList) {
                        KwlReturnObject preqDetailResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetailCustomData.class.getName(), detail.getID());
                        PurchaseRequisitionDetailCustomData detailCustData = (PurchaseRequisitionDetailCustomData) preqDetailResult.getEntityList().get(0);
                        int colnum = fieldParams.getColnum();
                        Class cl = Class.forName(PurchaseRequisitionDetailCustomData.class.getName());
                        Method getter = cl.getMethod("getCol" + colnum);
                        String colValue = (String) getter.invoke(detailCustData);

                        if (!StringUtil.isNullOrEmpty(colValue)) {
                            KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), colValue);
                            FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);

                            if (fieldComboData != null) {
                                double budgetedAmount;
                                double invoiceAmount = 0;
                                double approvedPurchaseRequisitionAmount = 0;

                                HashMap<String, Object> budgetingParams = new HashMap<>();
                                budgetingParams.put("companyID", companyid);
                                budgetingParams.put("dimensionValue", fieldComboData.getId());
                                budgetingParams.put("frequencyType", frequencyType);
                                if (!StringUtil.isNullOrEmpty(frequencyColumn)) {
                                    budgetingParams.put("frequencyColumn", frequencyColumn);
                                }

                                if (!StringUtil.isNullOrEmpty(year)) {
                                    budgetingParams.put("year", year);
                                }

                                KwlReturnObject result = accPurchaseOrderobj.getBudgeting(budgetingParams);

                                if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                    Budgeting budgeting = (Budgeting) result.getEntityList().get(0);
                                    budgetedAmount = budgeting.getAmount();

                                    HashMap<String, Object> invoiceAmountParams = new HashMap<>();
                                    invoiceAmountParams.put("companyID", companyid);
                                    invoiceAmountParams.put("startdate", startDate);
                                    invoiceAmountParams.put("enddate", endDate);
                                    invoiceAmountParams.put("productID", productid);
                                    invoiceAmountParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);

                                    HashMap<String, Object> invFieldParamsMap = new HashMap<>();
                                    invFieldParamsMap.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
                                    invFieldParamsMap.put(Constants.filter_values, Arrays.asList(fieldParams.getFieldlabel(), companyid, Constants.Acc_Vendor_Invoice_ModuleId));
                                    KwlReturnObject invFieldParamsIDResult = accAccountDAOobj.getFieldParamsIds(invFieldParamsMap);
                                    if (invFieldParamsIDResult.getEntityList() != null && !invFieldParamsIDResult.getEntityList().isEmpty() && invFieldParamsIDResult.getEntityList().get(0) != null) {
                                        String invFieldParamsID = (String) invFieldParamsIDResult.getEntityList().get(0);
                                        KwlReturnObject invFieldParamsResult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), invFieldParamsID);
                                        FieldParams invFieldParams = (FieldParams) invFieldParamsResult.getEntityList().get(0);

                                        String invFieldComboDataID = fieldManagerDAOobj.getIdsUsingParamsValue(invFieldParams.getId(), fieldComboData.getValue());
                                        KwlReturnObject invFieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), invFieldComboDataID);
                                        FieldComboData invFieldComboData = (FieldComboData) invFieldComboDataResult.getEntityList().get(0);

                                        // for creating invoice search json
                                        JSONObject searchJson = new JSONObject();
                                        JSONArray dimSearchJsonArr = new JSONArray();
                                        JSONObject dimJson = new JSONObject();
                                        dimJson.put("column", invFieldParams.getId());
                                        dimJson.put("refdbname", "Col" + invFieldParams.getColnum());
                                        dimJson.put("xfield", "Col" + invFieldParams.getColnum());
                                        dimJson.put("iscustomcolumn", true);
                                        dimJson.put("iscustomcolumndata", true);
                                        dimJson.put("isfrmpmproduct", false);
                                        dimJson.put("fieldtype", invFieldParams.getFieldtype());
                                        dimJson.put("searchText", invFieldComboData.getId());
                                        dimJson.put("columnheader", URLEncoder.encode(invFieldParams.getFieldlabel()));
                                        dimJson.put("search", invFieldComboData.getId());
                                        dimJson.put("xtype", "select");
                                        dimJson.put("combosearch", invFieldComboData.getValue());
                                        dimJson.put("isinterval", false);
                                        dimJson.put("interval", "");
                                        dimJson.put("isbefore", "");
                                        dimJson.put("isdefaultfield", false);
                                        dimJson.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                                        dimJson.put("isForProductMasterOnly", "");
                                        dimJson.put("transactionSearch", "1");
                                        dimJson.put("includingTax", false);
                                        dimJson.put("includingDiscount", false);
                                        dimSearchJsonArr.put(dimJson);
                                        searchJson.put("root", dimSearchJsonArr);

                                        invoiceAmountParams.put("searchJson", searchJson);

                                        KwlReturnObject invoiceAmountResult = accPurchaseOrderobj.getPuchaseRequisitionInvoiceAmount(invoiceAmountParams);
                                        List<String> invRows = invoiceAmountResult.getEntityList();

                                        for (String rowID : invRows) {
                                            KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), rowID);
                                            GoodsReceiptDetail temp = (GoodsReceiptDetail) rowResult.getEntityList().get(0);

                                            double quantity = temp.getInventory().getQuantity();
                                            double amount = authHandler.round(temp.getRate() * quantity, companyid);
                                            double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                                            double rowTaxPercent = 0;
                                            double rowTaxAmount = 0;
                                            boolean isRowTaxApplicable = false;
                                            if (temp.getTax() != null) {
//                                                KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getJournalEntry().getEntryDate(), temp.getTax().getID());
                                                KwlReturnObject perresult = accTaxObj.getTaxPercent(temp.getCompany().getCompanyID(), temp.getGoodsReceipt().getCreationDate(), temp.getTax().getID());
                                                rowTaxPercent = (Double) perresult.getEntityList().get(0);
                                                isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                                            }

                                            if (temp.isWasRowTaxFieldEditable()) { // After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                                if (isRowTaxApplicable) {
                                                    rowTaxAmount = temp.getRowTaxAmount() + temp.getRowTermTaxAmount();
                                                }
                                            } else { // for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                                rowTaxAmount = (amount - rdisc) * rowTaxPercent / 100;
                                            }

                                            double ramount = amount - rdisc;
                                            ramount += rowTaxAmount;

                                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), ramount, temp.getGoodsReceipt().getCurrency().getCurrencyID(), requisitionDate, 0);
                                            ramount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                            invoiceAmount += ramount;
                                        }
                                    }

                                    HashMap<String, Object> pReqAmountParams = new HashMap<>();
                                    pReqAmountParams.put("companyID", companyid);
                                    pReqAmountParams.put("startdate", startDate);
                                    pReqAmountParams.put("enddate", endDate);
                                    pReqAmountParams.put("df", df);
                                    pReqAmountParams.put("productID", productid);

                                    // for creating search json
                                    JSONObject preqSearchJson = new JSONObject();
                                    JSONArray preqDimSearchJsonArr = new JSONArray();
                                    JSONObject preqDimJson = new JSONObject();
                                    preqDimJson.put("column", fieldParams.getId());
                                    preqDimJson.put("refdbname", "Col" + fieldParams.getColnum());
                                    preqDimJson.put("xfield", "Col" + fieldParams.getColnum());
                                    preqDimJson.put("iscustomcolumn", true);
                                    preqDimJson.put("iscustomcolumndata", true);
                                    preqDimJson.put("isfrmpmproduct", false);
                                    preqDimJson.put("fieldtype", fieldParams.getFieldtype());
                                    preqDimJson.put("searchText", fieldComboData.getId());
                                    preqDimJson.put("columnheader", URLEncoder.encode(fieldParams.getFieldlabel()));
                                    preqDimJson.put("search", fieldComboData.getId());
                                    preqDimJson.put("xtype", "select");
                                    preqDimJson.put("combosearch", fieldComboData.getValue());
                                    preqDimJson.put("isinterval", false);
                                    preqDimJson.put("interval", "");
                                    preqDimJson.put("isbefore", "");
                                    preqDimJson.put("isdefaultfield", false);
                                    preqDimJson.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                                    preqDimJson.put("isForProductMasterOnly", "");
                                    preqDimJson.put("transactionSearch", "1");
                                    preqDimJson.put("includingTax", false);
                                    preqDimJson.put("includingDiscount", false);
                                    preqDimSearchJsonArr.put(preqDimJson);
                                    preqSearchJson.put("root", preqDimSearchJsonArr);

                                    pReqAmountParams.put("searchJson", preqSearchJson);
                                    pReqAmountParams.put(Constants.moduleid, Constants.Acc_Purchase_Requisition_ModuleId);

                                    KwlReturnObject pReqAmountResult = accPurchaseOrderobj.getApprovedPurchaseRequisitionAmountWhoseInvoiceIsNotCreated(pReqAmountParams);
                                    List<String> pReqRows = pReqAmountResult.getEntityList();

                                    for (String rowID : pReqRows) {
                                        KwlReturnObject rowResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), rowID);
                                        PurchaseRequisitionDetail row = (PurchaseRequisitionDetail) rowResult.getEntityList().get(0);

                                        double quantity = row.getBaseuomquantity();
                                        double rate = row.getRate();
                                        double tempAmount = quantity * rate;

                                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), tempAmount, row.getPurchaserequisition().getCurrency().getCurrencyID(), requisitionDate, 0);
                                        tempAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                        approvedPurchaseRequisitionAmount += tempAmount;
                                    }

                                    double preqProductTotalAmount = detail.getBaseuomquantity() * detail.getRate();
                                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(AccountingManager.getGlobalParams(request), preqProductTotalAmount, currencyID, requisitionDate, 0);
                                    preqProductTotalAmount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                                    // check for budget is exceeding or not
                                    if (budgetedAmount < (invoiceAmount + approvedPurchaseRequisitionAmount + preqProductTotalAmount)) {
                                        isBudgetExceeding = true;
                                        break outerloop;
                                    } else {
                                        isBudgetExceeding = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isBudgetExceeding;
    }
    
    public ModelAndView attachApprovalDocuments(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        Boolean success = false;
        String docID = "";
        JSONObject jobj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            docID = uploadApprovalDoc(request);
            jobj.put("docID", docID);
            success = true;
            msg = messageSource.getMessage("acc.invoiceList.bt.fileUploadedSuccess", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            txnManager.rollback(status);
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String uploadApprovalDoc(HttpServletRequest request) throws ServiceException, AccountingException {
        String docID = "";
        try {
            Boolean fileflag = false;
            String fileName = "";
            boolean isUploaded;
            String Ext;
            final String sep = StorageHandler.GetFileSeparator();
            DiskFileUpload fu = new DiskFileUpload();
            java.util.List fileItems = null;
            FileItem fi = null;

            try {
                fileItems = fu.parseRequest(request);
            } catch (FileUploadException e) {
                throw ServiceException.FAILURE("accPurchaseOrderControllerCMN.uploadApprovalDoc", e);
            }

            for (java.util.Iterator k = fileItems.iterator(); k.hasNext();) {
                fi = (FileItem) k.next();
                if (!fi.isFormField()) {
                    if (fi.getSize() != 0) {
                        fileflag = true;
                        fileName = new String(fi.getName().getBytes());
                    } else {
                        throw new AccountingException("File not uploaded! File should not be empty."); // When file is empty
                    }
                }
            }

            if (fileflag) {
                try {
                    String storePath = StorageHandler.GetDocStorePath();
                    File destDir = new File(storePath);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }
                    int doccount = 0;
                    fu = new DiskFileUpload();
                    fu.setSizeMax(-1);
                    fu.setSizeThreshold(4096);
                    fu.setRepositoryPath(storePath);
                    for (Iterator i = fileItems.iterator(); i.hasNext();) {
                        fi = (FileItem) i.next();
                        if (!fi.isFormField() && fi.getSize() != 0 && doccount < 3) {
                            Ext = "";
                            doccount++; // ie 8 fourth file gets attached				
                            String filename = UUID.randomUUID().toString();
                            try {
                                fileName = new String(fi.getName().getBytes(), "UTF8");
                                if (fileName.contains(".")) {
                                    Ext = fileName.substring(fileName.lastIndexOf("."));
                                }
                                if (fi.getSize() != 0) {
                                    isUploaded = true;
                                    File uploadFile = new File(storePath + sep + filename + Ext);
                                    fi.write(uploadFile);

                                    InvoiceDocuments document = new InvoiceDocuments();
                                    document.setDocID(filename);
                                    document.setDocName(fileName);
                                    document.setDocType("");

                                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                    hashMap.put("InvoceDocument", document);
                                    accInvoiceDAOobj.saveinvoiceDocuments(hashMap);
                                    docID = document.getID();
                                } else {
                                    isUploaded = false;
                                }
                            } catch (Exception e) {
                                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, e);
                                throw ServiceException.FAILURE("accPurchaseOrderControllerCMN.uploadApprovalDoc", e);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    throw ServiceException.FAILURE("accPurchaseOrderControllerCMN.uploadApprovalDoc", ex);
                }
            }
        } catch (AccountingException ae) {
            throw new AccountingException("File not uploaded! File should not be empty.");
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accPurchaseOrderControllerCMN.uploadApprovalDoc", ex);
        }
        return docID;
    }
    
    public ModelAndView unlinkVendorQuotationDocuments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        StringBuffer msg = new StringBuffer();
        boolean issuccess = false;
        try {
//            int linkType = -1; // linkType = 1 - Purchase Requisition, and -1 - No linking available
//            String linkedDocNo = "";
            String purchaseRequisitionNo = "", customerQuotationNo = "", purchaseOrderNo = "", vendorInvoiceNo = "", rfqNo = "";
            String billid = (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) ? (String) request.getParameter("billid") : "";

            KwlReturnObject result = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), billid);
            VendorQuotation vendorQuotation = (VendorQuotation) result.getEntityList().get(0);
            String vqNo = vendorQuotation.getQuotationNumber();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray documentArr = new JSONArray(request.getParameter("data"));
            if (!StringUtil.isNullOrEmpty(billid) && documentArr != null && documentArr.length() > 0) {
                HashMap<String, Object> linkingrequestParams = new HashMap<String, Object>();
                for (int i = 0; i < documentArr.length(); i++) {
                    JSONObject document = documentArr.getJSONObject(i);
                    int type = document.optInt("type", -1);
                    String linkedTransactionID = document.optString("billid", "");
                    if (!StringUtil.isNullOrEmpty(linkedTransactionID) && type != -1) {

               if (type == 7) {//PR->VQ


            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("vendorquotation.ID");
            filter_params.add(vendorQuotation.getID());
            requestParams.put("filter_names", filter_names);
            requestParams.put("filter_params", filter_params);

            KwlReturnObject invDetailsResult = accPurchaseOrderobj.getQuotationDetails(requestParams);
            List<VendorQuotationDetail> quotationDetailList = invDetailsResult.getEntityList();
            for (VendorQuotationDetail vendorQuotationDetail : quotationDetailList) {
                if (vendorQuotationDetail.getPurchaseRequisitionDetailsId() != null) {
                    KwlReturnObject objResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), vendorQuotationDetail.getPurchaseRequisitionDetailsId());
                    PurchaseRequisitionDetail purchaseRequisitionDetail = (PurchaseRequisitionDetail) objResult.getEntityList().get(0);
                                    if (purchaseRequisitionDetail.getPurchaserequisition().getID().equals(linkedTransactionID)) {
                                        if (purchaseRequisitionDetail != null && purchaseRequisitionNo.indexOf(purchaseRequisitionDetail.getPurchaserequisition().getPrNumber()) == -1) {
                                            purchaseRequisitionNo += purchaseRequisitionDetail.getPurchaserequisition().getPrNumber() + ",";
                    }
                    vendorQuotationDetail.setPurchaseRequisitionDetailsId(null);
                                        
                }

            }
                            }

                            /*
                             * Deleting linking information of VQ from linking
                             * information table of PR & VQ at the time of
                             * unlinking PR from VQ
                             */
                            
                            linkingrequestParams.clear();
                            linkingrequestParams.put("qid", billid);//Vendor Quotation ID 
                            linkingrequestParams.put("linkedTransactionID", linkedTransactionID);//Purchase Requisition ID
                            linkingrequestParams.put("type", type);
                            linkingrequestParams.put("unlinkflag", true);
                            accPurchaseOrderobj.deleteLinkingInformationOfVQ(linkingrequestParams);//Deleting linking information of VQ
                        } else if (type == 9) {//RFQ->VQ

                            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                            filter_names.add("vendorquotation.ID");
                            filter_params.add(vendorQuotation.getID());
                            requestParams.put("filter_names", filter_names);
                            requestParams.put("filter_params", filter_params);

                            KwlReturnObject invDetailsResult = accPurchaseOrderobj.getQuotationDetails(requestParams);
                            List<VendorQuotationDetail> quotationDetailList = invDetailsResult.getEntityList();
                            for (VendorQuotationDetail vendorQuotationDetail : quotationDetailList) {
                                if (vendorQuotationDetail.getRfqDetailsId() != null) {
                                    KwlReturnObject objResult = accountingHandlerDAOobj.getObject(RequestForQuotationDetail.class.getName(), vendorQuotationDetail.getRfqDetailsId());
                                    RequestForQuotationDetail requestForQuotationDetail = (RequestForQuotationDetail) objResult.getEntityList().get(0);
                                    if (requestForQuotationDetail.getRequestforquotation().getID().equals(linkedTransactionID)) {
                                        if (requestForQuotationDetail != null && rfqNo.indexOf(requestForQuotationDetail.getRequestforquotation().getRfqNumber()) == -1) {
                                            rfqNo += requestForQuotationDetail.getRequestforquotation().getRfqNumber() + ",";
                                        }
                                        vendorQuotationDetail.setRfqDetailsId(null);
                                    }
                                }
                            }

                            /*
                             * Deleting linking information of VQ from linking
                             * information table of RFQ & VQ at the time of
                             * unlinking RFQ from VQ
                             */
                            linkingrequestParams.clear();
                            linkingrequestParams.put("qid", billid);//Vendor Quotation ID 
                            linkingrequestParams.put("linkedTransactionID", linkedTransactionID);//Purchase Requisition ID
                            linkingrequestParams.put("type", type);
                            linkingrequestParams.put("unlinkflag", true);
                            accPurchaseOrderobj.deleteLinkingInformationOfVQ(linkingrequestParams);//Deleting linking information of VQ
                        } else if (type == 2) {//VQ->PO

                            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();

                            filter_names.add("vqdetail.vendorquotation.ID");
                            filter_params.add(billid);
                            requestParams.put("filter_names", filter_names);
                            requestParams.put("filter_params", filter_params);
                            KwlReturnObject poDetailsResult = accPurchaseOrderobj.getPurchaseOrderDetails(requestParams);
                            List<PurchaseOrderDetail> poDetailList = poDetailsResult.getEntityList();
                            if (poDetailList != null) {
                                VendorQuotationDetail vendorQuotationDetail = null;
                                for (PurchaseOrderDetail purchaseOrderDetail : poDetailList) {
                                    if (purchaseOrderDetail.getVqdetail() != null) {

                                        vendorQuotationDetail = purchaseOrderDetail.getVqdetail();
                                        if (purchaseOrderNo.indexOf(purchaseOrderDetail.getPurchaseOrder().getPurchaseOrderNumber()) == -1) {
                                            purchaseOrderNo += purchaseOrderDetail.getPurchaseOrder().getPurchaseOrderNumber() + ",";
                                        }
                                        purchaseOrderDetail.setVqdetail(null);
                                        vendorQuotationDetail.getVendorquotation().setIsOpen(true);
                                       
                                    }
                                }
                                KwlReturnObject resultso = accGoodsReceiptobj.checkVQLinkedWithAnotherPO(vendorQuotationDetail.getVendorquotation().getID());//Returning no of PO linked with VQ

                                Long count = (Long) resultso.getEntityList().get(0);
                                if (count == 1) {
                                    vendorQuotationDetail.getVendorquotation().setLinkflag(0);
                                }

                                linkingrequestParams.clear();
                                linkingrequestParams.put("linkedTransactionID", linkedTransactionID);//Purchase Order ID 
                                linkingrequestParams.put("qid", billid);//Vendor Quotation ID
                                linkingrequestParams.put("type", type);
                                linkingrequestParams.put("unlinkflag", true);
                                accPurchaseOrderobj.deleteLinkingInformationOfVQ(linkingrequestParams);//Deleting linking information of VQ


                            }
                        } else if (type == 8) {//VQ->PI
                            VendorQuotationDetail vendorQuotationDetails = null;
                            KwlReturnObject doDetailsResult = accGoodsReceiptobj.getPIDetailsFromVQ(linkedTransactionID, billid, companyid);
                            List<GoodsReceiptDetail> goodsReceiptDetails = doDetailsResult.getEntityList();
                            if (goodsReceiptDetails != null) {
                                for (GoodsReceiptDetail grDetails : goodsReceiptDetails) {
                                    if (grDetails.getVendorQuotationDetail() != null) {
                                        vendorQuotationDetails = grDetails.getVendorQuotationDetail();
                                        grDetails.setVendorQuotationDetail(null);
                                        vendorQuotationDetails.getVendorquotation().setIsOpen(true);
                                        if (vendorInvoiceNo.indexOf(grDetails.getGoodsReceipt().getGoodsReceiptNumber()) == -1) {
                                            vendorInvoiceNo += grDetails.getGoodsReceipt().getGoodsReceiptNumber() + ",";
                                        }
                                    }
                                }
                                KwlReturnObject resultso = accGoodsReceiptobj.checkVQLinkedWithAnotherPI(vendorQuotationDetails.getVendorquotation().getID());

                                Long count = (Long) resultso.getEntityList().get(0);
                                if (count == 1) {
                                    vendorQuotationDetails.getVendorquotation().setLinkflag(0);
                                }
                                linkingrequestParams.clear();
                                linkingrequestParams.put("billid", linkedTransactionID);//Purchase Invoice ID
                                linkingrequestParams.put("linkedTransactionID", billid);//Vendor Quotation ID
                                linkingrequestParams.put("type", type);
                                linkingrequestParams.put("unlinkflag", true);
                                accGoodsReceiptobj.deleteLinkingInformationOfPI(linkingrequestParams);//Deleting linking information 
                            }

                        } else if (type == 6) {//VQ->CQ

                            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                            filter_names.add("quotation.ID");
                            filter_params.add(linkedTransactionID);
                            requestParams.put("filter_names", filter_names);
                            requestParams.put("filter_params", filter_params);
                            KwlReturnObject quotationDetailsResult = accSalesOrderDAOobj.getQuotationDetails(requestParams);
                            List<QuotationDetail> quotationDetailList = quotationDetailsResult.getEntityList();

                            for (QuotationDetail quotationDetail : quotationDetailList) {
                                if (quotationDetail.getVendorquotationdetails() != null) {
                                    KwlReturnObject objResult = accountingHandlerDAOobj.getObject(VendorQuotationDetail.class.getName(), quotationDetail.getVendorquotationdetails());
                                    VendorQuotationDetail vendorQuotationDetail = (VendorQuotationDetail) objResult.getEntityList().get(0);
                                    if (vendorQuotationDetail.getVendorquotation().getID().equals(billid)) {
                                        if (vendorQuotationDetail != null && customerQuotationNo.indexOf(quotationDetail.getQuotation().getquotationNumber()) == -1) {
                                            customerQuotationNo += quotationDetail.getQuotation().getquotationNumber() + ",";
                                        }
                                        quotationDetail.setVendorquotationdetails(null);
                                        quotationDetail.getQuotation().setIsopen(true);
                                        quotationDetail.getQuotation().setLinkflag(0);
                                    }


                                }
                            }
                            
                            linkingrequestParams.clear();
                            linkingrequestParams.put("qid", billid);//Vendor Quotation ID 
                            linkingrequestParams.put("linkedTransactionID", linkedTransactionID);//Customer Quotation ID
                            linkingrequestParams.put("type", type);
                            linkingrequestParams.put("unlinkflag", true);
                            accPurchaseOrderobj.deleteLinkingInformationOfVQ(linkingrequestParams);//Deleting linking information of VQ 

                        }
                    }
                }
            }

   
        
            if (!StringUtil.isNullOrEmpty(vqNo) && !StringUtil.isNullOrEmpty(purchaseRequisitionNo)) {
                msg.append(messageSource.getMessage("acc.field.purchaseRequisition(s)", null, RequestContextUtils.getLocale(request)) + " " + purchaseRequisitionNo.substring(0, purchaseRequisitionNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.vend.createvendQ", null, RequestContextUtils.getLocale(request)) + " " + vqNo + ".");
                issuccess = true;
                msg.append("<br>");
                auditTrailObj.insertAuditLog(AuditAction.UNLINK_PREQ_FROM_VQ, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Vendor Quotation " + vqNo + " from the Purchase Requisition(s) " + purchaseRequisitionNo.substring(0, purchaseRequisitionNo.length() - 1) + ".", request, vqNo);
            }
            if (!StringUtil.isNullOrEmpty(vqNo) && !StringUtil.isNullOrEmpty(purchaseOrderNo)) {
                msg.append(messageSource.getMessage("acc.field.purchaseOrder(s)", null, RequestContextUtils.getLocale(request)) + " " + purchaseOrderNo.substring(0, purchaseOrderNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.vend.createvendQ", null, RequestContextUtils.getLocale(request)) + " " + vqNo + ".");
                issuccess = true;
                msg.append("<br>");
                auditTrailObj.insertAuditLog(AuditAction.UNLINK_VQ_FROM_PO, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Vendor Quotation " + vqNo + " from the Purchase Order(s) " + purchaseOrderNo.substring(0, purchaseOrderNo.length() - 1) + ".", request, vqNo);
            }
            if (!StringUtil.isNullOrEmpty(vqNo) && !StringUtil.isNullOrEmpty(vendorInvoiceNo)) {
                msg.append(messageSource.getMessage("acc.field.purchaseInvoice(s)", null, RequestContextUtils.getLocale(request)) + " " + vendorInvoiceNo.substring(0, vendorInvoiceNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.vend.createvendQ", null, RequestContextUtils.getLocale(request)) + " " + vqNo + ".");
                issuccess = true;
                msg.append("<br>");
                auditTrailObj.insertAuditLog(AuditAction.UNLINK_VQ_FROM_PI, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Vendor Quotation " + vqNo + " from the Purchase Invoice(s) " + vendorInvoiceNo.substring(0, vendorInvoiceNo.length() - 1) + ".", request, vqNo);
            }
            if (!StringUtil.isNullOrEmpty(vqNo) && !StringUtil.isNullOrEmpty(customerQuotationNo)) {
                msg.append(messageSource.getMessage("acc.field.CustomerQuotation(s)", null, RequestContextUtils.getLocale(request)) + " " + customerQuotationNo.substring(0, customerQuotationNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.vend.createvendQ", null, RequestContextUtils.getLocale(request)) + " " + vqNo + ".");
                issuccess = true;
                msg.append("<br>");
                auditTrailObj.insertAuditLog(AuditAction.UNLINK_VQ_FROM_PI, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Vendor Quotation " + vqNo + " from the Customer Quotation(s) " + customerQuotationNo.substring(0, customerQuotationNo.length() - 1) + ".", request, vqNo);
            }
            if (!StringUtil.isNullOrEmpty(vqNo) && !StringUtil.isNullOrEmpty(rfqNo)) {
                msg.append(messageSource.getMessage("acc.field.RFQ(s)", null, RequestContextUtils.getLocale(request)) + " " + rfqNo.substring(0, rfqNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.vend.createvendQ", null, RequestContextUtils.getLocale(request)) + " " + vqNo + ".");
                issuccess = true;
                msg.append("<br>");
                auditTrailObj.insertAuditLog(AuditAction.UNLINK_RFQ_FROM_VQ, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Vendor Quotation " + vqNo + " from the RFQ(s) " + rfqNo.substring(0, rfqNo.length() - 1) + ".", request, vqNo);
            }
        } catch (Exception ex) {
            msg.append("accPurchaseOrderControllerCMN.unlinkVendorQuotationDocuments:" + ex.getMessage());
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView unlinkPurchaseOrderDocuments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        StringBuffer msg = new StringBuffer();
        boolean issuccess = false;
        try {
            int linkType = -1; 
            String linkedDocNo = "";
            String vendorQuotationNo = "", salesOrderNo = "",purchaseReqNo = "", goodsReceiptNo = "", invoiceNo = "", poSalesOrderNo = "";
            String billid = (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) ? (String) request.getParameter("billid") : "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray documentArr = new JSONArray(request.getParameter("data"));
            if (!StringUtil.isNullOrEmpty(billid) && documentArr != null && documentArr.length() > 0) {

                HashMap<String, Object> linkingrequestParams = new HashMap<String, Object>();

                HashMap<String, Object> requestParams = new HashMap<String, Object>();

                KwlReturnObject result = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), billid);
                PurchaseOrder purchaseOrder = (PurchaseOrder) result.getEntityList().get(0);
                String poNo = purchaseOrder.getPurchaseOrderNumber();
                
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("purchaseOrder.ID");
                filter_params.add(purchaseOrder.getID());
                requestParams.put("filter_names", filter_names);
                requestParams.put("filter_params", filter_params);

                KwlReturnObject poDetailsResult = accPurchaseOrderobj.getPurchaseOrderDetails(requestParams);
                List<PurchaseOrderDetail> poDetailList = poDetailsResult.getEntityList();
          
                for (int i = 0; i < documentArr.length(); i++) {

                    JSONObject document = documentArr.getJSONObject(i);
                    int type = document.optInt("type", -1);
                    String linkedTransactionID = document.optString("billid", "");
                    if (!StringUtil.isNullOrEmpty(linkedTransactionID) && type != -1) {

                  
                        if (poDetailList != null && poDetailList.size() > 0) {
                            if (type == 2) {//SO->PO
                                KwlReturnObject soObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), linkedTransactionID);
                                SalesOrder salesOrder = (SalesOrder) soObj.getEntityList().get(0);
                                if (salesOrder != null) {
                                    Set<SalesOrderDetail> sodetails = salesOrder.getRows();
                                    if (sodetails != null && sodetails.size() > 0) {
                                        for (PurchaseOrderDetail purchaseOrderDetail : poDetailList) {
                                            if (purchaseOrderDetail.getSalesorderdetailid() != null) {
                                                for (SalesOrderDetail salesOrderDetails : sodetails) {
                                                    if (salesOrderDetails != null && !StringUtil.isNullOrEmpty(purchaseOrderDetail.getSalesorderdetailid()) && purchaseOrderDetail.getSalesorderdetailid().equals(salesOrderDetails.getID())) {
                                                        KwlReturnObject objResult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), purchaseOrderDetail.getSalesorderdetailid());
                                                        SalesOrderDetail salesOrderDetail = (SalesOrderDetail) objResult.getEntityList().get(0);
                                                        if (salesOrderNo.indexOf(salesOrderDetail.getSalesOrder().getSalesOrderNumber()) == -1) {
                                                            salesOrderNo += salesOrderDetail.getSalesOrder().getSalesOrderNumber() + ",";
                                                        }
                                                        purchaseOrderDetail.setSalesorderdetailid(null);
                                                    }                                 
                                                }
                                            }
                                        }
                                    }
                                }
                                linkingrequestParams.clear();
                                linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                                linkingrequestParams.put("poid", billid);
                                linkingrequestParams.put("type", type);
                                linkingrequestParams.put("unlinkflag", true);
                                accPurchaseOrderobj.deleteLinkingInformationOfPO(linkingrequestParams);//Deleting linking information of PO

                            } else if (type == 5) {//VQ->PO
                                VendorQuotationDetail vendorQuotationDetail = null;
                                for (PurchaseOrderDetail purchaseOrderDetail : poDetailList) {
                                    if (purchaseOrderDetail.getVqdetail() != null) {
                                        
                                        vendorQuotationDetail = purchaseOrderDetail.getVqdetail();
                                        if (vendorQuotationDetail.getVendorquotation().getID().equals(linkedTransactionID)) {
                                            if (vendorQuotationNo.indexOf(vendorQuotationDetail.getVendorquotation().getQuotationNumber()) == -1) {
                                                vendorQuotationNo += vendorQuotationDetail.getVendorquotation().getQuotationNumber() + ",";
                                            }
                                            purchaseOrderDetail.setVqdetail(null);
                                            vendorQuotationDetail.getVendorquotation().setIsOpen(true);
                                        }
                                }
                                    
                                }
                                KwlReturnObject resultso = accGoodsReceiptobj.checkVQLinkedWithAnotherPO(vendorQuotationDetail.getVendorquotation().getID());//Returning no of PO linked with VQ

                                Long count = (Long) resultso.getEntityList().get(0);
                                if (count == 1) {
                                    vendorQuotationDetail.getVendorquotation().setLinkflag(0);
                                }
                                linkingrequestParams.clear();
                                linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                                linkingrequestParams.put("poid", billid);
                                linkingrequestParams.put("type", type);
                                linkingrequestParams.put("unlinkflag", true);
                                accPurchaseOrderobj.deleteLinkingInformationOfPO(linkingrequestParams);//Deleting linking information of PO
                            } else if (type == Constants.Link_PR_TO_PO) {
                                KwlReturnObject soObj = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), linkedTransactionID);
                                PurchaseRequisition purchaseRequisition = (PurchaseRequisition) soObj.getEntityList().get(0);
                                if (purchaseRequisition != null) {
                                    Set<PurchaseRequisitionDetail> requisitionDetails = purchaseRequisition.getRows();
                                    if (requisitionDetails != null && requisitionDetails.size() > 0) {
                                        for (PurchaseOrderDetail purchaseOrderDetail : poDetailList) {
                                            if (purchaseOrderDetail.getPurchaseRequisitionDetailId() != null) {
                                                for (PurchaseRequisitionDetail purchaseRequisitionDetail : requisitionDetails) {
                                                    if (purchaseRequisitionDetail != null && !StringUtil.isNullOrEmpty(purchaseOrderDetail.getPurchaseRequisitionDetailId()) && purchaseOrderDetail.getPurchaseRequisitionDetailId().equals(purchaseRequisitionDetail.getID())) {
                                                        KwlReturnObject objResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), purchaseOrderDetail.getPurchaseRequisitionDetailId());
                                                        PurchaseRequisitionDetail purchaseRequisitionDetail1 = (PurchaseRequisitionDetail) objResult.getEntityList().get(0);
                                                        if (purchaseReqNo.indexOf(purchaseRequisitionDetail1.getPurchaserequisition().getPrNumber()) == -1) {
                                                            purchaseReqNo += purchaseRequisitionDetail1.getPurchaserequisition().getPrNumber() + ",";
                                                        }
                                                        purchaseOrderDetail.setPurchaseRequisitionDetailId(null);
                                                        
                                                        
                                                        /*------ Updating isopeninpo flag while unlinking PO from requisition--------- */
                                                        requestParams.put("requisitionDetailId", purchaseRequisitionDetail.getID());
                                                        requestParams.put("purchaseOrderDetailId", purchaseOrderDetail.getID());
                                                        double resultQuantity = accPurchaseOrderModuleServiceObj.getQuantityStatusOfRequisition(requestParams);
                                                        if (purchaseRequisitionDetail.getQuantity() > resultQuantity) {
                                                            /*------ Updating openinpo flag of Requisition to true-----*/
                                                            purchaseRequisition.setIsOpenInPO(true);
                                                        }
                                                        
                                                   
                                                         /*------Update Balance Quantity of Requisition Detail------------ */
                                                        purchaseRequisitionDetail.setBalanceqty(purchaseRequisitionDetail.getBalanceqty()+purchaseOrderDetail.getQuantity());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                linkingrequestParams.clear();
                                linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                                linkingrequestParams.put("poid", billid);
                                linkingrequestParams.put("type", type);
                                linkingrequestParams.put("unlinkflag", true);
                                accPurchaseOrderobj.deleteLinkingInformationOfPO(linkingrequestParams);//Deleting linking information of PO
                            }
                        }


                        if (type == 3) {//PO->SO
                            filter_names.clear();
                            filter_params.clear();
                            filter_names.add("salesOrder.ID");
                            filter_params.add(linkedTransactionID);
                            KwlReturnObject soDetailResult = accSalesOrderDAOobj.getSalesOrderDetails(requestParams);
                            List<SalesOrderDetail> soDetailList = soDetailResult.getEntityList();
                          
                            if (purchaseOrder != null) {
                                Set<PurchaseOrderDetail> podetails = purchaseOrder.getRows();
                                if (podetails != null && podetails.size() > 0) {


                                    for (SalesOrderDetail salesOrderDetail : soDetailList) {
                                        if (salesOrderDetail.getPurchaseorderdetailid() != null) {

                                            for (PurchaseOrderDetail purchaseOrderDetails : podetails) {

                                                if (purchaseOrderDetails != null && !StringUtil.isNullOrEmpty(salesOrderDetail.getPurchaseorderdetailid()) && salesOrderDetail.getPurchaseorderdetailid().equals(purchaseOrderDetails.getID())) {
                                                    if (poSalesOrderNo.indexOf(salesOrderDetail.getSalesOrder().getSalesOrderNumber()) == -1) {
                                                        poSalesOrderNo += salesOrderDetail.getSalesOrder().getSalesOrderNumber() + ",";
                                                    }
                                                    salesOrderDetail.setPurchaseorderdetailid(null);

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            linkingrequestParams.clear();
                            linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                            linkingrequestParams.put("poid", billid);
                            linkingrequestParams.put("type", type);
                            linkingrequestParams.put("unlinkflag", true);
                            accPurchaseOrderobj.deleteLinkingInformationOfPO(linkingrequestParams);//Deleting linking information of PO

                        } else if (type == 4) {//PO->GR
                                PurchaseOrderDetail purchaseOrderDetails = null;
                                KwlReturnObject doDetailsResult = accGoodsReceiptobj.getGRDetailsFromPO(linkedTransactionID, billid, companyid);
                                List<GoodsReceiptOrderDetails> grOrderdetails = doDetailsResult.getEntityList();
                                if (grOrderdetails != null) {
                                    /*
                                         * Updating balance quantity of product
                                         * in PurchaseOrderDetail table
                                     *
                                     * while unlinking PO linked with GR
                                     */
                                    accGoodsReceiptobj.updatePOBalanceQtyAfterGR(linkedTransactionID, billid, companyid);

                                /* updating isPOClosed flag to false if PO is unlinked from GR i.e PO->GR*/
                                    purchaseOrder.setIsPOClosed(false);

                                    for (GoodsReceiptOrderDetails groDetails : grOrderdetails) {
                                        if (groDetails.getPodetails() != null) {
                                            purchaseOrderDetails = groDetails.getPodetails();
                                            groDetails.setPodetails(null);

                                        /* updating isLineItemClosed flag to false if PO is unlinked from GR  i.e PO->GR*/
                                            purchaseOrderDetails.setIsLineItemClosed(false);
                                            purchaseOrderDetails.getPurchaseOrder().setIsOpen(true);
                                            if (goodsReceiptNo.indexOf(groDetails.getGrOrder().getGoodsReceiptOrderNumber()) == -1) {
                                                goodsReceiptNo += groDetails.getGrOrder().getGoodsReceiptOrderNumber() + ",";
                                            }
                                        }
                                    }
                                    KwlReturnObject resultso = accGoodsReceiptobj.checkPOLinkedWithAnotherGR(billid);//Returning no of GR linked with PO

                                    Long count = (Long) resultso.getEntityList().get(0);
                                    if (count == 1) {
                                        purchaseOrderDetails.getPurchaseOrder().setLinkflag(0);
                                    }
                                    linkingrequestParams.clear();
                                    linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                                    linkingrequestParams.put("poid", billid);
                                    linkingrequestParams.put("type", type);
                                    linkingrequestParams.put("unlinkflag", true);
                                    accPurchaseOrderobj.deleteLinkingInformationOfPO(linkingrequestParams);//Deleting linking information of PO
                                }

                        } else if (type == 1) {//PO->PI
                            if (purchaseOrder.isIsExpenseType()) {
                                ExpensePODetail expensePODetails = null;
                                KwlReturnObject expensePIResult = accGoodsReceiptobj.getExpensePIDetailsFromPO(linkedTransactionID,billid,companyid);
                                List<ExpenseGRDetail> expenseGRDetails = expensePIResult.getEntityList();
                                 if (expenseGRDetails != null && !expenseGRDetails.isEmpty()) {
                                    for (ExpenseGRDetail gRDetail : expenseGRDetails) {
                                        if (gRDetail.getExpensePODetail() != null) {
                                            expensePODetails = gRDetail.getExpensePODetail();
                                            gRDetail.setExpensePODetail(null);
                                            expensePODetails.getPurchaseOrder().setIsOpen(true);
                                            /**
                                             * when unlink Expense Purchase
                                             * Order From 'Expense PI' need to
                                             * update Balance Amount.
                                             */
                                            expensePODetails.setBalAmount(expensePODetails.getBalAmount() + gRDetail.getAmount());
                                            if (invoiceNo.indexOf(gRDetail.getGoodsReceipt().getGoodsReceiptNumber()) == -1) {
                                                invoiceNo += gRDetail.getGoodsReceipt().getGoodsReceiptNumber() + ",";
                                            }
                                        }
                                    }
                                    KwlReturnObject resultso = accGoodsReceiptobj.checkPOLinkedWithAnotherPI(billid);//returning no of PI linked with PO

                                    Long count = (Long) resultso.getEntityList().get(0);
                                    if (count == 1) {
                                        expensePODetails.getPurchaseOrder().setLinkflag(0);
                                    }
                                    linkingrequestParams.clear();
                                    linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                                    linkingrequestParams.put("poid", billid);
                                    linkingrequestParams.put("type", type);
                                    linkingrequestParams.put("unlinkflag", true);
                                    accPurchaseOrderobj.deleteLinkingInformationOfPO(linkingrequestParams);//Deleting linking information of PO
                                 }
                            } else {
                                PurchaseOrderDetail purchaseOrderDetails = null;
                                KwlReturnObject doDetailsResult = accGoodsReceiptobj.getPIDetailsFromPO(linkedTransactionID, billid, companyid);
                                List<GoodsReceiptDetail> invoiceDetails = doDetailsResult.getEntityList();
                                if (invoiceDetails != null) {
                                    for (GoodsReceiptDetail grDetails : invoiceDetails) {
                                        if (grDetails.getPurchaseorderdetail() != null) {
                                            purchaseOrderDetails = grDetails.getPurchaseorderdetail();

                                       /* updating isLineItemClosed & isPOClosed flag to false if PO is unlinked from PI that was linked with GR i.e PO->PI->GR*/
                                            KwlReturnObject doresult = accPurchaseOrderobj.checkWhetherPOIsUsedInGROrNot(purchaseOrderDetails.getID(), companyid);
                                            List list1 = doresult.getEntityList();
                                            if (list1.size() > 0) {
                                                purchaseOrderDetails.getPurchaseOrder().setIsPOClosed(false);
                                                purchaseOrderDetails.setIsLineItemClosed(false);

                                            /*Updating balance quantity of PO, if PI is linked with PO and PI is linked with GR */
                                                String grOrderDetailId = (String) doresult.getEntityList().get(0);
                                                result = accountingHandlerDAOobj.getObject(GoodsReceiptOrderDetails.class.getName(), grOrderDetailId);
                                                GoodsReceiptOrderDetails groDetails = (GoodsReceiptOrderDetails) result.getEntityList().get(0);

                                                HashMap poMap = new HashMap();
                                                poMap.put("podetails", purchaseOrderDetails.getID());
                                                poMap.put("companyid", companyid);
                                                poMap.put("balanceqty", groDetails.getDeliveredQuantity());
                                                poMap.put("add", true);
                                                accCommonTablesDAO.updatePurchaseOrderStatus(poMap);
                                            }
                                            grDetails.setPurchaseorderdetail(null);
                                            purchaseOrderDetails.getPurchaseOrder().setIsOpen(true);
                                            if (invoiceNo.indexOf(grDetails.getGoodsReceipt().getGoodsReceiptNumber()) == -1) {
                                                invoiceNo += grDetails.getGoodsReceipt().getGoodsReceiptNumber() + ",";
                                            }
                                        }
                                    }
                                    KwlReturnObject resultso = accGoodsReceiptobj.checkPOLinkedWithAnotherPI(billid);//returning no of PI linked with PO

                                    Long count = (Long) resultso.getEntityList().get(0);
                                    if (count == 1) {
                                        purchaseOrderDetails.getPurchaseOrder().setLinkflag(0);
                                    }
                                    linkingrequestParams.clear();
                                    linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                                    linkingrequestParams.put("poid", billid);
                                    linkingrequestParams.put("type", type);
                                    linkingrequestParams.put("unlinkflag", true);
                                    accPurchaseOrderobj.deleteLinkingInformationOfPO(linkingrequestParams);//Deleting linking information of PO
                                }
                                }
                            }
                        }
                    }

                if (!StringUtil.isNullOrEmpty(poNo) && !StringUtil.isNullOrEmpty(vendorQuotationNo)) {
                    msg.append(messageSource.getMessage("acc.field.vendorQuotation(s)", null, RequestContextUtils.getLocale(request)) + " " + vendorQuotationNo.substring(0, vendorQuotationNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.wtfTrans.po", null, RequestContextUtils.getLocale(request)) + " " + poNo + ".");
                    issuccess = true;
                    msg.append("<br>");
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_VQ_FROM_PO, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Purchase Order " + poNo + " from the Vendor Quotation(s) " + vendorQuotationNo.substring(0, vendorQuotationNo.length() - 1) + ".", request, poNo);
                }
                if (!StringUtil.isNullOrEmpty(poNo) && !StringUtil.isNullOrEmpty(salesOrderNo)) {
                    msg.append(messageSource.getMessage("acc.field.salesOrder(s)", null, RequestContextUtils.getLocale(request)) + " " + salesOrderNo.substring(0, salesOrderNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.wtfTrans.po", null, RequestContextUtils.getLocale(request)) + " " + poNo + ".");
                    issuccess = true;
                    msg.append("<br>");
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_SO_FROM_PO, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Purchase Order " + poNo + " from the Sales Order(s) " + salesOrderNo.substring(0, salesOrderNo.length() - 1) + ".", request, poNo);
                }
                if (!StringUtil.isNullOrEmpty(poNo) && !StringUtil.isNullOrEmpty(purchaseReqNo)) {
                    msg.append(messageSource.getMessage("acc.field.purchaseRequisition(s)", null, RequestContextUtils.getLocale(request)) + " " + purchaseReqNo.substring(0, purchaseReqNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.wtfTrans.po", null, RequestContextUtils.getLocale(request)) + " " + poNo + ".");
                    issuccess = true;
                    msg.append("<br>");
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_SO_FROM_PO, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Purchase Order " + poNo + " from the Purchase Requisition(s) " + purchaseReqNo.substring(0, purchaseReqNo.length() - 1) + ".", request, poNo);
                }
                if (!StringUtil.isNullOrEmpty(poNo) && !StringUtil.isNullOrEmpty(poSalesOrderNo)) {
                    msg.append(messageSource.getMessage("acc.field.salesOrder(s)", null, RequestContextUtils.getLocale(request)) + " " + poSalesOrderNo.substring(0, poSalesOrderNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.wtfTrans.po", null, RequestContextUtils.getLocale(request)) + " " + poNo + ".");
                    issuccess = true;
                    msg.append("<br>");
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_PO_FROM_SO, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Purchase Order " + poNo + " from the Sales Order(s) " + poSalesOrderNo.substring(0, poSalesOrderNo.length() - 1) + ".", request, poNo);
                }
                if (!StringUtil.isNullOrEmpty(poNo) && !StringUtil.isNullOrEmpty(invoiceNo)) {
                    msg.append(messageSource.getMessage("acc.field.purchaseInvoice(s)", null, RequestContextUtils.getLocale(request)) + " " + invoiceNo.substring(0, invoiceNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.wtfTrans.po", null, RequestContextUtils.getLocale(request)) + " " + poNo + ".");
                    issuccess = true;
                    msg.append("<br>");
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_PO_FROM_PI, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Purchase Order " + poNo + " from the Purchase Invoice(s) " + invoiceNo.substring(0, invoiceNo.length() - 1) + ".", request, poNo);
                }
                if (!StringUtil.isNullOrEmpty(poNo) && !StringUtil.isNullOrEmpty(goodsReceiptNo)) {
                    msg.append(messageSource.getMessage("acc.field.goodsReceipt(s)", null, RequestContextUtils.getLocale(request)) + " " + goodsReceiptNo.substring(0, goodsReceiptNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.wtfTrans.po", null, RequestContextUtils.getLocale(request)) + " " + poNo + ".");
                    issuccess = true;
                    msg.append("<br>");
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_PO_FROM_GR, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Purchase Order " + poNo + " from the Goods Receipt(s) " + goodsReceiptNo.substring(0, goodsReceiptNo.length() - 1) + ".", request, poNo);
                }
            }

        } catch (Exception ex) {
            msg.append("accPurchaseOrderControllerCMN.unlinkPurchaseOrderDocuments:" + ex.getMessage());
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void exportSingleRFQ(HttpServletRequest request, HttpServletResponse response) { //Document Designer Request For Quotation
        try {
            JSONObject requestObj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> otherconfigrequestParams = new HashMap();
            String RFQID = requestObj.optString("bills");
            String companyid = requestObj.optString(Constants.companyKey);
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(RequestForQuotation.class.getName(), RFQID);
            RequestForQuotation requestforquotation = (RequestForQuotation) objItr.getEntityList().get(0);
            AccCustomData accCustomData = null;
            if (requestforquotation.getRfqCustomData() != null) {
                accCustomData = requestforquotation.getRfqCustomData();
            }
            String recordids = requestObj.optString("recordids");
            int moduleid = requestObj.optInt(Constants.moduleid, Constants.Acc_RFQ_ModuleId);
            //change module id for asset module
            boolean isFixedAsset = requestObj.optBoolean(Constants.isFixedAsset, false);
            if(isFixedAsset){
                moduleid = Constants.Acc_FixedAssets_RFQ_ModuleId;
            }
            ArrayList<String> RFQIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            replaceFieldMap = new HashMap<String, String>();
            /*
             * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
             */
            fieldrequestParams.clear();
            HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
            HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

            fieldrequestParams.clear();
            HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
            HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);
            //reset module id
            if(isFixedAsset){
                moduleid = Constants.Acc_RFQ_ModuleId;
            }

            String vendorid = "";
            HashMap<String, JSONArray> itemDataRFQ = new HashMap<String, JSONArray>();
            
            HashMap<String, Object> paramMap = new HashMap();
            paramMap.put(Constants.fieldMap, FieldMap);
            paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
            paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
            paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
            
            for (int count = 0; count < RFQIDList.size(); count++) {
                KwlReturnObject objItr1 = accountingHandlerDAOobj.getObject(RequestForQuotation.class.getName(), RFQIDList.get(count));
                RequestForQuotation requestforquotation1 = (RequestForQuotation) objItr1.getEntityList().get(0);
                JSONArray lineItemsArr1 = new JSONArray();
                if (!StringUtil.isNullOrEmpty(requestforquotation1.getVendors())) {
                    String[] vendornames = requestforquotation1.getVendors().split(",");
                    for (int i = 0; i < vendornames.length; i++) {
                        vendorid = vendornames[i];
                        JSONArray lineItemsArr = getRFQetailsItemJSON(requestObj, RFQIDList.get(count), paramMap, vendorid);
                        lineItemsArr1.put(lineItemsArr);
                    }
                }
                itemDataRFQ.put(RFQIDList.get(count), lineItemsArr1);
            }

            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            String invoicePostText = "";
            ExportRecordHandler.exportSingleGeneric(request, response, itemDataRFQ, accCustomData, customDesignDAOObj,accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj, velocityEngine, invoicePostText, otherconfigrequestParams,accInvoiceServiceDAOObj,accGoodsReceiptServiceDAOObj);
        } catch (ServiceException | SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public JSONArray getRFQetailsItemJSON(JSONObject requestObj, String SOID, HashMap<String, Object> paramMap, String vendorid) {
        JSONArray jArr = new JSONArray();
        String prqref = "";
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            String companyid = requestObj.optString(Constants.companyKey);
            
            String billAddr = "", shipAddr = "", createdby = "",vendorname="",vendorcode="";
            String vendorCurrency="", vendorCurrencyCode="", vendorCurrencySymbol="", vendorTerm="", vendorTermDays="";
            double quantityConstant = 0;
            double totalAmount = 0;
            double subtotal = 0;
            String globallevelcustomfields = "", globalleveldimensions = "";

            /*Company Preferences Data*/
            KwlReturnObject pref = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) pref.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            
            HashMap<String, Object> rfqRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(RequestForQuotation.class.getName(), SOID);
            RequestForQuotation requestforquotation = (RequestForQuotation) objItr.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestObj.optString(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            filter_names.add("requestforquotation.ID");
            filter_params.add(requestforquotation.getID());
            rfqRequestParams.put("filter_names", filter_names);
            rfqRequestParams.put("filter_params", filter_params);
            rfqRequestParams.put("order_by", order_by);
            rfqRequestParams.put("order_type", order_type);
            KwlReturnObject podresult = accPurchaseOrderobj.getRFQDetails(rfqRequestParams);
            Iterator itr = podresult.getEntityList().iterator();

            int rowcnt = 0;
            while (itr.hasNext()) {
                RequestForQuotationDetail row = (RequestForQuotationDetail) itr.next();
                String proddesc = "";
                rowcnt++;
                JSONObject obj = new JSONObject();
                Product prod = row.getProduct();
                double rate = 0;

                obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr no
                obj.put(CustomDesignerConstants.ProductName, prod.getName().replaceAll("\n", "<br>"));// productname
                obj.put("type", prod.getProducttype() == null ? "" : prod.getProducttype().getName());
                proddesc = (StringUtil.isNullOrEmpty(row.getRemark()) ? "" : row.getRemark());
                proddesc = StringUtil.DecodeText(proddesc);
                obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>"));//Product Description
                obj.put(CustomDesignerConstants.PO_ProductCode, prod.getProductid() == null ? "" : prod.getProductid());
                obj.put("currencysymbol",currency.getSymbol());
                obj.put("currencycode",currency.getCurrencyCode());
                rate = row.getRate();
                obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate
                String uom = row.getUom() == null ? (row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA()) : row.getUom().getNameEmptyforNA();
                double quantity = row.getQuantity();
                double rowamountwithouttax = rate * quantity;//amount without tax
                 KwlReturnObject productCategories = null;
                productCategories = accProductObj.getProductCategoryForDetailsReport(prod.getID());
                List productCategoryList = productCategories.getEntityList();
                String cateogry = "";
                Iterator catIte = productCategoryList.iterator();
                while (catIte.hasNext()) {
                    ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                    String categoryName = pcm.getProductCategory() != null ? pcm.getProductCategory().getValue().toString() : "";
                    cateogry += categoryName + " ";
                } 
                if ( StringUtil.isNullOrEmpty(cateogry)) {
                    cateogry = "None";
                }
                obj.put("productCategory", cateogry);
                HashMap<String, Object> params = new HashMap<String, Object>();
                filter_names = new ArrayList();
                filter_params = new ArrayList();
                filter_names.add("companyid");
                filter_names.add("fieldtype");
                filter_names.add("customcolumn");
                filter_names.add("moduleid");
                filter_params.add(companyid);
                filter_params.add(4);
                filter_params.add(1);
                filter_params.add(requestObj.optInt("moduleid"));
                params.put("filter_names", filter_names);
                params.put("filter_values", filter_params);
                
                KwlReturnObject fieldparams =  accAccountDAOobj.getFieldParams(params);
                List fieldParamsList = fieldparams.getEntityList();
                
                JSONArray jsonarr = new JSONArray();
                for(int cnt=0; cnt < fieldParamsList.size(); cnt++){
                    FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("comboName", fieldParamsObj.getFieldname());
                    jsonarr.put(tempObj);
                }
                
                obj.put("groupingComboList", jsonarr);
                
                obj.put(CustomDesignerConstants.QuantitywithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom); // Quantity
                quantityConstant += quantity;
                obj.put(CustomDesignerConstants.PO_Quantity, authHandler.formattingDecimalForQuantity(quantity, companyid));
                obj.put(CustomDesignerConstants.PO_UOM, uom);
                String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                String filePathString = baseUrl + "productimage?fname=" + row.getProduct().getID() + ".png&isDocumentDesignerPrint=true";
                obj.put(CustomDesignerConstants.imageTag, filePathString);

                subtotal += authHandler.round(rowamountwithouttax, companyid);
                obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(rowamountwithouttax, companyid)); // Amount

                /*
                 * get custom line data
                 */
                Map<String, Object> variableMap = new HashMap<String, Object>();
                RequestForQuotationDetailCustomData jeDetailCustom = (RequestForQuotationDetailCustomData) row.getRequestForQuotationDetailCustomData();
                replaceFieldMap = new HashMap<String, String>();
                if (jeDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, jeDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                                            }
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields

                jArr.put(obj);
            }
            JSONObject summaryData = new JSONObject();
            totalAmount = subtotal;
            String baseCurrency = currency.getSymbol();
            DateFormat df = authHandler.getUserDateFormatterJson(requestObj);//User Date Formatter
            Date date = new Date();
            String printedOn = df.format(date);

            createdby = requestforquotation.getUsers().getFullName();
            KwlReturnObject vendorit = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorid);
            Vendor singlevendor = (Vendor) vendorit.getEntityList().get(0);
            vendorname = singlevendor.getName();
            if (!StringUtil.isNullOrEmpty(SOID)) {
                vendorcode = singlevendor.getAcccode();
            }
            vendorCurrency = singlevendor.getCurrency().getName();
            vendorCurrencyCode = singlevendor.getCurrency().getCurrencyCode();
            vendorCurrencySymbol = singlevendor.getCurrency().getSymbol();
            vendorTerm = singlevendor.getDebitTerm().getTermname();
            vendorTermDays = singlevendor.getDebitTerm().getTermdays()+"";

            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("vendorid", vendorid);
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData, preferences, extraCompanyPreferences);
                
            String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId);
            /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, Constants.Acc_RFQ_ModuleId);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", requestforquotation.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            returnvalues.clear();

            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(totalAmount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword + " Only.");
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorQuantity_Total, authHandler.formattingDecimalForQuantity(quantityConstant, companyid));
            summaryData.put(CustomDesignerConstants.BillTo, billAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.ShipTo, shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.CustomDesignBaseCurrency_fieldTypeId, baseCurrency);
            summaryData.put(CustomDesignerConstants.Createdby, createdby);
            summaryData.put(CustomDesignerConstants.RPMP_CustomerVendorName, vendorname);
            summaryData.put(CustomDesignerConstants.CustomerVendorCode, vendorcode);
            summaryData.put(CustomDesignerConstants.CustomDesignTemplate_Print, printedOn);
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.CustomDesignCurrency, vendorCurrency);
            summaryData.put(CustomDesignerConstants.CustomDesignCurrencyCode, vendorCurrencyCode);
            summaryData.put(CustomDesignerConstants.CustomDesignCurrencySymbol, vendorCurrencySymbol);
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term, vendorTerm);
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term_Days, vendorTermDays);
            summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, singlevendor.getGSTIN() != null ? singlevendor.getGSTIN() : "");
            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    public ModelAndView getVersionQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            if (StringUtil.isNullOrEmpty(request.getParameter("archieve"))) {
                requestParams.put("archieve", 0);
            } else {
                requestParams.put("archieve", Integer.parseInt(request.getParameter("archieve")));
}
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            KwlReturnObject result = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                boolean salesPersonFilterFlag = request.getParameter("salesPersonFilterFlag") != null ? Boolean.parseBoolean(request.getParameter("salesPersonFilterFlag")) : false;
                if (salesPersonFilterFlag) {
                    requestParams.put("salesPersonFilterFlag", salesPersonFilterFlag);
                }
                requestParams.put("userId", sessionHandlerImpl.getUserid(request));
                requestParams.put("versionid", request.getParameter("versionid"));
                result = accPurchaseOrderobj.getVersionQuotations(requestParams);
                DataJArr = accPurchaseOrderServiceDAOobj.getVersionQuotationsJson(request, result.getEntityList(), DataJArr);
            }
            int cnt = consolidateFlag ? DataJArr.length() : result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            if (consolidateFlag) {
                String start = request.getParameter(Constants.start);
                String limit = request.getParameter(Constants.limit);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            jobj.put("data", pagedJson);
            jobj.put("count", cnt);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getQuotationVersionRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getQuotationVersionRows(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accPurchaseOrderController.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accPurchaseOrderController.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getQuotationVersionRows(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            String closeflag = request.getParameter("closeflag");
            boolean soflag = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("sopolinkflag")) && request.getParameter("sopolinkflag") != null) {
                soflag = Boolean.FALSE.parseBoolean(request.getParameter("sopolinkflag"));
            }
            String[] sos = (String[]) request.getParameter("bills").split(",");
            String dType = request.getParameter("dtype");
            boolean isOrder = false;
            String isorder = request.getParameter("isOrder");
            if (!StringUtil.isNullOrEmpty(isorder) && StringUtil.equal(isorder, "true")) {
                isOrder = true;
            }
            boolean isReport = false;
            boolean customIsReport = false;
            if (!StringUtil.isNullOrEmpty(dType) && StringUtil.equal(dType, "report")) {
                isReport = true;
                customIsReport = true;
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("copyInvoice")) && Boolean.parseBoolean(request.getParameter("copyInvoice"))) {
                isReport = true;
            }
            int i = 0;
            JSONArray jArr = new JSONArray();
            double addobj = 1;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Vendor_Quotation_ModuleId, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("quotationversion.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);

            while (sos != null && i < sos.length) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(VendorQuotationVersion.class.getName(), sos[i]);
                VendorQuotationVersion so = (VendorQuotationVersion) result.getEntityList().get(0);
                KWLCurrency currency = null;
                if (so.getCurrency() != null) {
                    currency = so.getCurrency();
                } else {
                    currency = so.getVendor().getAccount().getCurrency() == null ? kwlcurrency : so.getVendor().getAccount().getCurrency();
                }
                filter_params.clear();
                filter_params.add(so.getID());
                KwlReturnObject podresult = accPurchaseOrderobj.getQuotationVersionDetails(soRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                while (itr.hasNext()) {
                    VendorQuotationVersionDetail row = (VendorQuotationVersionDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", so.getID());
                    obj.put("billno", so.getQuotationNumber());
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("originalTransactionRowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("productname", row.getProduct().getName());
                    String uom = row.getUom() != null ? row.getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    obj.put("unitname", uom);
                    obj.put("uomname", uom);
                    obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("multiuom", row.getProduct().isMultiuom());
                    obj.put("desc", StringUtil.isNullOrEmpty(row.getDescription()) ? row.getProduct().getDescription() : row.getDescription());
                    obj.put("type", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getName());
                    obj.put("pid", row.getProduct().getProductid());
                    obj.put("memo", row.getRemark());
                    obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
                    obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());
                    if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {  //This is sats specific code 	 
                        obj.put("invoicetype", so.getInvoicetype());
                        obj.put("dependentType", row.getDependentType() == null ? "" : row.getDependentType());
                       
                            obj.put("showquantity",StringUtil.DecodeText(row.getShowquantity() == null ? "" : row.getShowquantity()));
                        
                        obj.put("inouttime", StringUtil.isNullOrEmpty(row.getInouttime()) ? "" : row.getInouttime().replaceAll("%20", " "));
                        if (!StringUtil.isNullOrEmpty(row.getInouttime())) {
                            try {
                                String interVal = accSalesOrderServiceDAOobj.getTimeIntervalForProduct(row.getInouttime());
                                obj.put("timeinterval", interVal);
                            } catch (ParseException ex) {
                                Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        try {
                            String numberVal = row.getDependentType();
                            obj.put("dependentTypeNo", Integer.parseInt(numberVal));
                        } catch (Exception e) {
                        }
                        obj.put("parentid", ((row.getProduct().getParent() != null) ? row.getProduct().getParent().getID() : ""));
                        obj.put("parentname", ((row.getProduct().getParent() != null) ? row.getProduct().getParent().getName() : ""));
                        if (row.getProduct().getParent() != null) {
                            obj.put("issubproduct", true);
                        }
                        if (row.getProduct().getChildren().size() > 0) {
                            obj.put("isparentproduct", true);
                        } else {
                            obj.put("isparentproduct", false);
                        }
                        
                            obj.put("showquantity",StringUtil.DecodeText(row.getShowquantity() == null ? "" : row.getShowquantity()));
                        
                    }
                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable = false;
                    if (row.getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), so.getQuotationDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                        if (isRowTaxApplicable) {
                            rowTaxAmount = row.getRowTaxAmount();
                        }
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("rowTaxAmount", rowTaxAmount);
                    double amountWithoutTax = row.getRate();
                    if (row.getQuantity() != 0) {
                        amountWithoutTax = authHandler.round(row.getRate() * row.getQuantity(), companyid);
                    }
                    obj.put("rateIncludingGst", authHandler.roundUnitPrice(row.getRateincludegst(), companyid));
                    obj.put("israteIncludingGst", so.isGstIncluded());
                    obj.put("prtaxid", row.getTax() == null ? "" : row.getTax().getID());

                    if (!isReport && row.getDiscount() > 0 && isOrder) {//In Sales order creation, we need to display Unit Price including row discount
                        double discount = (row.getDiscountispercent() == 1) ? (row.getRate() * (row.getDiscount() / 100)) : row.getDiscount();
                        obj.put("rate", (row.getRate() - discount));
                        obj.put("discountispercent", 1);
                        obj.put("prdiscount", 0);
                    } else {
                        obj.put("rate", row.getRate());
                        obj.put("discountispercent", row.getDiscountispercent());
                        obj.put("prdiscount", row.getDiscount());
                    }
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    VendorQuotationVersionDetailCustomData quotationDetailCustomData = (VendorQuotationVersionDetailCustomData) row.getQuotationDetailCustomData();
                    AccountingManager.setCustomColumnValues(quotationDetailCustomData, FieldMap, replaceFieldMap, variableMap);
                    if (quotationDetailCustomData != null) {
                        JSONObject params = new JSONObject();
                        boolean isExport = false;
                        if (customIsReport) {
                            isExport = true;
                        }
                        params.put("isExport", isExport);
                        params.put("isForReport", isReport);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getQuotationDate(), 0);
                    obj.put("orderrate", row.getRate());//obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                    double baseuomrate = row.getBaseuomrate();
                    double quantity = 0;
                    quantity = row.getQuantity();
                    obj.put("quantity", quantity);
                    obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, baseuomrate, companyid));
                    if (row.getUom() != null) {
                        obj.put("uomid", row.getUom().getID());
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, row.getBaseuomrate(), companyid));
                        obj.put("baseuomrate", row.getBaseuomrate());
                    } else {
                        obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, row.getBaseuomrate(), companyid));
                        obj.put("baseuomrate", row.getBaseuomrate());
                    }
                    if (addobj > 0) {
                        jArr.put(obj);
                    }
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    
    public void exportSinglePR(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        try {
            JSONObject requestObj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> otherconfigrequestParams = new HashMap();
            String PRID = requestObj.optString("bills");
            String companyid = requestObj.optString(Constants.companyKey);
            int moduleid = requestObj.optInt(Constants.moduleid);
            //change module id for asset module
            boolean isFixedAsset = requestObj.optBoolean(Constants.isFixedAsset, false);
            if(isFixedAsset){
                moduleid = Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId;
            }

            AccCustomData accCustomData = null;
            String recordids = requestObj.optString("recordids");
            ArrayList<String> PRIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            replaceFieldMap = new HashMap<String, String>();
            /*
             * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
             */
            fieldrequestParams.clear();
            HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
            HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

            fieldrequestParams.clear();
            HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
            HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);
            //reset module id
            if(isFixedAsset){
                moduleid = Constants.Acc_Purchase_Requisition_ModuleId;
            }
            //For product custom field
            fieldrequestParams.clear();
            HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
            HashMap<String, Integer> ProductLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);


            HashMap<String, JSONArray> itemDataPO = new HashMap<String, JSONArray>();
            
            HashMap<String, Object> paramMap = new HashMap();
            paramMap.put(Constants.fieldMap, FieldMap);
            paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
            paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
            paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
            paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);
            
            for (int count = 0; count < PRIDList.size(); count++) {
                JSONArray lineItemsArr = getPRDetailsItemJSON(requestObj, PRIDList.get(count), paramMap);
                itemDataPO.put(PRIDList.get(count), lineItemsArr);

                // Below Function called to update print flag for PR Report
                accCommonTablesDAO.updatePrintFlag(moduleid, PRIDList.get(count), companyid);
            }

            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            ExportRecordHandler.exportSingleGeneric(request, response, itemDataPO, accCustomData, customDesignDAOObj,accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj, velocityEngine, "", otherconfigrequestParams,accInvoiceServiceDAOObj,accGoodsReceiptServiceDAOObj);
        } catch (ServiceException | SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JSONArray getPRDetailsItemJSON(JSONObject requestObj, String SOID, HashMap<String, Object> paramMap) {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            HashMap<String, Integer> ProductLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.productLevelCustomFieldMap);
            String companyid = requestObj.optString(Constants.companyKey);
            Date purchaseRequisitionDate = null;
            String createdby = "", updatedby = "";
            String purchaseRequisitionNo = "";
            double quantityConstant = 0;
            double totalDiscount = 0;
            double totaltax = 0;
            double totalAmount = 0;
            double subtotal = 0;
            String approvername = "", globallevelcustomfields = "", globalleveldimensions = "";
            boolean isgstincluded = false;
            int quantitydigitafterdecimal = 2, amountdigitafterdecimal = 2, unitpricedigitafterdecimal = 2;
            
            JSONObject summaryData = new JSONObject();

            HashMap<String, Object> prRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), SOID);
            PurchaseRequisition purchaseRequisition = (PurchaseRequisition) objItr.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestObj.optString(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            filter_names.add("purchaserequisition.ID");
            filter_params.add(purchaseRequisition.getID());
            prRequestParams.put("filter_names", filter_names);
            prRequestParams.put("filter_params", filter_params);
            prRequestParams.put("order_by", order_by);
            prRequestParams.put("order_type", order_type);
            KwlReturnObject prdresult = accPurchaseOrderobj.getPurchaseRequisitionDetails(prRequestParams);
            Iterator itr = prdresult.getEntityList().iterator();
            
            //Document Currency
            if (purchaseRequisition != null && purchaseRequisition.getCurrency() != null && !StringUtil.isNullOrEmpty(purchaseRequisition.getCurrency().getCurrencyID())) {
                summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, purchaseRequisition.getCurrency().getCurrencyID());
            }

            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                }
            }
            purchaseRequisitionNo = purchaseRequisition.getPrNumber();
            purchaseRequisitionDate = purchaseRequisition.getRequisitionDate();

            createdby = purchaseRequisition.getCreatedby() != null ? purchaseRequisition.getCreatedby().getFullName() : "";
            updatedby = purchaseRequisition.getModifiedby() != null ? purchaseRequisition.getModifiedby().getFullName() : "";
            
            KwlReturnObject pref = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) pref.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            int countryid = 0;
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            int rowcnt = 0;

            while (itr.hasNext()) {
                PurchaseRequisitionDetail row = (PurchaseRequisitionDetail) itr.next();
                String proddesc = "";
                rowcnt++;
                JSONObject obj = new JSONObject();
                Product prod = row.getProduct();
                double rate = 0, rowdiscountvalue = 0, baseQuantity = 0;

                obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr no
                obj.put(CustomDesignerConstants.ProductName, prod.getName().replaceAll("\n", "<br>"));// productname
                obj.put("type", prod.getProducttype() == null ? "" : prod.getProducttype().getName());
                proddesc = StringUtil.isNullOrEmpty(row.getProductdescription()) ? "" : row.getProductdescription();
                proddesc =StringUtil.DecodeText(proddesc);
                obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>"));//Product Description
                obj.put(CustomDesignerConstants.AdditionalDescription, !StringUtil.isNullOrEmpty(prod.getAdditionalDesc()) ? prod.getAdditionalDesc().replaceAll(Constants.REGEX_LINE_BREAK, "<br>") :"");  //product Addtional description ERP-39061
                obj.put(CustomDesignerConstants.PO_ProductCode, prod.getProductid() == null ? "" : prod.getProductid());
                obj.put(CustomDesignerConstants.HSCode, (prod.getHSCode() != null) ? prod.getHSCode().replaceAll("\n", "<br>") : "");//get HSCode of ProductGrid

                rate = row.getRate();
                KwlReturnObject productCategories = null;
                productCategories = accProductObj.getProductCategoryForDetailsReport(prod.getID());
                List productCategoryList = productCategories.getEntityList();
                String cateogry = "";
                Iterator catIte = productCategoryList.iterator();
                while (catIte.hasNext()) {
                    ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                    String categoryName = pcm.getProductCategory() != null ? pcm.getProductCategory().getValue().toString() : "";
                    cateogry += categoryName + " ";
                }
                if (StringUtil.isNullOrEmpty(cateogry)) {
                    cateogry = "None";
                }
                obj.put("productCategory", cateogry);
                HashMap<String, Object> params = new HashMap<String, Object>();
                filter_names = new ArrayList();
                filter_params = new ArrayList();
                filter_names.add("companyid");
                filter_names.add("fieldtype");
                filter_names.add("customcolumn");
                filter_names.add("moduleid");
                filter_params.add(companyid);
                filter_params.add(4);
                filter_params.add(1);
                filter_params.add(requestObj.optInt("moduleid"));
                params.put("filter_names", filter_names);
                params.put("filter_values", filter_params);
                
                KwlReturnObject fieldparams =  accAccountDAOobj.getFieldParams(params);
                List fieldParamsList = fieldparams.getEntityList();
                
                JSONArray jsonarr = new JSONArray();
                for(int cnt=0; cnt < fieldParamsList.size(); cnt++){
                    FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("comboName", fieldParamsObj.getFieldname());
                    jsonarr.put(tempObj);
                }
                
                obj.put("groupingComboList", jsonarr);
                
                obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate
                String uom = row.getUom() == null ? (row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA()) : row.getUom().getNameEmptyforNA();
                double quantity = row.getQuantity();
                baseQuantity = row.getBaseuomquantity();
                double rowamountwithouttax = rate * quantity;//amount without tax

                obj.put(CustomDesignerConstants.QuantitywithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom); // Quantity
                quantityConstant += quantity;
                obj.put(CustomDesignerConstants.IN_Currency, purchaseRequisition.getCurrency().getCurrencyCode());
                obj.put(CustomDesignerConstants.Quantity, authHandler.formattingDecimalForQuantity(quantity, companyid));
                obj.put(CustomDesignerConstants.BaseQty, authHandler.formattingDecimalForQuantity(baseQuantity, companyid));
                obj.put(CustomDesignerConstants.BaseQtyWithUOM, authHandler.formattingDecimalForQuantity(baseQuantity, companyid) + " " + uom);
                obj.put(CustomDesignerConstants.IN_UOM, uom);
                obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.round(rowamountwithouttax, companyid));
                obj.put("currencysymbol", purchaseRequisition.getCurrency().getSymbol()); //used for headercurrency and recordcurrency
                obj.put("isGstIncluded", isgstincluded);
                obj.put("currencycode", purchaseRequisition.getCurrency().getCurrencyCode()); //used for headercurrencycode and recordcurrency
                String stockUOM = "";
                if (prod.getUnitOfMeasure() != null) {
                    stockUOM = prod.getUnitOfMeasure().getNameEmptyforNA();
                }
                obj.put(CustomDesignerConstants.ProductAvailableQuantity, prod.getAvailableQuantity() + " " + stockUOM); //Product Avaiable quantity in Stock UOM
                obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount((rowamountwithouttax - rowdiscountvalue), companyid));//Subtotal
                String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                String filePathString = baseUrl + "productimage?fname=" + row.getProduct().getID() + ".png&isDocumentDesignerPrint=true";
                obj.put(CustomDesignerConstants.imageTag, filePathString);

                subtotal += authHandler.round(rowamountwithouttax, companyid);

                /*
                 * get custom line data
                 */
                Map<String, Object> variableMap = new HashMap<String, Object>();
                PurchaseRequisitionDetailCustomData jeDetailCustom = (PurchaseRequisitionDetailCustomData) row.getPurchaseRequisitionDetailCustomData();
                replaceFieldMap = new HashMap<String, String>();
                if (jeDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, jeDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }

                /*
                 * Product Level Custom Fields Evaluation
                 */
                KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(PurchaseOrderDetailProductCustomData.class.getName(), row.getID());
                PurchaseOrderDetailProductCustomData qProductDetailCustom = (PurchaseOrderDetailProductCustomData) resultProduct.getEntityList().get(0);
                replaceFieldMap = new HashMap<String, String>();
                if (qProductDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, qProductDetailCustom, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }

                /*
                 * Set All Line level Dimension & All LIne level Custom Field
                 * Values
                 */
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                JSONObject jObj = new JSONObject();
                if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                    jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                }
                if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                    obj = accProductObj.getProductDisplayUOM(prod, quantity,row.getBaseuomrate(), true, obj);
                }
                jArr.put(obj);
            }

            if (isgstincluded) {
                totalAmount = subtotal;
            } else {
                totalAmount = subtotal + totaltax - totalDiscount;
            }
            KwlReturnObject basecurresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestObj.optString(Constants.globalCurrencyKey));
            KWLCurrency baseCur = (KWLCurrency) basecurresult.getEntityList().get(0);
            String baseCurrency = baseCur.getSymbol();

            String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId);
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if(countryid == Constants.INDONESIAN_COUNTRY_ID){
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(totalAmount)), indoCurrency);
            }
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData, preferences, extraCompanyPreferences);

            /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(requestObj);//User Date Formatter
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, Constants.Acc_Purchase_Requisition_ModuleId);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", purchaseRequisition.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            extraparams.put("approvestatuslevel", purchaseRequisition.getApprovestatuslevel());

            //Details like company details,base currency
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);
            String userId = requestObj.optString(Constants.userid,"");
            KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userresult.getEntityList().get(0);
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(totalAmount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword + " Only.");
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.TotalQuantity, authHandler.formattingDecimalForQuantity(quantityConstant, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignBaseCurrency_fieldTypeId, baseCurrency);
            summaryData.put(CustomDesignerConstants.Approvedby, approvername);
            summaryData.put(CustomDesignerConstants.PurchaseRequisitionNumber, purchaseRequisitionNo);
            summaryData.put(CustomDesignerConstants.PurchaseRequisitionDate, df.format(purchaseRequisitionDate));
            summaryData.put(CustomDesignerConstants.Createdby, createdby);
            summaryData.put(CustomDesignerConstants.Updatedby, updatedby);
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName())?user.getFullName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserFirstName, !StringUtil.isNullOrEmpty(user.getFirstName())?user.getFirstName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserLastName, !StringUtil.isNullOrEmpty(user.getLastName())?user.getLastName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserAddress, !StringUtil.isNullOrEmpty(user.getAddress())?user.getAddress():"");
            summaryData.put(CustomDesignerConstants.CurrentUserEmail, !StringUtil.isNullOrEmpty(user.getEmailID())?user.getEmailID():"");
            summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber())?user.getContactNumber():"");
            summaryData.put(CustomDesignerConstants.CustomDesignCompAccPrefBillAddress_fieldTypeId, AccountingAddressManager.getCompanyDefaultBillingAddress(companyid, accountingHandlerDAOobj));
            summaryData.put(CustomDesignerConstants.CustomDesignCompAccPrefShipAddress_fieldTypeId, AccountingAddressManager.getCompanyDefaultShippingAddress(companyid, accountingHandlerDAOobj));

            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    /* Method is used to get Documents linked with RFQ in linking information report*/
    public ModelAndView getRFQLinkedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getRFQLinkedInTransaction(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderControllerCMN.getRequisitionLinkedInTransaction : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
        
    public JSONObject getRFQLinkedInTransaction(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();

        try {

            String rfqID = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateFormatter(request);

            DateFormat userdf = (DateFormat) authHandler.getUserDateFormatter(request);
            KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
            KWLCurrency currency = (KWLCurrency) curresult1.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(rfqID)) {

                /*
                 * Getting Purchase Requisition linked in RFQ
                 */
                Map requestparams = new HashMap();
                requestparams.put("rfqID", rfqID);
                requestparams.put("companyid", companyid);
                KwlReturnObject result = accPurchaseOrderobj.getPRLinkedInRFQ(requestparams);
                List invoiceList = result.getEntityList();

                if (invoiceList != null && invoiceList.size() > 0) {
                    int type = 7;
                    jArr = accPurchaseOrderServiceDAOobj.getPurchaseRequisitionJsonForLinking(jArr, invoiceList, currency, df, type);
                }
                
                /*
                 * Getting Vendor Quotation linked in RFQ
                 */
                result = accPurchaseOrderobj.getVQLinkedInRFQ(requestparams);
                invoiceList = result.getEntityList();

                if (invoiceList != null && invoiceList.size() > 0) {
                    int type = 7;
                    jArr = accSalesOrderServiceDAOobj.getVendorQuotationJsonForLinking(jArr, invoiceList, currency, df, companyid);
                }

                jobj.put("count", jArr.length());
                jobj.put("data", jArr);

            }

        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }

        return jobj;
    }

    public ModelAndView getPurchaseOrdersVersion(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            if (request.getParameter("isFixedAsset") != null && !StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) {
                boolean isFixedAsset = Boolean.FALSE.parseBoolean(requestParams.get("isFixedAsset").toString());
                requestParams.put("isFixedAsset", isFixedAsset);
            }
            if (request.getParameter("versionid") != null && !StringUtil.isNullOrEmpty(request.getParameter("versionid"))) {
                String versionid = request.getParameter("versionid");
                requestParams.put("versionid", versionid);
            }
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            KwlReturnObject result = accPurchaseOrderobj.getPurchaseOrderVersions(requestParams);
            requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(request));
            DataJArr = accPurchaseOrderServiceDAOobj.getPurchaseOrdersVersionJson(requestParams, result.getEntityList(), DataJArr);
            int cnt = result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            jobj.put("data", pagedJson);
            jobj.put("count", cnt);
        } catch (SessionExpiredException | ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getPurchaseOrders : "+ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
        public ModelAndView getPurchaseOrdersVersionRow(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            
            // Get line-level CUSTOMFIELDS for Purchase Order
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
             if (request.getParameter("bills") != null) {
                requestParams.put("bills", request.getParameter("bills").split(","));
            }
            if (request.getParameter("isJobWorkOutRemain") != null) {
                requestParams.put("isJobWorkOutRemain", request.getParameter("isJobWorkOutRemain"));
            }
            if (request.getParameter("prodIds") != null) {
                requestParams.put("prodIds", request.getParameterValues("prodIds"));
            }
            requestParams.put("closeflag", request.getParameter("closeflag"));
            requestParams.put("moduleid", request.getParameter("moduleid"));
            requestParams.put("doflag", request.getParameter("doflag")!=null?true:false);
            requestParams.put("dateFormatValue", authHandler.getDateOnlyFormat());
            boolean isConsignment= StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))?false:Boolean.parseBoolean(request.getParameter("isConsignment"));
            requestParams.put("isConsignment", isConsignment);
            boolean isForDOGROLinking = (StringUtil.isNullOrEmpty(request.getParameter("isForDOGROLinking")))?false:Boolean.parseBoolean(request.getParameter("isForDOGROLinking"));
            requestParams.put("isForDOGROLinking", isForDOGROLinking);
            boolean isForSGELinking=(StringUtil.isNullOrEmpty(request.getParameter("isForSGELinking")))?false:Boolean.parseBoolean(request.getParameter("isForSGELinking"));
            requestParams.put("isForSGELinking", isForSGELinking);
            boolean isForInvoice = (StringUtil.isNullOrEmpty(request.getParameter("isForInvoice")))?false:Boolean.parseBoolean(request.getParameter("isForInvoice"));
            requestParams.put("isForInvoice", isForInvoice);
            boolean sopolinkflag = (StringUtil.isNullOrEmpty(request.getParameter(Constants.POSOFLAG)))?false:Boolean.parseBoolean(request.getParameter(Constants.POSOFLAG));
            requestParams.put(Constants.POSOFLAG, sopolinkflag);
            boolean isFixedAsset = Boolean.FALSE.parseBoolean(request.getParameter("isFixedAsset"));
            requestParams.put("isFixedAsset", isFixedAsset);
            boolean FA_POlinkToFA_PI = Boolean.FALSE.parseBoolean(request.getParameter("FA_POlinkToFA_PI"));
            requestParams.put("FA_POlinkToFA_PI", FA_POlinkToFA_PI);
            boolean isForJobWorkOut=(StringUtil.isNullOrEmpty(request.getParameter("isForJobWorkOut")))?false:Boolean.parseBoolean(request.getParameter("isForJobWorkOut"));            
            requestParams.put("isForJobWorkOut", isForJobWorkOut);
            boolean isJobWorkOutLinkedWithGRN=(StringUtil.isNullOrEmpty(request.getParameter("isJobWorkOutLinkedWithGRN")))?false:Boolean.parseBoolean(request.getParameter("isJobWorkOutLinkedWithGRN"));                        
            requestParams.put("isJobWorkOutLinkedWithGRN", isJobWorkOutLinkedWithGRN);
            boolean isJobWorkOutLinkedWithPI=(StringUtil.isNullOrEmpty(request.getParameter("isJobWorkOutLinkedWithPI")))?false:Boolean.parseBoolean(request.getParameter("isJobWorkOutLinkedWithPI"));                        
            requestParams.put("isJobWorkOutLinkedWithPI", isJobWorkOutLinkedWithPI);
            boolean isForLinking=Boolean.FALSE.parseBoolean(request.getParameter("isForLinking"));
            requestParams.put("isForLinking", isForLinking);
            boolean isJobWorkStockOut = (StringUtil.isNullOrEmpty(request.getParameter("isJobWorkStockOut")))?false:Boolean.parseBoolean(request.getParameter("isJobWorkStockOut"));
            requestParams.put("isJobWorkStockOut", isJobWorkStockOut);
            String storeId=request.getParameter("storeId");
            if(!StringUtil.isNullOrEmpty(storeId)){
                requestParams.put("storeId", storeId);
            }
            
            String dtype = request.getParameter("dtype");
            
            boolean isForReport = false;
            
            if(!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")){
                isForReport = true;
            }
            requestParams.put("isForReport", isForReport);
            boolean isExpenseType = StringUtil.isNullOrEmpty(request.getParameter("isexpenseinv"))?false:Boolean.parseBoolean(request.getParameter("isexpenseinv"));// will be true for expense PO
            requestParams.put("isExpenseType", isExpenseType);
            
            boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag"))) ? false : Boolean.parseBoolean(request.getParameter("linkingFlag"));
            requestParams.put("linkingFlag", linkingFlag);
            if(request.getParameter("requestModuleid")!=null && !StringUtil.isNullOrEmpty(request.getParameter("requestModuleid"))){
                requestParams.put("requestModuleid",request.getParameter("requestModuleid"));
            }
              /*--- generateInvoiceFromTransactionForms ->Flag is true if invoice is generated from PO form------*/
            boolean generateInvoiceFromTransactionForms = (StringUtil.isNullOrEmpty(request.getParameter("generateInvoiceFromTransactionForms")))?false:Boolean.parseBoolean(request.getParameter("generateInvoiceFromTransactionForms"));
            /*
            'isSOfromPO' flag is used to get custom field or dimension while generating SO from PO
            */
            boolean isSOfromPO = (StringUtil.isNullOrEmpty(request.getParameter("isSOfromPO")))?false:Boolean.parseBoolean(request.getParameter("isSOfromPO"));
             requestParams.put("generateInvoiceFromTransactionForms",generateInvoiceFromTransactionForms);
             requestParams.put("isSOfromPO",isSOfromPO);
             JSONArray DataJArr = accPurchaseOrderServiceDAOobj.getPurchaseOrderVersionRows(requestParams);
            jobj.put("data", DataJArr);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getPurchaseOrderRows : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
        
    // Fecth Shipping Address of PO
    /*
     * Description: It is used to call the shipping address of PO used in module
     * PO and GRN Module for Multicompany Ticket:ERM-77
     */
    public ModelAndView fetchShippingAddressOfPO(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject returnJObj=new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        int moduleid=0;
        if(paramJobj.has(Constants.moduleid) && paramJobj.get(Constants.moduleid)!=null ){
          moduleid=Integer.parseInt(paramJobj.optString(Constants.moduleid));
        }
        
        //Callled in case of Purchase Order Module
        if(moduleid==Constants.Acc_Purchase_Order_ModuleId){
          returnJObj = accPurchaseOrderServiceDAOobj.fetchShippingAddress(paramJobj);
        }else{//Callled in case of GRN and GRN with PO
          returnJObj = accPurchaseOrderServiceDAOobj.fetchShippingAddressForGRN(paramJobj);
        }
        
        return new ModelAndView("jsonView", "model", returnJObj.toString());
    }  
}
