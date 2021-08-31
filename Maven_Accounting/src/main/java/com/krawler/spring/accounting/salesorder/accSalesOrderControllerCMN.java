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
package com.krawler.spring.accounting.salesorder;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.StorageHandler;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customer.accCustomerControllerCMN;
import com.krawler.spring.accounting.customreports.AccCustomReportService;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonEnglishNumberToWords;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.invoice.*;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.AccExportReportsServiceDAO;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.mainaccounting.service.AccCustomerMainAccountingService;
import com.krawler.spring.mrp.contractmanagement.AccContractManagementServiceDAO;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.LocalDate;
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
public class accSalesOrderControllerCMN extends MultiActionController implements MessageSourceAware{
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accReceiptDAO accReceiptDAOobj;
    private AccExportReportsServiceDAO accExportReportsServiceDAOobj;
    private AccExportReportsServiceDAO accExportOtherReportsServiceDAOobj;
    private accCurrencyDAO accCurrencyobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private accTaxDAO accTaxObj;
    private String successView;
    private MessageSource messageSource;
    private accAccountDAO accAccountDAOobj;
    private CustomDesignDAO customDesignDAOObj;
    private VelocityEngine velocityEngine;
    private CommonEnglishNumberToWords EnglishNumberToWordsOjb = new CommonEnglishNumberToWords();
    private authHandlerDAO authHandlerDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accProductDAO accProductObj;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private StoreService storeService;
    private fieldDataManager fieldDataManagercntrl;
    private auditTrailDAO auditTrailObj;
    private APICallHandlerService apiCallHandlerService;  
    private AccInvoiceServiceDAO accInvoiceServiceDAOObj;
    private AccContractManagementServiceDAO accContractManagementServiceDAOObj;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOobj;
    private AccCustomReportService accCustomReportService;
    private HibernateTransactionManager txnManager;
    private accSalesOrderService accSalesOrderServiceobj;
    private permissionHandlerDAO permissionHandlerDAOObj;
//    private accCustomerControllerCMN acCustomerControllerCMNObj;
    private CommonFnControllerService commonFnControllerService;
    private AccCustomerMainAccountingService accCustomerMainAccountingService;
    

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }
    
    public void setaccCustomerMainAccountingService(AccCustomerMainAccountingService accCustomerMainAccountingService) {
        this.accCustomerMainAccountingService = accCustomerMainAccountingService;
    }
    
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    public void setAccSalesOrderServiceobj(accSalesOrderService accSalesOrderServiceobj) {
        this.accSalesOrderServiceobj = accSalesOrderServiceobj;
    }

    public void setPermissionHandlerDAOObj(permissionHandlerDAO permissionHandlerDAOObj) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj;
    }

//    public void setAcCustomerControllerCMNObj(accCustomerControllerCMN acCustomerControllerCMNObj) {
//        this.acCustomerControllerCMNObj = acCustomerControllerCMNObj;
//    }
    
    public void setAccCustomReportService(AccCustomReportService accCustomReportService) {
        this.accCustomReportService = accCustomReportService;
    }
    
     public void setAccContractManagementServiceDAOObj(AccContractManagementServiceDAO accContractManagementServiceDAOObj) {
        this.accContractManagementServiceDAOObj = accContractManagementServiceDAOObj;
    }
    public void setAccInvoiceServiceDAOObj(AccInvoiceServiceDAO accInvoiceServiceDAOObj) {
        this.accInvoiceServiceDAOObj = accInvoiceServiceDAOObj;
    }

    public void setAccGoodsReceiptServiceDAOobj(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOobj) {
        this.accGoodsReceiptServiceDAOobj = accGoodsReceiptServiceDAOobj;
    }
    
    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
        
    public void setaccExportReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }
     public void setaccExportOtherReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportOtherReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }
    public void setaccSalesOrderServiceDAO(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }
    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    public void setcustomDesignDAO(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }
    public void setvelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}
        
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }   
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
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
      public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
       public void setAccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    
    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }
        public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }
        
    public ModelAndView getSalesOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
             KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            if (request.getParameter("requestModuleid") != null && !StringUtil.isNullOrEmpty(request.getParameter("requestModuleid"))) {
                int requestModuleID=Integer.parseInt(request.getParameter("requestModuleid"));
                if(extraCompanyPreferences.isEnableLinkToSelWin()){
                    requestParams.put("requestModuleid", requestModuleID);    
                }
                /*-------isGenerateOrderFromOrder comes true when Generating PO from SO-------*/
                boolean isGenerateOrderFromOrder = request.getParameter("isGenerateOrderFromOrder") != null ? Boolean.parseBoolean(request.getParameter("isGenerateOrderFromOrder")) : false;
                if (extraCompanyPreferences.isEnableLinkToSelWin() && !Boolean.parseBoolean(request.getParameter("isGrid")) && (requestModuleID == Constants.Acc_Invoice_ModuleId || requestModuleID == Constants.Acc_Delivery_Order_ModuleId || requestModuleID == Constants.Acc_Purchase_Order_ModuleId || requestModuleID == Constants.Acc_ConsignmentDeliveryOrder_ModuleId || requestModuleID == Constants.Acc_Lease_DO || requestModuleID == Constants.Acc_Lease_Contract)) {
                    requestParams.put("start", "0");
                    requestParams.put("limit", "10");
                    requestParams.put("dropDown", true);
                    if (isGenerateOrderFromOrder) {
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
            KwlReturnObject result = accSalesOrderDAOobj.getSalesOrders(requestParams);
//            JSONArray jarr = getSalesOrdersJson(request, result.getEntityList());
            JSONArray jarr = getSalesOrdersJsonOptimized(request, result.getEntityList());
                jobj.put("data", jarr);
                jobj.put("count", result.getRecordTotalCount());
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
    
    public ModelAndView getReplacementRequests(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONArray jarr = getReplacementRequestsJsonArray(request);
            jobj.put("data", jarr);
            jobj.put("count", jarr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getReplacementRequestsForReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONArray jarr = getReplacementRequestsForReportJsonArray(request);
            jobj.put("data", jarr);
            jobj.put("count", jarr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView exportReplacementRequestsForReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        request.setAttribute("isExport", true);
        String view = "jsonView_ex";
        try {
            JSONArray jarr = getReplacementRequestsForReportJsonArray(request);
            jobj.put("data", jarr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView getMaintenanceRequests(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONArray jarr = getMaintenanceRequests(request);
            jobj.put("data", jarr);
            jobj.put("count", jarr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getMaintenanceSchedulerReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONArray jarr = getMaintenanceSchedulerReport(request);
            jobj.put("data", jarr);
            jobj.put("count", jarr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    public JSONArray getMaintenanceSchedulerReport(HttpServletRequest request) {
        JSONArray returnArray = new JSONArray();
        try {
            JSONObject obj = new JSONObject();
            obj.put("scheduleId", "scheduleNumber");
            obj.put("scheduleNumber", "scheduleNumber");
            obj.put("startDate", new Date());
            obj.put("endDate", new Date());
            obj.put("actualStartDate", new Date());
            obj.put("actualEndDate", new Date());
            obj.put("workJobId", "workJobId");
            obj.put("assignedTo", "Atul");
            obj.put("status", "Un Assigned");
            obj.put("action", "action");
            returnArray.put(obj);


        } catch (JSONException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnArray;
    }

    public ModelAndView getMaintenanceRequestsForReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONArray jarr = getMaintenanceRequestsForReport(request);
            jobj.put("data", jarr);
            jobj.put("count", jarr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getMaintenanceRequestsForReport(HttpServletRequest request) {
        JSONArray returnArray = new JSONArray();
        try {
            HashMap<String, Object> requestMap = new HashMap<String, Object>();
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String customerId = request.getParameter("id");
            boolean isExport = false;
            requestMap.put("companyId", companyId);
            
            boolean isNormalContract = false;
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("isNormalContract"))){
                isNormalContract = Boolean.parseBoolean(request.getParameter("isNormalContract"));
            }
       
            if (request.getAttribute("isExport")!=null) {
                isExport = (Boolean)request.getAttribute("isExport");
            }
            requestMap.put("isNormalContract", isNormalContract);
            

            KwlReturnObject result = accSalesOrderDAOobj.getMaintenanceRequests(requestMap);
            List list = result.getEntityList();

            if (list != null && !list.isEmpty()) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    Maintenance maintenance = (Maintenance) it.next();

                    JSONObject obj = new JSONObject();
                    obj.put("maintenanceId", maintenance.getId());
                    obj.put("maintenanceNumber", maintenance.getMaintenanceNumber());
                    obj.put("customerName", (maintenance.getCustomer() != null) ? maintenance.getCustomer().getName() : "");
                    obj.put("contractName", (maintenance.getContract() != null) ? maintenance.getContract().getContractNumber() : "");
                    if (isExport) {
                        obj.put("status", maintenance.isClosed() ? "Close" : "Open");

                    } else {
                        obj.put("status", maintenance.isClosed());
                    }
                    //*** Attachments Documents SJ[ERP-16428] 
                    obj.put("billid", maintenance.getId());
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", maintenance.getId());
                    hashMap.put("companyid", maintenance.getCompany().getCompanyID());
                    KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    obj.put("attachment", attachemntcount);
                    //*** Attachments Documents SJ[ERP-16428]
                    returnArray.put(obj);
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnArray;
    }

     public ModelAndView exportMaintenanceRequestsForReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        request.setAttribute("isExport", true);
        String view = "jsonView_ex";
        try{
           JSONArray jarr = getMaintenanceRequestsForReport(request);
            jobj.put("data", jarr);
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
     
    public ModelAndView getSalesOrderByProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap requestParams = getSalesOrdersMap(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            requestParams.put("start", start);
            requestParams.put("limit", limit);
            KwlReturnObject result = accSalesOrderDAOobj.getSalesOrderByProduct(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();
            JSONArray DataJArr  = getSalesOrderByProductJson(request, list).getJSONArray("data");
            jobj.put("data", DataJArr);
            jobj.put("count", count);
            issuccess = true;
        } catch (Exception ex){
            msg = ""+ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView exportSalesOrderByProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap requestParams = getSalesOrdersMap(request);
            KwlReturnObject result = accSalesOrderDAOobj.getSalesOrderByProduct(requestParams);
            jobj = getSalesOrderByProductJson(request, result.getEntityList());
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
    
    public JSONObject getSalesOrderByProductJson(HttpServletRequest request, List<Object[]> list) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            HashMap requestParams = getSalesOrdersMap(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KWLCurrency kwlcurrency=(KWLCurrency) requestParams.get("kwlcurrency");
            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            for (Object[] oj : list) {
                if(oj[0]!=null){
                    String soid = oj[0].toString();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soid);
                    SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                    String currencyid = (salesOrder.getCurrency() == null ? currency.getCurrencyID() : salesOrder.getCurrency().getCurrencyID());
                    JSONObject obj = new JSONObject();
                    obj.put("billid", salesOrder.getID());
                    obj.put("customername", salesOrder.getCustomer() == null ? "" : salesOrder.getCustomer().getName());
                    obj.put("customerid", salesOrder.getCustomer() == null ? "" : salesOrder.getCustomer().getID());
                    obj.put("personname", salesOrder.getCustomer() == null ? "" : salesOrder.getCustomer().getName());
                    obj.put("personid", salesOrder.getCustomer() == null ? "" : salesOrder.getCustomer().getID());
                    obj.put(Constants.SEQUENCEFORMATID, salesOrder.getSeqformat() == null ? "" : salesOrder.getSeqformat().getID());
                    obj.put("lasteditedby", salesOrder.getModifiedby() == null ? "" : (salesOrder.getModifiedby().getFirstName() + " " + salesOrder.getModifiedby().getLastName()));
                    obj.put("shipdate", salesOrder.getShipdate() == null ? "" : df.format(salesOrder.getShipdate()));
                    obj.put("termid", salesOrder.getTerm() == null ? "" : salesOrder.getTerm().getID());
                    obj.put("duedate", salesOrder.getDueDate() == null ? "" : df.format(salesOrder.getDueDate()));
                    obj.put("memo", salesOrder.getMemo() == null ? "" : salesOrder.getMemo());
                    obj.put("shipvia", salesOrder.getShipvia() == null ? "" : salesOrder.getShipvia());
                    obj.put("fob", salesOrder.getFob() == null ? "" : salesOrder.getFob());
                    boolean includeprotax = false;
                    double rowTaxAmt = 0d;
                    double taxAmt = 0d;
                    Set<SalesOrderDetail> salesOrderDetails = salesOrder.getRows();
                    for (SalesOrderDetail salesOrderDetail : salesOrderDetails) {
                        if (salesOrderDetail.getTax() != null) {
                            includeprotax = true;
                            rowTaxAmt += salesOrderDetail.getRowTaxAmount();
                        }
                    }
                    taxAmt += rowTaxAmt;// either row level tax will be avvailable or invoice level
                    obj.put("taxamount", taxAmt);
                    obj.put("includeprotax", includeprotax);
                    obj.put("taxincluded", salesOrder.getTax() != null);
                    obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                    obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                    obj.put("salesPerson", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getID());
                    obj.put("salespersonname", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getValue());
                    obj.put("billno", salesOrder.getSalesOrderNumber());
                    obj.put("currencyid", currencyid);
                    obj.put("currencysymbol", (salesOrder.getCurrency() == null ? currency.getSymbol() : salesOrder.getCurrency().getSymbol()));
                    obj.put("currencycode", (salesOrder.getCurrency() == null ? currency.getCurrencyCode() : salesOrder.getCurrency().getCurrencyCode()));
                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, salesOrder.getOrderDate(), 0);
                    obj.put("oldcurrencyrate", (Double) bAmt.getEntityList().get(0));
                    obj.put("date", df.format(salesOrder.getOrderDate()));
                    /*
                    * For Product search, add Products details from SO details
                    */

                    String idvString = !StringUtil.isNullOrEmpty(oj[3].toString()) ? oj[3].toString() : ""; //as in list invoiedetail id comes 4th
                    KwlReturnObject objItrID = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), idvString);
                    SalesOrderDetail idvObj = (SalesOrderDetail) objItrID.getEntityList().get(0);
                    if (idvObj != null) {
                        obj.put("rowproductname", idvObj.getProduct().getName());
                        obj.put("rowproductid", idvObj.getProduct().getProductid());
                        obj.put("rowproductdescription", StringUtil.isNullOrEmpty(idvObj.getDescription()) ? "" : idvObj.getDescription());
                        double quantity = idvObj.getQuantity();
                        double discountPrice = 0, rowDiscountAmt = 0d;
                        obj.put("rowquantity", authHandler.formattedQuantity(quantity, companyid)); //To show quantity with four decimal point in PDF & Print
                        obj.put("rowrate", idvObj.getRate());
                        obj.put("unitpricecurrency", idvObj.getRate());
                        double sorate = idvObj.getRate();
                        double quotationPrice = quantity * sorate;
                        double discountSOD = idvObj.getDiscount();

                        if (idvObj.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountSOD / 100), companyid);
                            rowDiscountAmt += authHandler.round((quotationPrice * discountSOD / 100), companyid);
                        } else {
                            discountPrice = quotationPrice - discountSOD;
                            rowDiscountAmt += discountSOD;
                        }
                        double amountWithoutTax = authHandler.round((Double) discountPrice, companyid);
                        obj.put("amount", amountWithoutTax);
                        double amountInBase = (Double) accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountWithoutTax, currencyid, idvObj.getSalesOrder().getOrderDate(), idvObj.getSalesOrder().getExternalCurrencyRate()).getEntityList().get(0);
                        obj.put("amountinbase", amountInBase);
                        obj.put("amountinbasewithouttax", amountInBase);

                        double rowTaxPercent = 0;
                        double rowTaxAmount = 0;
                        if (idvObj.getTax() != null) {
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, salesOrder.getOrderDate(), idvObj.getTax().getID());
                            rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        }
                        double purchaseCostInBase=0;
                        if(!StringUtil.isNullOrEmpty(idvObj.getPurchaseorderdetailid())){
                            KwlReturnObject resObj = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), idvObj.getPurchaseorderdetailid());
                            PurchaseOrderDetail purchaseOrderDetail = (PurchaseOrderDetail) resObj.getEntityList().get(0);
                            if(purchaseOrderDetail!=null) {
                                double purchaseRate = purchaseOrderDetail.getRate();
                                double purchaseCost = purchaseRate*quantity;
                                purchaseCostInBase = (Double) accCurrencyobj.getCurrencyToBaseAmount(requestParams, purchaseCost, currencyid, purchaseOrderDetail.getPurchaseOrder().getOrderDate(), purchaseOrderDetail.getPurchaseOrder().getExternalCurrencyRate()).getEntityList().get(0);
                            }
                            obj.put("purchasecost", authHandler.round((Double) purchaseCostInBase, companyid));
                        }else{
                            KwlReturnObject prdPriceRes = accProductObj.getProductPrice(idvObj.getProduct().getID(), true, null, "", "");
                            double purchasePrice = prdPriceRes.getEntityList().get(0)!=null ? (Double)prdPriceRes.getEntityList().get(0) : 0;
                            double purchaseCost = purchasePrice*quantity;
                            purchaseCostInBase = authHandler.round((Double) purchaseCost, companyid);
                            obj.put("purchasecost", purchaseCostInBase );
                        }
                        double profitMargin = amountInBase - purchaseCostInBase;
                        obj.put("profitmargin", authHandler.round((Double) profitMargin, companyid) );
                        obj.put("rowprtaxpercent", rowTaxPercent);
                        rowTaxAmount = amountInBase * rowTaxPercent / 100;

                        if (rowTaxPercent > 0) {
                            obj.put("amountinbasewithtax", amountInBase + (rowTaxAmount));//obj.put("amountinbasewithtax", amountInBase + (amountInBase * rowTaxPercent/100));
                        } else {
                            obj.put("amountinbasewithtax", amountInBase);
                        }
                        if (extraCompanyPreferences.isActivateProfitMargin()) {
                            KwlReturnObject sodVenRes = accSalesOrderDAOobj.getSalesOrderDetailsVendorMapping(idvObj.getID());
                            if (sodVenRes.getEntityList().size() > 0) {
                                SODetailsVendorMapping sodVendObj = (SODetailsVendorMapping) sodVenRes.getEntityList().get(0);
                                obj.put("vendorname", sodVendObj.getVendor() != null ? sodVendObj.getVendor().getName() : "");
                                obj.put("vendorunitcost", sodVendObj.getUnitcost());
                                obj.put("profitmargin", authHandler.round((Double) amountInBase - sodVendObj.getTotalcost(), companyid));
                                obj.put("profitmarginpercent", authHandler.round((((Double) amountInBase - sodVendObj.getTotalcost()) * 100) / amountInBase, companyid) + "%");
                                obj.put("vendorcurrexchangerate", sodVendObj.getExchangerate());
                                obj.put("vendorcurrencyid", sodVendObj.getVendor() != null ? (sodVendObj.getVendor().getCurrency() != null ? sodVendObj.getVendor().getCurrency().getCurrencyID() : gcurrencyid) : "");
                                obj.put("vendorcurrencysymbol", sodVendObj.getVendor() != null ? (sodVendObj.getVendor().getCurrency() != null ? sodVendObj.getVendor().getCurrency().getSymbol() : kwlcurrency.getSymbol()) : "");
                                obj.put("totalcost", sodVendObj.getTotalcost());
                            }
                        }
                    }
                    requestParams.put("taxtype", 2);
                    double percent=0.0;
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List<Object[]> listTax = result.getEntityList();
                    if (listTax != null && !listTax.isEmpty()) {
                        for (Object[] row : listTax) {
                            if (row[2] == null) {
                                continue;
                            }
                            Tax tax = (Tax) row[0];
                            if (salesOrder.getTax() != null ? tax.getID().equals(salesOrder.getTax().getID()) : false) {
                                percent = (Double) row[1];
                            };
                        }
                    }
                    if (salesOrder.getTax() != null) {
                        double amountInBaseWithInLineTax = obj.getDouble("amountinbasewithtax");
                        double amount = obj.getDouble("amountinbasewithtax");
                        double taxAmount = amount * percent / 100;
                        if (taxAmount > 0) {
                            obj.put("amountinbasewithtax", amountInBaseWithInLineTax + (taxAmount));//obj.put("amountinbasewithtax", amountInBase + (amountInBase * rowTaxPercent/100));
                        } else {
                            obj.put("amountinbasewithtax", amountInBaseWithInLineTax);
                        }
                    }
                    HashMap<String, String> customFieldMap = new HashMap();
                    HashMap<String, String> customDateFieldMap = new HashMap();
                    DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Sales_Order_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    Map<String, Object> variableMap = new HashMap();
                    SalesOrderCustomData jeDetailCustom = (SalesOrderCustomData) salesOrder.getSoCustomData();
                    replaceFieldMap = new HashMap();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyid);
                        params.put(Constants.isExport, false);
                        params.put(Constants.userdf, userdf);

                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getSalesOrderByProductJson: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    public ModelAndView getSalesOrdersMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> requestParams = accSalesOrderServiceDAOobj.getSalesOrdersMapJson(paramJobj);
            String companyid = paramJobj.getString(Constants.companyKey);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has permission to view all customers documents,so at that time there is need to filter record according to user&salesperson. 
                     */
                    String userId =paramJobj.getString(Constants.useridKey);
                    requestParams.put(Constants.useridKey, userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
                Map<String, Object> salesPersonParams = new HashMap<>();

                salesPersonParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                salesPersonParams.put(Constants.companyKey,companyid);
                salesPersonParams.put("grID", "15");
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
            /* Getting relevant type Document Parameter 
            1.orderLinkedWithDocType=12-If SO not Linked with any DO or Invoice
            
            2.orderLinkedWithDocType=13-If SO  Linked with  Invoice Only
            
            3.orderLinkedWithDocType=14-If SO  Linked with DO Only
            
            4.orderLinkedWithDocType=15-If SO  Linked with  DO & Invoice both
            */
            int orderLinkedWithDocType = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("orderLinkedWithDocType"))) {
                orderLinkedWithDocType = Integer.parseInt(request.getParameter("orderLinkedWithDocType"));
            }
            requestParams.put("orderLinkedWithDocType", orderLinkedWithDocType);
            if (!StringUtil.isNullOrEmpty(request.getParameter("requestStatus"))) {
                requestParams.put("status", request.getParameter("requestStatus"));
            }
        
            boolean ispendingAproval = paramJobj.optString("ispendingapproval",null)!=null?Boolean.parseBoolean(paramJobj.optString("ispendingapproval","")):false;
            boolean consolidateFlag = paramJobj.optString("consolidateFlag",null)!=null?Boolean.parseBoolean(paramJobj.optString("consolidateFlag",null)):false;            
            String[] companyids = (consolidateFlag && paramJobj.optString("companyids",null)!=null)?paramJobj.optString("companyids","").split(","):companyid.split(",");
            String gcurrencyid = (consolidateFlag && paramJobj.optString(Constants.globalCurrencyKey,null)!=null)?paramJobj.getString(Constants.globalCurrencyKey):paramJobj.getString(Constants.globalCurrencyKey);            
            boolean salesPersonFilterFlag = paramJobj.optString("salesPersonFilterFlag",null)!=null?Boolean.parseBoolean(paramJobj.optString("salesPersonFilterFlag","")):false;
            boolean isForTemplate = false;
            boolean isConsignment = paramJobj.optString("isConsignment",null)!=null?Boolean.parseBoolean(paramJobj.optString("isConsignment","")):false;
            String billid = "";
            String dir = "";
            String sort = "";
            boolean requestStatusFlag= false;
            requestParams.put("ispendingAproval", ispendingAproval);
            if(!StringUtil.isNullOrEmpty(paramJobj.optString("isForTemplate",null))){
                isForTemplate = Boolean.parseBoolean(paramJobj.getString("isForTemplate"));
                requestParams.put("isForTemplate", isForTemplate);
            }
           /* when clicking on bulk invoice button to get SO in bulk invoice report tab */
            boolean bulkInv = request.getParameter("bulkInv") != null ? Boolean.parseBoolean((String) request.getParameter("bulkInv")) : false;
            if (bulkInv) {
                requestParams.put("bulkInv", bulkInv);
                paramJobj.put("bulkInv", bulkInv);
            }
            if(!StringUtil.isNullOrEmpty(paramJobj.optString("billid",null))){
                billid = paramJobj.getString("billid");
                requestParams.put("billId", billid);
            }
             if(!StringUtil.isNullOrEmpty(paramJobj.optString("dir",null)) && !StringUtil.isNullOrEmpty(paramJobj.optString("sort",null))){
                dir = paramJobj.getString("dir");
                 sort = paramJobj.getString("sort");
                   requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            if(consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }else if(isConsignment && !StringUtil.isNullOrEmpty(paramJobj.optString("requestStatus",null)) && !paramJobj.optString("requestStatus","All").equals("All")){
                requestStatusFlag=true;
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            
            if (salesPersonFilterFlag) {
                requestParams.put("salesPersonFilterFlag", salesPersonFilterFlag);
            }
            
            if (isConsignment) {
                requestParams.put("isConsignment", isConsignment);
            }
            if(!StringUtil.isNullOrEmpty(paramJobj.optString("linknumber",null))){
                requestParams.put("linknumber", paramJobj.getString("linknumber")); 
            }
            requestParams.put("userId",paramJobj.getString(Constants.useridKey));
            
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), paramJobj.getString(Constants.useridKey));
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            
            // get TradingFlow indicator to determine which tables to be used for Outstanding Sales Orders report
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            Boolean isTradingFlow = false;
            if (pref!=null && pref.isWithInvUpdate())
            	isTradingFlow = true;            
            requestParams.put("isTradingFlow", isTradingFlow);
            KwlReturnObject result = null;
            companyid = "";
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];                
                paramJobj.put(Constants.companyKey, companyid);
                paramJobj.put(Constants.globalCurrencyKey, gcurrencyid);
                paramJobj.put(Constants.REQ_startdate, requestParams.get(Constants.REQ_startdate));
                paramJobj.put(Constants.REQ_enddate, requestParams.get(Constants.REQ_enddate));
                paramJobj.put(Constants.df, requestParams.get(Constants.df));
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
                boolean isOutstanding = paramJobj.optString("isOutstanding",null)!=null?Boolean.parseBoolean(paramJobj.getString("isOutstanding")):false;
                boolean isOutstandingproduct = paramJobj.optString("isOuststandingproduct",null)!=null?Boolean.parseBoolean(paramJobj.getString("isOuststandingproduct")):false; 
                boolean isUnInvoiced = request.getParameter("isUnInvoiced")!=null?Boolean.parseBoolean(request.getParameter("isUnInvoiced")):false;
                boolean isPendingInvoiced = request.getParameter("isPendingInvoiced")!=null?Boolean.parseBoolean(request.getParameter("isPendingInvoiced")):false;
                boolean outStandingSOfromReportList = request.getParameter("isfromReportList")!=null?Boolean.parseBoolean(request.getParameter("isfromReportList")):false;
                /* If request is called from create Bulk DO report from Outstanding SO(s)*/
                if (outStandingSOfromReportList) {
                    paramJobj.put("isOutstanding", "true");
                }
                requestParams.put("isOutstanding", isOutstanding);
                requestParams.put("isPendingInvoiced", isPendingInvoiced);
                requestParams.put("isOuststandingproduct", isOutstandingproduct);
                
                //ERP-41133:Outstanding SO Issue
                boolean checkServiceProductFlag = true; //by default check true in extracompanypreferences to check both service and inventory type
                JSONObject jObj = new JSONObject();
                if (!StringUtil.isNullOrEmpty(extraPref.getColumnPref())) {
                    jObj = new JSONObject((String) extraPref.getColumnPref());
                    if (jObj.has(Constants.columnPref.undeliveredServiceSOOpen.get()) && jObj.get(Constants.columnPref.undeliveredServiceSOOpen.get()) != null) {
                        checkServiceProductFlag = jObj.optBoolean(Constants.columnPref.undeliveredServiceSOOpen.get());
                    }
                }
                requestParams.put("checkServiceProductFlag", checkServiceProductFlag);
                /* "isUnInvoiced" Flag is true if  it is called for bulk invoice report*/
                if (!isUnInvoiced) {

                    if (orderLinkedWithDocType != 0) {
                        result = accSalesOrderDAOobj.getRelevantSalesOrderLinkingWise(requestParams);
                    } else if (!isOutstanding && !isOutstandingproduct && !isPendingInvoiced) {
                        result = accSalesOrderDAOobj.getSalesOrdersMerged(requestParams);
                    } else {

                        result = accSalesOrderDAOobj.getOutstandingSalesOrders(requestParams);
                    }
                } else {

                    if (outStandingSOfromReportList) {
                        /* Get SO without DO & Invoice i.e not liked with anyone*/
                        result = accSalesOrderDAOobj.getOutstandingSalesOrders(requestParams);
                    } else {
                        /* Get SO without DO & Invoice i.e not liked with anyone*/
                        result = accSalesOrderDAOobj.getUnInvoicedSalesOrders(requestParams);
                    }

                }
                                    
                DataJArr = accSalesOrderServiceDAOobj.getSalesOrdersJsonMerged(paramJobj, result.getEntityList(), DataJArr);
            }
                                 
            int cnt = consolidateFlag ||requestStatusFlag ?DataJArr.length():result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            if(consolidateFlag) {
                String start = paramJobj.optString(Constants.start,null);
                String limit = paramJobj.optString(Constants.limit,null);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            if(requestStatusFlag){
                String start = paramJobj.optString(Constants.start,null);
                String limit = paramJobj.optString(Constants.limit,null);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            
            if (extraPref != null && extraPref.isUpsIntegration()) {
                //If UPS integration is enabled, this code is used to calculate and update Shipping cost when Shipping cost is estimated for Sales Order
                JSONObject dataJobjWithShippingCost = accSalesOrderServiceDAOobj.calculateAndUpdateTotalShippingCost(pagedJson, paramJobj);
                jobj.put("data", dataJobjWithShippingCost.optJSONArray(Constants.data));
                jobj.put("upsErrorJSON", dataJobjWithShippingCost.optJSONObject("upsErrorJSON"));
            } else {
                jobj.put("data", pagedJson);
            }
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
    
    
        
    public ModelAndView getOpeningBalanceSalesOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        int count=0;
        boolean issuccess = false;
        String msg = "";
        try {
            String[] companyids = sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);

            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }


            String customerId = request.getParameter("custVenId");
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
//                requestParams.put("datefilter", request.getParameter("datefilter"));
                requestParams.put("customerid", customerId);
                requestParams.put("start", request.getParameter("start"));
                requestParams.put("limit", request.getParameter("limit"));
                KwlReturnObject result = accSalesOrderDAOobj.getOpeningBalanceSalesOrders(requestParams);
                List<SalesOrder> list = result.getEntityList();
                count =result.getRecordTotalCount();
                DataJArr = getOpeningBalanceSOJson(request, list, DataJArr);
            }

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
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public JSONArray getOpeningBalanceSOJson(HttpServletRequest request, List<SalesOrder> list, JSONArray dataArray) {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            HashMap requestParams = getSalesOrdersMap(request);

            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            for (SalesOrder salesOrder : list) {
                JSONObject soJson = new JSONObject();
                soJson.put("transactionId", salesOrder.getID());
                soJson.put("transactionNo", salesOrder.getSalesOrderNumber());

                KWLCurrency currency = null;
                if (salesOrder.getCurrency() != null) {
                    currency = salesOrder.getCurrency();
                } else {
                    currency = salesOrder.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : salesOrder.getCustomer().getAccount().getCurrency();
                }


                Iterator itrRow = salesOrder.getRows().iterator();
                double amount = 0, totalDiscount = 0, discountPrice = 0;
                while (itrRow.hasNext()) {
                    SalesOrderDetail sod = (SalesOrderDetail) itrRow.next();
                    double rowTaxPercent = 0;
                    if (sod.getTax() != null) {
                        requestParams.put("transactiondate", salesOrder.getOrderDate());
                        requestParams.put("taxid", sod.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    }

                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, sod.getRate(), currency.getCurrencyID(), salesOrder.getOrderDate(), 0);
                    double sorate = sod.getRate();//double sorate=(Double) bAmt.getEntityList().get(0);

                    double quotationPrice = sod.getQuantity() * sorate;
                    if (sod.getDiscountispercent() == 1) {
                        discountPrice = (quotationPrice) - (quotationPrice * sod.getDiscount() / 100);
                    } else {
                        discountPrice = quotationPrice - sod.getDiscount();
                    }

                    amount += discountPrice + sod.getRowTaxAmount();//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                }

                if (salesOrder.getDiscount() != 0) {
                    if (salesOrder.isPerDiscount()) {
                        totalDiscount = amount * salesOrder.getDiscount() / 100;
                        amount = amount - totalDiscount;
                    } else {
                        amount = amount - salesOrder.getDiscount();
                        totalDiscount = salesOrder.getDiscount();
                    }
                }

                double totalTermAmount = 0;
                HashMap<String, Object> requestParam = new HashMap();
                requestParam.put("salesOrder", salesOrder.getID());
                KwlReturnObject salesOrderResult = null;
                salesOrderResult = accSalesOrderDAOobj.getSalesOrderTermMap(requestParam);
                List<SalesOrderTermMap> termMap = salesOrderResult.getEntityList();
                for (SalesOrderTermMap salesOrderTermMap : termMap) {
                    InvoiceTermsSales mt = salesOrderTermMap.getTerm();
                    double termAmnt = salesOrderTermMap.getTermamount();
                    totalTermAmount += termAmnt;
                }


                KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getOrderDate(), 0);

                double taxPercent = 0;
                if (salesOrder.getTax() != null) {
                    requestParams.put("transactiondate", salesOrder.getOrderDate());
                    requestParams.put("taxid", salesOrder.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    if (taxList != null && !taxList.isEmpty()) { 
                        Object[] taxObj = (Object[]) taxList.get(0);
                        taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    }
                }
                double orderAmount = amount;//(Double) bAmt.getEntityList().get(0);
                double ordertaxamount = (taxPercent == 0 ? 0 : orderAmount * taxPercent / 100);

                amount += totalTermAmount;
                orderAmount += totalTermAmount;

                soJson.put("transactionDate", df.format(salesOrder.getOrderDate()));
                soJson.put("transactionAmount", orderAmount + ordertaxamount);
                soJson.put("currencysymbol", currency.getSymbol());
                soJson.put("currencyid", currency.getCurrencyID());

                dataArray.put(soJson);
            }


        } catch (ServiceException ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }
    
    public HashMap<String, Object> getSalesOrdersMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            requestParams.put(Constants.start, request.getParameter(Constants.start));
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        }
        requestParams.put(Constants.df,authHandler.getDateOnlyFormat(request));
        requestParams.put(Constants.userdf,authHandler.getUserDateFormatterWithoutTimeZone(request));
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_customerId,request.getParameter(Constants.REQ_customerId));        
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
        requestParams.put(Constants.MARKED_FAVOURITE ,request.getParameter(Constants.MARKED_FAVOURITE));
        requestParams.put(InvoiceConstants.newcustomerid, request.getParameter(InvoiceConstants.newcustomerid));
        requestParams.put(InvoiceConstants.productid, request.getParameter(InvoiceConstants.productid));
        requestParams.put(InvoiceConstants.productCategoryid, request.getParameter(InvoiceConstants.productCategoryid));
        requestParams.put(Constants.isRepeatedFlag, request.getParameter(Constants.isRepeatedFlag));
        requestParams.put(Constants.userid, request.getParameter(InvoiceConstants.userid));//putting userid to export number of users
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put("orderforcontract", request.getParameter("orderForContract")!=null?Boolean.parseBoolean(request.getParameter("orderForContract")):false);
        requestParams.put(Constants.ValidFlag, request.getParameter(Constants.ValidFlag));
        requestParams.put(Constants.BillDate ,request.getParameter(Constants.BillDate));
        requestParams.put("pendingapproval" ,(request.getParameter("pendingapproval") != null)? Boolean.parseBoolean(request.getParameter("pendingapproval")): false);
        requestParams.put("istemplate" ,(request.getParameter("istemplate") != null)? Integer.parseInt(request.getParameter("istemplate")): 0);
        requestParams.put("currencyid",request.getParameter("currencyid"));
        requestParams.put("exceptFlagINV" ,request.getParameter("exceptFlagINV"));
        requestParams.put("exceptFlagORD" ,request.getParameter("exceptFlagORD"));
        requestParams.put("linkFlagInSO" ,request.getParameter("linkFlagInSO"));
        requestParams.put("linkFlagInInv" ,request.getParameter("linkFlagInInv"));
        requestParams.put("linkflag" ,request.getParameter("linkflag"));
        requestParams.put(Constants.Acc_Search_Json ,request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria ,request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid ,request.getParameter(Constants.moduleid));
        requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null)? "" : request.getParameter("currencyfilterfortrans"));
        requestParams.put("isOpeningBalanceOrder", request.getParameter("isOpeningBalanceOrder")!=null?Boolean.parseBoolean(request.getParameter("isOpeningBalanceOrder")):false);
        requestParams.put("isLeaseFixedAsset", request.getParameter("isLeaseFixedAsset")!=null?Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")):false);
        requestParams.put("isConsignment", request.getParameter("isConsignment")!=null?Boolean.parseBoolean(request.getParameter("isConsignment")):false);
        requestParams.put("isMRPSalesOrder", request.getParameter("isMRPSalesOrder")!=null?Boolean.parseBoolean(request.getParameter("isMRPSalesOrder")):false);
        requestParams.put("isJobWorkOrderReciever", request.getParameter("isJobWorkOrderReciever")!=null?Boolean.parseBoolean(request.getParameter("isJobWorkOrderReciever")):false);
        requestParams.put("custWarehouse", (request.getParameter("custWarehouse") == null)? "" : request.getParameter("custWarehouse"));
        requestParams.put("movementtype", (request.getParameter("movementtype") == null)? "" : request.getParameter("movementtype"));
        requestParams.put(CCConstants.REQ_customerId,request.getParameter(CCConstants.REQ_customerId));
        requestParams.put(Constants.customerCategoryid, request.getParameter(Constants.customerCategoryid));
        requestParams.put("billId",request.getParameter("billid"));
        requestParams.put("isJobWorkINReciever", request.getParameter("isJobWorkInReciever")!=null?Boolean.parseBoolean(request.getParameter("isJobWorkInReciever")):false);
        requestParams.put("blockedDocuments",request.getParameter("blockedDocuments"));
        requestParams.put("unblockedDocuments",request.getParameter("unblockedDocuments"));
        requestParams.put(Constants.checksoforcustomer,StringUtil.isNullOrEmpty(request.getParameter(Constants.checksoforcustomer)) ? false : Boolean.parseBoolean(request.getParameter(Constants.checksoforcustomer)));
        if(request.getParameter("includingGSTFilter")!=null){
            requestParams.put("includingGSTFilter",Boolean.parseBoolean(request.getParameter("includingGSTFilter")));
        }
        if(request.getParameter("requestModuleid")!=null){
            requestParams.put("requestModuleid",Integer.parseInt(request.getParameter("requestModuleid")));
        }
        if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
            requestParams.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
        }
        requestParams.put("isDraft", (request.getParameter("isDraft") != null) ? Boolean.parseBoolean(request.getParameter("isDraft")) : false);    
        return requestParams;
    }
    
    public JSONArray getReplacementRequestsJsonArray(HttpServletRequest request) {
        JSONArray returnArray = new JSONArray();
        try {
            HashMap<String, Object> requestMap = new HashMap<String, Object>();
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String customerId = request.getParameter("id");
            
            boolean isNormalContract = false;
            boolean isForQuotation = false;
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("isForQuotation"))) {
                isForQuotation = Boolean.parseBoolean(request.getParameter("isForQuotation"));
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNormalContract"))) {
                isNormalContract = Boolean.parseBoolean(request.getParameter("isNormalContract"));
            }

            requestMap.put("companyId", companyId);
            requestMap.put("isNormalContract", isNormalContract);
            requestMap.put("customerId", customerId);

            KwlReturnObject result = accSalesOrderDAOobj.getReplacementRequests(requestMap);
            List list = result.getEntityList();

            if (list != null && !list.isEmpty()) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    ProductReplacement pr = (ProductReplacement) it.next();

                    JSONObject obj = new JSONObject();
                    obj.put("billid", pr.getId());
                    obj.put("personid", (pr.getCustomer() != null) ? pr.getCustomer().getID() : "");
                    obj.put("billno", pr.getReplacementRequestNumber());
//                    obj.put("replacementQuantity", pr.getReplacementQuantity());
//                    obj.put("isAsset", pr.isIsAsset());
//                    obj.put("batchSerialId", (pr.getBatchSerial() != null) ? pr.getBatchSerial().getId() : "");
                    obj.put("contractgetQuotationRowsId", (pr.getContract() != null) ? pr.getContract().getID() : "");
//                    obj.put("duedate", authHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
//                    obj.put("date", authHandler.getDateFormatter(request).format(salesOrder.getOrderDate()));
//                    obj.put("shipdate", salesOrder.getShipdate()==null? "" : authHandler.getDateFormatter(request).format(salesOrder.getShipdate()));
//                    obj.put("shipvia", salesOrder.getShipvia()== null? "" : salesOrder.getShipvia());
//                    obj.put("fob", salesOrder.getFob() == null?"":salesOrder.getFob());
//                    obj.put("amount", amount);
//                    obj.put("amountinbase", amount);
//                    obj.put("discount", totalDiscount);
//                    obj.put("discountispertotal", salesOrder.isPerDiscount());
//                    obj.put("taxpercent", taxPercent);
//                    obj.put("taxamount",ordertaxamount );
//                    obj.put("orderamount",orderAmount );
//                    obj.put("orderamountwithTax",orderAmount+ordertaxamount);
//                    obj.put("currencyid",currency.getCurrencyID());
//                    obj.put("personname", customer.getName());
//                    obj.put("memo", salesOrder.getMemo());
//                    obj.put("costcenterid", salesOrder.getCostcenter()==null?"":salesOrder.getCostcenter().getID());
//                    obj.put("costcenterName", salesOrder.getCostcenter()==null?"":salesOrder.getCostcenter().getName());
//                    obj.put("status",status); 
                    
                    // if quotation has been created of a product replacement request then it will not be visible in sales order
                    // and if sales order has been created of selected product replacement request then it will not be visible to 
                    // quotation form
                    
                    String status = "";
                    
                    if(isForQuotation){
                        // Check whether SO is created by selecting PR or not
                        boolean isSalesOrderCreatedBySelectedPR = isSalesOrderCreatedBySelectedPR(pr);
                        if(isSalesOrderCreatedBySelectedPR){
                            status = "Closed";
                        }else{
                            status = getProductReplacementRequestStatusForQuotation(pr);
                        }
                    }else{
                        // Check whether Quotation is created by selecting PR or not
                        
                        boolean isQuotationCreatedBySelectedPR = isQuotationCreatedBySelectedPR(pr);
                        if(isQuotationCreatedBySelectedPR){
                            status = "Closed";
                        }else{
                            status = getProductReplacementRequestStatusForSO(pr);
                        }
                    }
                    
                    
                    obj.put("status", status);
                    boolean addFlag = true;
//                    if(closeflag && salesOrder.isDeleted()){
//                        addFlag = false;
//                    }
                    if (status.equalsIgnoreCase("Closed")) {
                        addFlag = false;
                    }
                    if (addFlag) {
                        returnArray.put(obj);
                    }

                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnArray;
    }
    
    public JSONArray getReplacementRequestsForReportJsonArray(HttpServletRequest request) {
        JSONArray returnArray = new JSONArray();
        try {
            HashMap<String, Object> requestMap = new HashMap<String, Object>();
            String companyId = sessionHandlerImpl.getCompanyid(request);
            boolean isNormalContract = false;
            boolean isExport = false;
          
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNormalContract"))) {
                isNormalContract = Boolean.parseBoolean(request.getParameter("isNormalContract"));
            }
            if (request.getAttribute("isExport")!=null) {
                isExport = (Boolean)request.getAttribute("isExport");
            }

            requestMap.put("companyId", companyId);
            requestMap.put("isNormalContract", isNormalContract);

            KwlReturnObject result = accSalesOrderDAOobj.getReplacementRequests(requestMap);
            List list = result.getEntityList();

            if (list != null && !list.isEmpty()) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    ProductReplacement pr = (ProductReplacement) it.next();
                    JSONObject obj = new JSONObject();
                    obj.put("replacementId", pr.getId());
                    if (isExport) {
                        obj.put("status", pr.isClosed() ? "Close" : "Open");

                    } else {
                        obj.put("status", pr.isClosed());
                    }
                    obj.put("customerName", (pr.getCustomer() != null) ? pr.getCustomer().getName() : "");
                    obj.put("replacementNumber", pr.getReplacementRequestNumber());
                    obj.put("contractName", (pr.getContract() != null) ? pr.getContract().getContractNumber() : "");
                    //*** Attachments Documents SJ[ERP-16428] 
                    obj.put("billid", pr.getId());
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", pr.getId());
                    hashMap.put("companyid", pr.getCompany().getCompanyID());
                    KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    obj.put("attachment", attachemntcount);
                    obj.put("description",(pr.getDescription()!=null)? pr.getDescription():"");
                    //*** Attachments Documents SJ[ERP-16428]
                    returnArray.put(obj);
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnArray;
    }
    
    public JSONArray getMaintenanceRequests(HttpServletRequest request) {
        JSONArray returnArray = new JSONArray();
        try {
            HashMap<String, Object> requestMap = new HashMap<String, Object>();
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String customerId = request.getParameter("id");

            requestMap.put("companyId", companyId);
            requestMap.put("customerId", customerId);
            requestMap.put("exclusedClosed", true);
            if (!StringUtil.isNullOrEmpty(request.getParameter("soId"))) {// In case of edit SO if maintenance is checked then linked maintenance id also need to show in dropdown
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), request.getParameter("soId"));
                SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                String maintenanceIdForSo = (salesOrder.getMaintenance() != null) ? salesOrder.getMaintenance().getId() : "";
                requestMap.put("soId", request.getParameter("soId"));
                requestMap.put("maintenanceIdForSo", maintenanceIdForSo);
            }
            KwlReturnObject result = accSalesOrderDAOobj.getMaintenanceRequests(requestMap);
            List list = result.getEntityList();

            if (list != null && !list.isEmpty()) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    Maintenance maintenance = (Maintenance) it.next();

                    JSONObject obj = new JSONObject();
                    obj.put("billid", maintenance.getId());
                    obj.put("personid", (maintenance.getCustomer() != null) ? maintenance.getCustomer().getID() : "");
                    obj.put("billno", maintenance.getMaintenanceNumber());
                    String status = "";
                    //check whether quotation is created for maintenance
                    KwlReturnObject idresult = accInvoiceDAOobj.getMaintenanceFromQuotation(maintenance.getId(), companyId);
                    List list1 = idresult.getEntityList();
                    Iterator ite1 = list1.iterator();
                    if (ite1.hasNext()) {
                        status = "Closed";
                    }
                    boolean addFlag = true;

                    if (status.equalsIgnoreCase("Closed")) {
                        addFlag = false;
                    }
                    if (addFlag) {
                        returnArray.put(obj);
                    }
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnArray;
    }
    
    public JSONArray getSalesOrdersJson(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean closeflag = Boolean.parseBoolean(request.getParameter("closeflag"));
            boolean exceptFlagINV = Boolean.parseBoolean(request.getParameter("exceptFlagINV"));
            boolean doflag = request.getParameter("doflag")!=null?true:false;
            boolean isLeaseSO= Boolean.FALSE.parseBoolean(request.getParameter("isLeaseFixedAsset"));
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(),sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            boolean isConsignment = request.getParameter("isConsignment")!=null?Boolean.parseBoolean(request.getParameter("isConsignment")):false;
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request),isLeaseSO ?Constants.Acc_Lease_Order_ModuleId:isConsignment?Constants.Acc_ConsignmentRequest_ModuleId:Constants.Acc_Sales_Order_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                SalesOrder salesOrder=(SalesOrder)itr.next();
                KWLCurrency currency = null;
                
                if(salesOrder.getCurrency() != null){
                    currency = salesOrder.getCurrency();
                } else {
                    currency=salesOrder.getCustomer().getAccount().getCurrency()==null?kwlcurrency:salesOrder.getCustomer().getAccount().getCurrency();
                }
                    
                    Customer customer=salesOrder.getCustomer();
                    boolean isRequestPending=false;
                    boolean isRequestApproved=false;
                    boolean isRequestRejected=false;
                    boolean isquantityapproved=false;
                    JSONObject obj = new JSONObject();
                    obj.put("billid", salesOrder.getID());
                    obj.put("personid", customer.getID());
                    obj.put("billno", salesOrder.getSalesOrderNumber());          
                    obj.put("duedate", authHandler.getDateOnlyFormat(request).format(salesOrder.getDueDate()));
                    obj.put("date", authHandler.getDateOnlyFormat(request).format(salesOrder.getOrderDate()));
                    obj.put("shipdate", salesOrder.getShipdate()==null? "" : authHandler.getDateOnlyFormat(request).format(salesOrder.getShipdate()));
                    obj.put("shipvia", salesOrder.getShipvia()== null? "" : salesOrder.getShipvia());
                    obj.put("fob", salesOrder.getFob() == null?"":salesOrder.getFob());
                    obj.put("islockQuantityflag", salesOrder.isLockquantityflag());
                    obj.put("externalcurrencyrate", salesOrder.getExternalCurrencyRate());
                    obj.put("termdetails", accSalesOrderServiceDAOobj.getTermDetails(salesOrder.getID(),true));
                    if(salesOrder.getTermsincludegst()!=null) {
                        obj.put(Constants.termsincludegst, salesOrder.getTermsincludegst());
                    }
                    obj.put("movementtype", salesOrder.getMovementType()!=null ? salesOrder.getMovementType().getID() : "");
                    obj.put("shiplengthval", salesOrder.getShiplength());
                    obj.put("customerporefno", salesOrder.getCustomerPORefNo());
                    Iterator itrRow = salesOrder.getRows().iterator();
                    double amount = 0,totalDiscount = 0, discountPrice = 0;
                    double rejectedrowCount=0;
                    while (itrRow.hasNext()) {
                        SalesOrderDetail sod= (SalesOrderDetail) itrRow.next();
                        amount+=authHandler.round(sod.getQuantity()*sod.getRate(), companyid);
                        double  rowTaxPercent=0;
                        if(sod.getTax()!=null){
                            requestParams.put("transactiondate", salesOrder.getOrderDate());
                            requestParams.put("taxid", sod.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            if (taxList != null && !taxList.isEmpty()) { 
                             Object[] taxObj=(Object[]) taxList.get(0);
                             rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            }
                        }
                        double rejectedCount=0;
                        rejectedrowCount=0;
                        double quantityCount=0;
                        double approvedQuantity=0;
//                        if(isConsignment && salesOrder.isLockquantityflag() && sod.getProduct()!=null && sod.getProduct().isIsSerialForProduct()){
//                            KwlReturnObject result=accSalesOrderDAOobj.getSerialsFormDocumentid(sod.getID(), sod.getCompany().getCompanyID());
//                            Iterator serialItr=result.getEntityList().iterator();                                                     
//                            while(serialItr.hasNext()){
//                                    quantityCount++;
//                                SerialDocumentMapping documentMapping=(SerialDocumentMapping) serialItr.next();
//                                if(documentMapping !=null && documentMapping.getRequestApprovalStatus()== RequestApprovalStatus.PENDING){
//                                    isRequestPending=true;
////                                    break;
//                                 }else if(documentMapping !=null && documentMapping.getRequestApprovalStatus()== RequestApprovalStatus.APPROVED){
//                                    isRequestApproved=true;
//                                    approvedQuantity+=documentMapping.getSerialid()!=null?documentMapping.getSerialid().getQuantity():1;
////                                    break;
//                                }else if(documentMapping !=null && documentMapping.getRequestApprovalStatus()== RequestApprovalStatus.REJECTED){
//                                    rejectedCount+=documentMapping.getSerialid()!=null?documentMapping.getSerialid().getQuantity():1;
////                                    rejectedrowCount+=rejectedCount;
//                                    }
//
//                                }
//                            if(quantityCount==0 || rejectedCount==sod.getBaseuomquantity()){
//                                isRequestRejected=true;
//                            }else {
//                                isRequestRejected=false;
//                            }
////                            break;    //Removed as Partial Rquest Comes for Linking. 
//                        } //else 
                        if (isConsignment && salesOrder.isAutoapproveflag() ) {
                            approvedQuantity += sod.getApprovedQuantity();
                        }
                        if (approvedQuantity > 0) {
                            isquantityapproved = true;
                        }
//                        amount+=sod.getQuantity() *sod.getRate()*rowTaxPercent/100;

//                        KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, sod.getRate(), currency.getCurrencyID(), salesOrder.getOrderDate(), 0);
//                        double sorate=(Double) bAmt.getEntityList().get(0);

                        double quotationPrice = sod.getQuantity() * sod.getRate();
                        if(sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - (quotationPrice * sod.getDiscount()/100);
                        } else {
                            discountPrice = quotationPrice - sod.getDiscount();
                        }

                        amount += discountPrice + (discountPrice * rowTaxPercent/100);
                    }
                    obj.put("amount", amount);
                    obj.put("amountinbase", amount);
                    if(salesOrder.getDiscount() != 0){
                	if(salesOrder.isPerDiscount()){
                		totalDiscount = amount * salesOrder.getDiscount()/100;
                		amount = amount - totalDiscount ;
                	}else{
                		amount = amount - salesOrder.getDiscount();
                		totalDiscount = salesOrder.getDiscount();
                	}
                        obj.put("discounttotal",salesOrder.getDiscount());
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    obj.put("discount", totalDiscount);
                    obj.put("discountispertotal", salesOrder.isPerDiscount());
    //                    obj.put("orderamount", CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currency.getCurrencyID(),salesOrder.getOrderDate()));
                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getOrderDate(), 0);
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("taxid", salesOrder.getTax()==null?"":salesOrder.getTax().getID());
                    obj.put("taxname", salesOrder.getTax()==null?"":salesOrder.getTax().getName());
                    double  taxPercent=0;
                    if(salesOrder.getTax()!=null){
                        requestParams.put("transactiondate", salesOrder.getOrderDate());
                        requestParams.put("taxid", salesOrder.getTax().getID());
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
                    obj.put("currencyid",currency.getCurrencyID());
                    obj.put("personname", customer.getName());
                    obj.put("contractstatus", (salesOrder.getContract()!=null)?salesOrder.getContract().getCstatus():"");
                    
                    String contractId = "";
                    if (salesOrder.getContract() != null) {
                        contractId = salesOrder.getContract().getID();
                    }
                     
                    obj.put("contract", contractId);
                    
                    obj.put("memo", salesOrder.getMemo());
                    obj.put("isConsignment", salesOrder.isIsconsignment());
                    if (salesOrder.getCustWarehouse() != null) {
                    obj.put("custWarehouse", salesOrder.getCustWarehouse().getId());
                   }
                if (salesOrder.isIsconsignment()) {
                    obj.put("todate", salesOrder.getTodate() == null ? "" : authHandler.getDateOnlyFormat(request).format(salesOrder.getTodate()));
                    obj.put("fromdate", salesOrder.getFromdate() == null ? "" : authHandler.getDateOnlyFormat(request).format(salesOrder.getFromdate()));
                }
                    obj.put("costcenterid", salesOrder.getCostcenter()==null?"":salesOrder.getCostcenter().getID());
                    obj.put("costcenterName", salesOrder.getCostcenter()==null?"":salesOrder.getCostcenter().getName());
                    String status = "";
                    if(exceptFlagINV) {
                        Iterator itr1 = salesOrder.getRows().iterator();
                        status = "Closed";
                        while(itr1.hasNext()) {
                            SalesOrderDetail row = (SalesOrderDetail) itr1.next();
                            double addobj = doflag ? accSalesOrderServiceDAOobj.getSalesOrderDetailStatusForDO(row) : accSalesOrderServiceDAOobj.getSalesOrderDetailStatus(row);   
                            if (addobj > 0) {
                                status = "Open";
                                break;
                            }
                        }
                    } else {
                        status = (doflag)? getSalesOrderStatusForDO(salesOrder,rejectedrowCount,isConsignment,extraCompanyPreferences) : accSalesOrderServiceDAOobj.getSalesOrderStatus(salesOrder);
                    }
                    obj.put("status",status);                    
                    boolean includeprotax = false;
                    Set<SalesOrderDetail> salesOrderDetails = salesOrder.getRows();
                    for (SalesOrderDetail salesOrderDetail : salesOrderDetails) {
                         if (salesOrderDetail.getTax() != null) {
                            includeprotax = true;
                            break;
                        }
                    }
                    obj.put("includeprotax", includeprotax);
                    obj.put("salesPerson", salesOrder.getSalesperson()==null?"":salesOrder.getSalesperson().getID());
                     obj.put("gstIncluded", salesOrder.isGstIncluded());
                    DateFormat df = (DateFormat) requestParams.get("df");
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                    SalesOrderCustomData jeDetailCustom = (SalesOrderCustomData) salesOrder.getSoCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                if (jeDetailCustom != null) {
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                    Date dateFromDB=null;
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                        if (customFieldMap.containsKey(varEntry.getKey())) {

                            String value = "";
                            String Ids[] = coldata.split(",");
                            for (int i = 0; i < Ids.length; i++) {
                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                if (fieldComboData != null) {
                                    if (fieldComboData.getField().getFieldtype() == 12) {
//                                      value += Ids[i] != null ? Ids[i] + "," : ",";
                                        value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                    } else {
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
                    if(closeflag && salesOrder.isDeleted()){
                        addFlag = false;
                    } else if (closeflag && status.equalsIgnoreCase("Closed")) {
                        addFlag = false;
                    }
                    if(addFlag){
                        if(isConsignment && salesOrder.isAutoapproveflag()){
                            //isRequestPending==true && isRequestApproved==t ---- if some ispending and some is e is approved means partial case then add
                            //isRequestApproved  ==true if all are approved
                            //salesOrder.isAutoapproveflag() in case of auto assign check isAutoapproveflag is on 
                            //if(((isRequestPending==false && isRequestRejected==false ) || (isRequestPending==true && isRequestApproved==true) || isRequestApproved==true  )&& isquantityapproved){
                            if(isquantityapproved){
                                    jArr.put(obj);
                            }
                        }else{
                            jArr.put(obj);
                        }
                    }
              }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getSalesOrdersJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }
    
    
    
    
    /*
    Function to fetch sales order status for Delivery order. Checked if delivery order of all sales order quantities are prepared.
     */
    public String getSalesOrderStatusForDO(SalesOrder so,double rejectedCount,boolean isConsignment, ExtraCompanyPreferences extraCompanyPreferences) throws ServiceException {
        Set<SalesOrderDetail> orderDetail = so.getRows();
        Iterator ite = orderDetail.iterator();
        String result = "Closed";
        while (ite.hasNext()) {
            SalesOrderDetail soDetail = (SalesOrderDetail) ite.next();

            KwlReturnObject idresult = accInvoiceDAOobj.getDOIDFromSOD(soDetail.getID(),"");
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            double count = 0;
            while (ite1.hasNext()) {
                DeliveryOrderDetail ge = (DeliveryOrderDetail) ite1.next();
                qua += ge.getInventory().getQuantity();
            }
            if (isConsignment && extraCompanyPreferences!=null && extraCompanyPreferences.isRequestApprovalFlow()) {            
                if (qua < soDetail.getApprovedQuantity()) {
                    result = "Open";
                    break;
                }
            }else {
                if (qua < soDetail.getQuantity()) {
                    result = "Open";
                    break;
                }
            }
            
        }
        return result;
    }
    public String getSalesOrderStatusForDONew(Set<SalesOrderDetail> orderDetail, boolean isConsignment, ExtraCompanyPreferences extraCompanyPreferences) throws ServiceException {
        Iterator ite = orderDetail.iterator();
        String result = "Closed";
        while (ite.hasNext()) {
            SalesOrderDetail soDetail = (SalesOrderDetail) ite.next();
            KwlReturnObject idresult = accInvoiceDAOobj.getDOIDFromSOD(soDetail.getID(),"");
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                DeliveryOrderDetail ge = (DeliveryOrderDetail) ite1.next();
                qua += ge.getInventory().getQuantity();
            }
            if (isConsignment && extraCompanyPreferences != null && extraCompanyPreferences.isRequestApprovalFlow()) {
                if (qua < soDetail.getApprovedQuantity()) {
                    result = "Open";
                    break;
                }
            } else {
                if (qua < soDetail.getQuantity()) {
                    result = "Open";
                    break;
                }
            }
        }
        return result;
    }

    public JSONArray getSalesOrdersJsonOptimized(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean closeflag = Boolean.parseBoolean(request.getParameter("closeflag"));
            boolean exceptFlagINV = Boolean.parseBoolean(request.getParameter("exceptFlagINV"));
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = companyObj!=null ? (Company) companyObj.getEntityList().get(0):null;
            int countryid = ( company != null && company.getCountry() != null) ? Integer.parseInt(company.getCountry().getID()) : 0;
            int moduleid=-1;
            boolean isJobWorkOrderReciever=false;
            if(requestParams.containsKey("requestModuleid")){
                moduleid=Integer.parseInt(requestParams.get("requestModuleid").toString());
            }  
            if (requestParams.containsKey("isJobWorkOrderReciever")) {
                isJobWorkOrderReciever = Boolean.parseBoolean(requestParams.get("isJobWorkOrderReciever").toString());
            }
        /*-------isGenerateOrderFromOrder comes true when Generating PO from SO-------*/
            boolean isGenerateOrderFromOrder = request.getParameter("isGenerateOrderFromOrder") != null ? Boolean.parseBoolean(request.getParameter("isGenerateOrderFromOrder")) : false;
            /*
            * isJobWorkINReciever = true, when request comes from job Work IN For.
            * further used to check status of Job work Order.
            */
            boolean isJobWorkINReciever=false;
            if (requestParams.containsKey("isJobWorkINReciever")) {
                isJobWorkINReciever = Boolean.parseBoolean(requestParams.get("isJobWorkINReciever").toString());
            }
            boolean isViewJobWorkStockIn=false;
            if (request.getParameter("isViewJWSI")!= null) {
                isViewJobWorkStockIn = Boolean.parseBoolean(request.getParameter("isViewJWSI").toString());
            }
            DateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            Date today = sdf1.parse(sdf1.format(new Date()));
            boolean doflag = request.getParameter("doflag") != null ? true : false;
            boolean isLeaseSO = Boolean.FALSE.parseBoolean(request.getParameter("isLeaseFixedAsset"));
            boolean isShowAddress = Boolean.FALSE.parseBoolean(request.getParameter("isShowAddress"));//when call came from show address component
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            boolean isConsignment = request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, isLeaseSO ? Constants.Acc_Lease_Order_ModuleId : isConsignment ? Constants.Acc_ConsignmentRequest_ModuleId : isJobWorkOrderReciever?Constants.VENDOR_JOB_WORKORDER_MODULEID:Constants.Acc_Sales_Order_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject cpresult = accountingHandlerDAOobj.loadObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
             /*
            created DocumentEmailSetting Object for getting customershippingaddress flag
            */
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), companyid);
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                SalesOrder salesOrder = (SalesOrder) itr.next();
                Set<SalesOrderDetail> orderSet = salesOrder.getRows();
                boolean isquantityapproved = false;
                boolean addFlag = true;
                /*
                 * Code to check valid SOs
                 */
                if (isJobWorkOrderReciever && isJobWorkINReciever && !isViewJobWorkStockIn) {
                    Iterator ite = orderSet.iterator();
                    while (ite.hasNext()) {
                        SalesOrderDetail sodetail = (SalesOrderDetail) ite.next();
                        String soid = salesOrder.getID();
                        KwlReturnObject result1 = null;
                        JSONObject saDetailJson = new JSONObject();
                        HashMap<String, Object> assemblyParams = new HashMap<String, Object>();
                        HashMap<String, Object> SADetailParams = new HashMap<String, Object>();
                        SADetailParams.put("soid", soid);
                        /*
                         *  fetching SA details 
                         */
                        result1 = accProductObj.getSADetailsForSO(SADetailParams);
                        List<String> saDetailIds = result1.getEntityList();
                        /*
                         *   Creating JSON For product and its quantity mapped
                         */
                        saDetailJson = accSalesOrderServiceDAOobj.getProductQuanityJSONForJWO(saDetailIds);

                        String productid = sodetail.getProduct() != null ? sodetail.getProduct().getID() : "";
                        String bomid = sodetail.getBomcode() != null ? sodetail.getBomcode().getID() : "";
                        if (!StringUtil.isNullOrEmpty(bomid)) {
                            assemblyParams.put("bomdetailid", bomid);
                        } else {
                            assemblyParams.put("isdefaultbom", true);
                        }
                        assemblyParams.put("currencyid", currencyid);
                        assemblyParams.put("productid", productid);
                        KwlReturnObject assemblyItem = accProductObj.getAssemblyItems(assemblyParams);
                        if (!assemblyItem.getEntityList().isEmpty()) {
                            Iterator assembltItr = assemblyItem.getEntityList().iterator();
                            boolean addproduct = false;
                            while (assembltItr.hasNext()) {
                                Object[] assemblyItemObject = (Object[]) assembltItr.next();
                                ProductAssembly passembly = (ProductAssembly) assemblyItemObject[0];
                                if (passembly.getSubproducts() != null) {
                                    if (passembly.getSubproducts().getProducttype().getID().equals(Producttype.CUSTOMER_INVENTORY)) {
                                        double quantity = sodetail.getQuantity();                                                   //Assembly product quantity for JWO
                                        double inQuantity = saDetailJson.optDouble(passembly.getSubproducts().getID(), 0.0);        //Already IN quantity of JWInv product through stock adjustment for this JW
                                        if (quantity > 0) {
                                            double subPrdQuantity = quantity * passembly.getActualQuantity();                       //Required quantity of JWInv product
                                            if ((subPrdQuantity - inQuantity) == 0) {
                                                addFlag = false;
                                            } else {
                                                addproduct = true;
                                            }
                                        }
                                    }
                                }
                            }
                            addFlag = addproduct;
                        }
                    }
                }
                boolean isOpen = salesOrder.isIsopen(); // Status of SO 
                if (!isShowAddress) {
                    if (closeflag && salesOrder.isDeleted()) {
                        addFlag = false;
                    } else if (closeflag && !isOpen && moduleid != Constants.Acc_Purchase_Order_ModuleId) { //(closeflag && status.equalsIgnoreCase("Closed"))
                    /*
                         * While linking Sales Order in Purchase Order, we
                         * should show all the Sales Orders even if it is linked
                         * with Sales Invoice or Delivery Order
                         */
                        addFlag = false;
                    } else if (!isOpen && moduleid != Constants.Acc_Purchase_Order_ModuleId) { //  (status.equalsIgnoreCase("QA Failed") || status.equalsIgnoreCase("Pending QA Approval")) 
                    /*
                         * While linking Sales Order in Purchase Order, we
                         * should show all the Sales Orders even if it is linked
                         * with Sales Invoice or Delivery Order
                         */
                        addFlag = false;
                    }
                }
                
                    JSONObject columnprefObj = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                        columnprefObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                    }
                    boolean activatedropship = false;
                    if (columnprefObj.has("activatedropship") && columnprefObj.get("activatedropship") != null && (Boolean) columnprefObj.get("activatedropship") != false) {
                        activatedropship = true;
                    }
                    
                  /*----- Dropship type is not being loaded in PO , Invoice --------- */
                    if (!activatedropship && salesOrder.isIsDropshipDocument()) {
                      continue;
                    } 
  
                String status = isOpen ? "Open" : "Closed";
                 //Allow only those SO whose status is open as done in accSalesOrderServiceImpl -> getSalesOrdersJsonMerged().
                if (isConsignment || isJobWorkINReciever ) { // to check if whole quantity is stocked in  for that Job work order.
                    status = accSalesOrderServiceDAOobj.getSalesOrderStatus(salesOrder);
//                    if (status.equals("Open")) {
//                        if (salesOrder.getFromdate() != null && salesOrder.getTodate() != null && salesOrder.getFromdate().before(today) && salesOrder.getTodate().before(today)) {
//                            status = "Closed";
//                        }
//                    }
                }
//                if (addFlag) {
//                    if (exceptFlagINV) {
////                        Iterator itr1 = orderSet.iterator();
////                        status = "Closed";
////                        while (itr1.hasNext()) {
////                            SalesOrderDetail row = (SalesOrderDetail) itr1.next();
////                            double addobj = doflag ? accSalesOrderServiceDAOobj.getSalesOrderDetailStatusForDO(row) : accSalesOrderServiceDAOobj.getSalesOrderDetailStatus(row);
////                            if (addobj > 0) {
////                                status = "Open";
////                                break;
////                            }
////                        }
//                    } else {
//                        status = (doflag) ? getSalesOrderStatusForDONew(orderSet, isConsignment, extraCompanyPreferences) : accSalesOrderServiceDAOobj.getSalesOrderStatusNew(salesOrder, orderSet, pref, companyid);
//                    }
//                }
//                if (closeflag && status.equalsIgnoreCase("Closed")) {
//                    addFlag = false;
//                }
                //Approved Quantity check
                
                
                if (addFlag) {
                    if (isConsignment && salesOrder.isAutoapproveflag()) {
                        Iterator itrRow = orderSet.iterator();
                        while (itrRow.hasNext()) {
                            SalesOrderDetail sod = (SalesOrderDetail) itrRow.next();
                            double approvedQuantity = 0;
                            double doQty = accSalesOrderServiceDAOobj.getSalesOrderDetailStatusForDO(sod);
                            if (isConsignment && salesOrder.isAutoapproveflag() && ! sod.getApproverSet().isEmpty()) { //Case were Rule is Present in Auto Approval Flow
                                approvedQuantity += sod.getApprovedQuantity();
                            }else if(isConsignment && salesOrder.isAutoapproveflag() && sod.getApproverSet().isEmpty()){ //Case were Rule is Not Present in Auto Approval Flow
                                approvedQuantity += sod.getQuantity();
                            }
                            
                            if (approvedQuantity > 0 &&  !(doQty==sod.getRejectedQuantity())) {
                                isquantityapproved = true;
                            }
                        }
                        if (isquantityapproved) {
                            addFlag = true;
                        } else {
                            addFlag = false;
                        }
                    } else {
                        addFlag = true;
                    }
                }
                if (addFlag && (!status.equals("Closed") ||moduleid == Constants.Acc_Purchase_Order_ModuleId || isViewJobWorkStockIn)) {
                    KWLCurrency currency = null;
                    Customer customer = salesOrder.getCustomer();
                    if (salesOrder.getCurrency() != null) {
                        currency = salesOrder.getCurrency();
                    } else {
                        currency = customer.getAccount().getCurrency() == null ? kwlcurrency : customer.getAccount().getCurrency();
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("billid", salesOrder.getID());
                    obj.put("personid", customer.getID());
                    obj.put("billno", salesOrder.getSalesOrderNumber());
                    obj.put("duedate", authHandler.getDateOnlyFormat(request).format(salesOrder.getDueDate()));
                    obj.put("date", authHandler.getDateOnlyFormat(request).format(salesOrder.getOrderDate()));
                    obj.put("shipdate", salesOrder.getShipdate() == null ? "" : authHandler.getDateOnlyFormat(request).format(salesOrder.getShipdate()));
                    obj.put("termid", salesOrder.getTerm()!=null ? salesOrder.getTerm().getID() : "");
                    obj.put("shipvia", salesOrder.getShipvia() == null ? "" : salesOrder.getShipvia());
                    obj.put("fob", salesOrder.getFob() == null ? "" : salesOrder.getFob());
                    obj.put("islockQuantityflag", salesOrder.isLockquantityflag());
                    obj.put("externalcurrencyrate", salesOrder.getExternalCurrencyRate());
                    obj.put("isDisabledPOforSO", salesOrder.isDisabledSOforPO());  
                    obj.put("isdropshipchecked", salesOrder.isIsDropshipDocument());
                    obj.put("termdetails", accSalesOrderServiceDAOobj.getTermDetails(salesOrder.getID(), true));
                    if (salesOrder.getTermsincludegst() != null) {
                        obj.put(Constants.termsincludegst, salesOrder.getTermsincludegst());
                    }
                    obj.put("movementtype", salesOrder.getMovementType() != null ? salesOrder.getMovementType().getID() : "");
                    obj.put("shiplengthval", salesOrder.getShiplength());
                    obj.put("customerporefno", salesOrder.getCustomerPORefNo());
                    obj.put("gtaapplicable", salesOrder.isRcmapplicable());//ERP-34970(ERM-534)
                     /**
                     * Put Merchant Exporter Check
                     */
                    obj.put(Constants.isMerchantExporter, salesOrder.isIsMerchantExporter());
                    BillingShippingAddresses addresses = salesOrder.getBillingShippingAddresses();
                     if (documentEmailSettings != null && documentEmailSettings.isCustShippingAddressInPurDoc() || (salesOrder.isIsDropshipDocument() && moduleid == Constants.Acc_Purchase_Order_ModuleId)) {
                        AccountingAddressManager.getTransactionAddressJSONForPOFromSO(obj, addresses, false);
                      } else {
                        AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    }
                    double amount = 0, totalDiscount = 0, discountPrice = 0;
                    boolean includeprotax = false;
                    Iterator itrRow = orderSet.iterator();
                    while (itrRow.hasNext()) {
                        SalesOrderDetail sod = (SalesOrderDetail) itrRow.next();
                       // amount += authHandler.round(sod.getQuantity() * sod.getRate(), 2);
                        double rowTaxPercent = 0, LineLevelTaxAmt = 0, OtherTermNonTaxableAmt = 0;
                        if (sod.getTax() != null) {
                            includeprotax = true;
                            requestParams.put("transactiondate", salesOrder.getOrderDate());
                            requestParams.put("taxid", sod.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        }
                        double approvedQuantity = 0;
                        if (isConsignment && salesOrder.isAutoapproveflag()) {
                            approvedQuantity += sod.getApprovedQuantity();
                        }
                        if (approvedQuantity > 0) {
                            isquantityapproved = true;
                        }
                        KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, sod.getRate(), currency.getCurrencyID(), salesOrder.getOrderDate(), 0);
                        double sorate = (Double) bAmt.getEntityList().get(0);

                        double quotationPrice = sod.getQuantity() * sorate;
                        if (sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - (quotationPrice * sod.getDiscount() / 100);
                        } else {
                            discountPrice = quotationPrice - sod.getDiscount();
                        }
                        //For Adding Linelevel Term Amount of SO in Amount.(For India Country Only)
                        if(countryid == Constants.indian_country_id && extraCompanyPreferences.getLineLevelTermFlag()==1 ){ // For India Country 
                            LineLevelTaxAmt += sod.getRowtermamount();
                            OtherTermNonTaxableAmt += sod.getOtherTermNonTaxableAmount();
                        }
                        amount += discountPrice + (discountPrice * rowTaxPercent / 100) + LineLevelTaxAmt + OtherTermNonTaxableAmt;
                    }
                    
                    
                    double totalGlobalTermAmount = 0, taxableGlobalTermamount = 0; //Global Term Amount
                    HashMap<String, Object> requestParam = new HashMap();
                    requestParam.put("salesOrder", salesOrder.getID());
                    HashMap<String, Object> filterrequestParams = new HashMap();
                    KwlReturnObject salesOrderResult = null;
                    salesOrderResult = accSalesOrderDAOobj.getSalesOrderTermMap(requestParam);
                    filterrequestParams.put("taxid", salesOrder.getTax()==null?"":salesOrder.getTax().getID());
                    List<SalesOrderTermMap> termMap = salesOrderResult.getEntityList();
                    for (SalesOrderTermMap salesOrderTermMap : termMap) {
                        filterrequestParams.put("term", salesOrderTermMap.getTerm() == null ? "" : salesOrderTermMap.getTerm().getId());
                        double GlobaltermAmnt = salesOrderTermMap.getTermamount();
                        totalGlobalTermAmount += GlobaltermAmnt;
                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);

                        if (isTermMappedwithTax) {
                            taxableGlobalTermamount += GlobaltermAmnt;
                        }
                    }
                    
                    double taxPercent = 0;
                     if(salesOrder.getTax()!=null){
                        requestParams.put("transactiondate", salesOrder.getOrderDate());
                        requestParams.put("taxid", salesOrder.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                       if (taxList != null && !taxList.isEmpty()) { 
                        Object[] taxObj=(Object[]) taxList.get(0);
                        taxPercent=taxObj[1]==null?0:(Double) taxObj[1];

                       }
                    }
                    double ordertaxamount=(taxPercent==0?0:authHandler.round(((amount + taxableGlobalTermamount)*taxPercent/100), companyid));
                    obj.put("amount", amount + totalGlobalTermAmount + ordertaxamount);
                    KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, (amount + totalGlobalTermAmount + ordertaxamount), salesOrder.getCurrency().getCurrencyID(), salesOrder.getOrderDate(), salesOrder.getExternalCurrencyRate());
                    double totalAmountinBase = (Double) bAmt.getEntityList().get(0);
                    obj.put("amountinbase", authHandler.round(totalAmountinBase, companyid));
                    if (salesOrder.getDiscount() != 0) {
                        if (salesOrder.isPerDiscount()) {
                            totalDiscount = amount * salesOrder.getDiscount() / 100;
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - salesOrder.getDiscount();
                            totalDiscount = salesOrder.getDiscount();
                        }
                        obj.put("discounttotal", salesOrder.getDiscount());
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    obj.put("discount", totalDiscount);
                    obj.put("discountispertotal", salesOrder.isPerDiscount());
//                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getOrderDate(), 0);
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                    obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
//                    double taxPercent = 0;
//                    if (salesOrder.getTax() != null) {
//                        requestParams.put("transactiondate", salesOrder.getOrderDate());
//                        requestParams.put("taxid", salesOrder.getTax().getID());
//                        KwlReturnObject result = accTaxObj.getTax(requestParams);
//                        List taxList = result.getEntityList();
//                        Object[] taxObj = (Object[]) taxList.get(0);
//                        taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
//
//                    }
//                    double orderAmount = (Double) bAmt.getEntityList().get(0);
//                    double ordertaxamount = (taxPercent == 0 ? 0 : orderAmount * taxPercent / 100);
//                    obj.put("taxpercent", taxPercent);
//                    obj.put("taxamount", ordertaxamount);
//                    obj.put("orderamount", orderAmount);
//                    obj.put("orderamountwithTax", orderAmount + ordertaxamount);
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("personname", customer.getName());
                    obj.put("contractstatus", (salesOrder.getContract() != null) ? salesOrder.getContract().getCstatus() : "");
                    String contractId = "";
                    if (salesOrder.getContract() != null) {
                        contractId = salesOrder.getContract().getID();
                    }
                    obj.put("contract", contractId);
                    obj.put("memo", salesOrder.getMemo());
                    obj.put("isConsignment", salesOrder.isIsconsignment());
                    if (salesOrder.getCustWarehouse() != null) {
                        obj.put("custWarehouse", salesOrder.getCustWarehouse().getId());
                    }
                    if (salesOrder.isIsconsignment()) {
                        obj.put("todate", salesOrder.getTodate() == null ? "" : authHandler.getDateOnlyFormat(request).format(salesOrder.getTodate()));
                        obj.put("fromdate", salesOrder.getFromdate() == null ? "" : authHandler.getDateOnlyFormat(request).format(salesOrder.getFromdate()));
                    }
                    obj.put("costcenterid", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getID());
                    obj.put("costcenterName", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getName());
                    obj.put("status", salesOrder.isIsopen()?"Open":"Closed");
                    obj.put("includeprotax", includeprotax);
                    obj.put("salesPerson", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getID());
                    //ERP-41011: Used for remote Store of Sales Person
                    obj.put("salespersonname", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getValue());
                    obj.put("gstIncluded", salesOrder.isGstIncluded());
                    boolean isApplyTaxToTerms=salesOrder.isApplyTaxToTerms();
                    obj.put("isapplytaxtoterms", isApplyTaxToTerms);
                    obj.put(Constants.IsRoundingAdjustmentApplied, salesOrder.isIsRoundingAdjustmentApplied());
                    DateFormat df = (DateFormat) requestParams.get("df");
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    SalesOrderCustomData jeDetailCustom = (SalesOrderCustomData) salesOrder.getSoCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyid);
                        boolean linkflag = false;
                        if (requestParams.containsKey("linkflag") && requestParams.get("linkflag") != null) {
                            linkflag = Boolean.FALSE.parseBoolean(requestParams.get("linkflag").toString());
                        }
                         /*-------isGenerateOrderFromOrder comes true when Generating PO from SO-------*/
                        if (doflag || exceptFlagINV || linkflag || isGenerateOrderFromOrder) {
                            int moduleId = doflag ? ((!isConsignment) ? Constants.Acc_Delivery_Order_ModuleId : Constants.Acc_ConsignmentDeliveryOrder_ModuleId) : exceptFlagINV ? Constants.Acc_Invoice_ModuleId : Constants.Acc_Purchase_Order_ModuleId;
                            if (moduleid == Constants.Acc_Lease_DO) {
                                moduleId = Constants.Acc_Lease_DO;
                            } else if (moduleid == Constants.Acc_Contract_Order_ModuleId){
                                moduleId = Constants.Acc_Contract_Order_ModuleId;
                            } else if (moduleid == Constants.Acc_Lease_Contract){
                                moduleId = Constants.Acc_Lease_Contract;
                            }
                            if(isGenerateOrderFromOrder){
                                 moduleId =  Constants.Acc_Purchase_Order_ModuleId; 
                            }
                            params.put("linkModuleId", moduleId);
                            params.put("isLink", true);
                            params.put("customcolumn", 0);
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);

//                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
//                            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
//                            if (customFieldMap.containsKey(varEntry.getKey())) {
//                                String value = "";
//                                String Ids[] = coldata.split(",");
//                                for (int i = 0; i < Ids.length; i++) {
//                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
//                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
//                                    if (fieldComboData != null) {
//                                        if (fieldComboData.getField().getFieldtype() == 12) {
//                                            value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
//                                        } else {
//                                            value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
//                                        }
//                                    }
//                                }
//                                if (!StringUtil.isNullOrEmpty(value)) {
//                                    value = value.substring(0, value.length() - 1);
//                                }
//                                obj.put(varEntry.getKey(), value);
//                            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
//                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                                obj.put(varEntry.getKey(), sdf.format(Long.parseLong(coldata)));
//                            } else {
//                                if (!StringUtil.isNullOrEmpty(coldata)) {
//                                    String[] coldataArray = coldata.split(",");
//                                    String Coldata = "";
//                                    for (int countArray = 0; countArray < coldataArray.length; countArray++) {
//                                        Coldata += "'" + coldataArray[countArray] + "',";
//                                    }
//                                    Coldata = Coldata.substring(0, Coldata.length() - 1);
//                                    String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
//                                    obj.put(varEntry.getKey(), coldata);
//                                    obj.put(varEntry.getKey() + "_Values", ColValue);
//                                }
//                            }
//                        }
                    }
                    jArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getSalesOrdersJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    public String getProductReplacementRequestStatusForSO(ProductReplacement pr) throws ServiceException {
        String result = "Closed";

        Set<ProductReplacementDetail> productReplacementDetails = pr.getProductReplacementDetails();
        Iterator ite = productReplacementDetails.iterator();
        if (ite.hasNext()) {
            ProductReplacementDetail productReplacementDetail = (ProductReplacementDetail) ite.next();

            String productReplacementDetailId = productReplacementDetail.getId();

            KwlReturnObject idresult = accInvoiceDAOobj.getProductReplacementIDFromSOD(productReplacementDetailId);
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                SalesOrderDetail sod = (SalesOrderDetail) ite1.next();
                qua += sod.getQuantity();
            }
            if (qua < productReplacementDetail.getReplacementQuantity()) {
                result = "Open";
            }
        }
        return result;
    }

    public String getProductReplacementRequestStatusForQuotation(ProductReplacement pr) throws ServiceException {
        String result = "Closed";

        Set<ProductReplacementDetail> productReplacementDetails = pr.getProductReplacementDetails();
        Iterator ite = productReplacementDetails.iterator();
        if (ite.hasNext()) {
            ProductReplacementDetail productReplacementDetail = (ProductReplacementDetail) ite.next();

            String productReplacementDetailId = productReplacementDetail.getId();

            KwlReturnObject idresult = accInvoiceDAOobj.getQuotationDetailsFromProductReplacementID(productReplacementDetailId);
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                QuotationDetail qd = (QuotationDetail) ite1.next();
                qua += qd.getQuantity();
            }
            if (qua < productReplacementDetail.getReplacementQuantity()) {
                result = "Open";
            }
        }
        return result;
    }
    

    private boolean isSalesOrderCreatedBySelectedPR(ProductReplacement pr) throws ServiceException {
        boolean isSalesOrderCreatedBySelectedPR = false;
        Set<ProductReplacementDetail> productReplacementDetails = pr.getProductReplacementDetails();
        Iterator ite = productReplacementDetails.iterator();
        if (ite.hasNext()) {
            ProductReplacementDetail productReplacementDetail = (ProductReplacementDetail) ite.next();
            String productReplacementDetailId = productReplacementDetail.getId();
            KwlReturnObject idresult = accInvoiceDAOobj.getProductReplacementIDFromSOD(productReplacementDetailId);
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            if (ite1.hasNext()) {
                isSalesOrderCreatedBySelectedPR = true;
            }
        }
        return isSalesOrderCreatedBySelectedPR;
    }
    
    private boolean isQuotationCreatedBySelectedPR(ProductReplacement pr) throws ServiceException {
        boolean isQuotationCreatedBySelectedPR = false;
        Set<ProductReplacementDetail> productReplacementDetails = pr.getProductReplacementDetails();
        Iterator ite = productReplacementDetails.iterator();
        if (ite.hasNext()) {
            ProductReplacementDetail productReplacementDetail = (ProductReplacementDetail) ite.next();
            String productReplacementDetailId = productReplacementDetail.getId();
            KwlReturnObject idresult = accInvoiceDAOobj.getQuotationDetailsFromProductReplacementID(productReplacementDetailId);
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            if (ite1.hasNext()) {
                isQuotationCreatedBySelectedPR = true;
            }
        }
        return isQuotationCreatedBySelectedPR;
    }
    
    public double getProductReplacementRequestQuantitiesForSO(ProductReplacementDetail row) throws ServiceException {
        double returnQuantity = 0;
        if (row != null) {
            String productReplacementDetalId = row.getId();
            KwlReturnObject idresult = accInvoiceDAOobj.getProductReplacementIDFromSOD(productReplacementDetalId);
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                SalesOrderDetail sod = (SalesOrderDetail) ite1.next();
                qua += sod.getQuantity();
            }
            if (qua < row.getReplacementQuantity()) {
                returnQuantity = row.getReplacementQuantity() - qua;
            }
        }
        return returnQuantity;
    }
    public ModelAndView getPendingApprovalCRDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getPendingApprovalConsignmentRows(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesOrderController.getSalesOrderRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getSalesOrderRows:" + ex.getMessage();
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
     public JSONObject getPendingApprovalConsignmentRows(HttpServletRequest request) throws SessionExpiredException, ServiceException, ParseException {
          JSONObject jobj=new JSONObject();
          try{
                HashMap<String,Object> requestParams = getSalesOrdersMap(request);
                String companyid = sessionHandlerImpl.getCompanyid(request);
                String requestorid = sessionHandlerImpl.getUserid(request);
                              
                boolean isRejectedItemsOnly=(StringUtil.isNullOrEmpty(request.getParameter("rejectedrecords")))?false:Boolean.parseBoolean(request.getParameter("rejectedrecords"));
                KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), requestorid);
                User user = (User) userresult.getEntityList().get(0);      
                Set<Store> storeSet = new HashSet();
                
                List storeListByManager = storeService.getStoresByStoreManagers(user, true, null, null, null);
                List storeListByExecutive = storeService.getStoresByStoreExecutives(user, true, null, null, null);
                List storeListByQA = storeService.getStoresByQAPerson(user);
                Iterator useritr = storeListByQA.iterator();
                while (useritr.hasNext()) {
                    InventoryWarehouse inventoryWarehouse = (InventoryWarehouse) useritr.next();
                    userresult = accountingHandlerDAOobj.getObject(Store.class.getName(), inventoryWarehouse.getId());
                    Store store1 = (Store) userresult.getEntityList().get(0);
                    if (store1 != null && store1.isActive()) {
                        storeSet.add(store1);
                    }
                }
                storeSet.addAll(storeListByManager);
                storeSet.addAll(storeListByExecutive);
          
                String storeids="";
                for (Store s : storeSet) {                     
                    if(StringUtil.isNullOrEmpty(storeids)){
                        storeids=s.getId();
                    }else{
                        storeids+=","+s.getId();
                    }                      
                }
                KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
                KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
//                boolean doflag = request.getParameter("doflag")!=null?true:false;
//                String[] sos=(String[]) request.getParameter("bills").split(",");
//                int i=0;
                JSONArray jArr=new JSONArray();
                double addobj = 1;

//                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

//                KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
//                ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
//            
//                boolean isForDOGROLinking = (StringUtil.isNullOrEmpty(request.getParameter("isForDOGROLinking")))?false:Boolean.parseBoolean(request.getParameter("isForDOGROLinking"));
//                String closeflag = request.getParameter("closeflag");    
                boolean isBatchForProduct=false;
                boolean isSerialForProduct=false;
                boolean isLocationForProduct=false;
                boolean isWarehouseForProduct=false;
                boolean isRowForProduct = false;
                boolean isRackForProduct = false;
                boolean isBinForProduct = false;
                
                requestParams.put("requestorid", requestorid);
                requestParams.put("storeids", storeids);
                requestParams.put("isRejectedItemsOnly", isRejectedItemsOnly);
                KwlReturnObject podresult = accSalesOrderDAOobj.getCRPendingApprovalSalesOrderDetailsModified(requestParams);
                Iterator itr = podresult.getEntityList().iterator();
                while (itr.hasNext()) {
                  Object object = (Object) itr.next();
                  curresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), object.toString());
                  SalesOrderDetail row = (SalesOrderDetail) curresult.getEntityList().get(0);
//                  SerialDocumentMapping documentMapping = null;
//                  LocationBatchDocumentMapping batchDocumentMapping = null;
//                  if (objArr[1] != null) {
//                      curresult = accountingHandlerDAOobj.getObject(SerialDocumentMapping.class.getName(), objArr[1].toString());
//                      documentMapping = (SerialDocumentMapping) curresult.getEntityList().get(0);
//                  }
//                  if (objArr[2] != null) {
//                      curresult = accountingHandlerDAOobj.getObject(LocationBatchDocumentMapping.class.getName(), objArr[2].toString());
//                      batchDocumentMapping = (LocationBatchDocumentMapping) curresult.getEntityList().get(0);
//                  }
                  KWLCurrency currency = null;
                  SalesOrder so = row != null ? row.getSalesOrder() : null;
                  if (so.getCurrency() != null) {
                      currency = so.getCurrency();
                  } else {
                      currency = so.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : so.getCustomer().getAccount().getCurrency();
                  }

//                  if (documentMapping != null) {
//                      JSONObject obj = new JSONObject();
//                      obj.put("billid", so.getID());
//                      obj.put("billno", so.getSalesOrderNumber());
//                      obj.put("currencysymbol", currency.getSymbol());
//                      obj.put("srno", row.getSrno());
//                      obj.put("rowid", row.getID());
//                      obj.put("createdby", StringUtil.getFullName(so.getCreatedby()));
//                      //                    obj.put("originalTransactionRowid", row.getID());
//                      obj.put("productid", row.getProduct().getID());
//                      obj.put("productname", row.getProduct().getName());
//                      String uom = row.getUom() != null ? row.getUom().getName() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getName();
//                      obj.put("unitname", uom);
//                      obj.put("uomname", uom);
//                      obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getName());
//                      //                    obj.put("multiuom", row.getProduct().isMultiuom());
//                      obj.put("desc", StringUtil.isNullOrEmpty(row.getDescription()) ? row.getProduct().getDescription() : row.getDescription());
//                      obj.put("description", StringUtil.isNullOrEmpty(row.getDescription()) ? row.getProduct().getDescription() : row.getDescription());                    
//                      obj.put("pid", row.getProduct().getProductid());
//                     
//                      obj.put("lockquantity", row.getLockquantity()); //for getting locked  quantity of indivisual so
//                      obj.put("lockquantitydue", row.getLockquantitydue()); //for getting locked  quantity due of indivisual so
//                      obj.put("islockQuantityflag", so.isLockquantityflag()); //for getting locked flag of indivisual so
//                      obj.put("isConsignment", so.isIsconsignment()); //for getting is consignment request                   
//                      if (so.isIsconsignment()) {
//                          obj.put("todate", so.getTodate() == null ? "" : authHandler.getDateFormatter(request).format(so.getTodate()));
//                          obj.put("fromdate", so.getFromdate() == null ? "" : authHandler.getDateFormatter(request).format(so.getFromdate()));
//                      }
//                      Set<User> approverSet = new HashSet<User>();
//                      obj.put("newbatchserialid", documentMapping.getSerialid().getId());
//                      obj.put("serialbatchmapid", documentMapping.getId());
//                      obj.put("status", documentMapping.getRequestApprovalStatus());
//                      if (isRejectedItemsOnly) {
//                          obj.put("rejectedby", documentMapping.getRejectedby() != null ? documentMapping.getRejectedby().getFullName() : "");
//                      }
//                      if (documentMapping.getSerialid() != null && documentMapping.getSerialid().getBatch() != null) {
//                          NewProductBatch batch = documentMapping.getSerialid().getBatch();
//                          obj.put("quantity", documentMapping.getSerialid().getQuantity());
//                          obj.put("serialname", documentMapping.getSerialid().getSerialname());
//                          obj.put("batchid", batch.getId());
//                          obj.put("batchname", batch.getBatchname());
//                          obj.put("warehousename", batch.getWarehouse() != null ? batch.getWarehouse().getName() : "");
//                          obj.put("locationname", batch.getLocation() != null ? batch.getLocation().getName() : "");
//                          obj.put("warehouse", batch.getWarehouse() != null ? batch.getWarehouse().getId() : "");
//                          obj.put("location", batch.getLocation() != null ? batch.getLocation().getId() : "");
//                      }
//                      approverSet = documentMapping.getApproverSet();
//                      KwlReturnObject result = accProductObj.getObject(User.class.getName(), requestorid);
//                      User requestor = (User) result.getEntityList().get(0);
//                      if (approverSet.contains(requestor)) {
//                          obj.put("isuserallowtoapprove", true);
//                      } else {
//                          obj.put("isuserallowtoapprove", false);
//                      }
//                      if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
//                          KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
//                          Product product = (Product) prodresult.getEntityList().get(0);
//                          isLocationForProduct = product.isIslocationforproduct();
//                          isWarehouseForProduct = product.isIswarehouseforproduct();
//                          isBatchForProduct = product.isIsBatchForProduct();
//                          isSerialForProduct = product.isIsSerialForProduct();
//                          isRowForProduct = product.isIsrowforproduct();
//                          isRackForProduct = product.isIsrackforproduct();
//                          isBinForProduct = product.isIsbinforproduct();
//                      }
//                      obj.put("isLocationForProduct", isLocationForProduct);
//                      obj.put("isWarehouseForProduct", isWarehouseForProduct);
//                      obj.put("isBatchForProduct", isBatchForProduct);
//                      obj.put("isSerialForProduct", isSerialForProduct);
//                      obj.put("isRowForProduct", isRowForProduct);
//                      obj.put("isRackForProduct", isRackForProduct);
//                      obj.put("isBinForProduct", isBinForProduct);
//                      obj.put("rate", row.getRate());
//
//                      KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getOrderDate(), 0);
//                      obj.put("orderrate", row.getRate());//obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
//                      //                        obj.put("quantity", row.getQuantity());                    
//                      if (row.getUom() != null) {
//                          obj.put("uomid", row.getUom().getID());
//                      } else {
//                          obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
//                      }
//
//                      obj.put("balanceQuantity", row.getQuantity() - accSalesOrderServiceDAOobj.getSalesOrderBalanceQuantity(row));
//
//                      jArr.put(obj);
//                  } else if (documentMapping == null && batchDocumentMapping != null) {
//                      JSONObject obj = new JSONObject();
//                      obj.put("billid", so.getID());
//                      obj.put("billno", so.getSalesOrderNumber());
//                      obj.put("currencysymbol", currency.getSymbol());
//                      obj.put("srno", row.getSrno());
//                      obj.put("rowid", row.getID());
//                      obj.put("createdby", StringUtil.getFullName(so.getCreatedby()));
//                      obj.put("productid", row.getProduct().getID());
//                      obj.put("productname", row.getProduct().getName());
//                      String uom = row.getUom() != null ? row.getUom().getName() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getName();
//                      obj.put("unitname", uom);
//                      obj.put("uomname", uom);
//                      obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getName());               
//                      obj.put("pid", row.getProduct().getProductid());
//                      obj.put("memo", row.getRemark());
//                      obj.put("lockquantity", row.getLockquantity()); //for getting locked  quantity of indivisual so
//                      obj.put("lockquantitydue", row.getLockquantitydue()); //for getting locked  quantity due of indivisual so
//                      obj.put("islockQuantityflag", so.isLockquantityflag()); //for getting locked flag of indivisual so
//                      obj.put("isConsignment", so.isIsconsignment()); //for getting is consignment request
//                      if (so.isIsconsignment()) {
//                          obj.put("todate", so.getTodate() == null ? "" : authHandler.getDateFormatter(request).format(so.getTodate()));
//                          obj.put("fromdate", so.getFromdate() == null ? "" : authHandler.getDateFormatter(request).format(so.getFromdate()));
//                      }
//                      Set<User> approverSet = new HashSet<User>();
//                      obj.put("batchid", batchDocumentMapping.getBatchmapid() != null ? batchDocumentMapping.getBatchmapid().getId() : "");
//                      obj.put("locationbatchmapid", batchDocumentMapping.getId());
//                      obj.put("status", isRejectedItemsOnly?RequestApprovalStatus.REJECTED:batchDocumentMapping.getRequestApprovalStatus());
//                      if (isRejectedItemsOnly) {
//                          //                            obj.put("rejectedby", batchDocumentMapping.getRejectedby() != null ? documentMapping.getRejectedby().getFullName() : "");
//                      }
//                      if (batchDocumentMapping.getBatchmapid() != null) {
//                          NewProductBatch batch = batchDocumentMapping.getBatchmapid();
//                          obj.put("quantity", batch.getQuantity());
//                          obj.put("serialname", "-");
//                          obj.put("batchid", batch.getId());
//                          obj.put("batchname", batch.getBatchname());
//                          obj.put("warehousename", batch.getWarehouse() != null ? batch.getWarehouse().getName() : "");
//                          obj.put("locationname", batch.getLocation() != null ? batch.getLocation().getName() : "");
//                          obj.put("warehouse", batch.getWarehouse() != null ? batch.getWarehouse().getId() : "");
//                          obj.put("location", batch.getLocation() != null ? batch.getLocation().getId() : "");
//                      }
//                      if(batchDocumentMapping.getApproverSet()!=null){
//                          approverSet = batchDocumentMapping.getApproverSet();
//                      }
//
//                      KwlReturnObject result = accProductObj.getObject(User.class.getName(), requestorid);
//                      User requestor = (User) result.getEntityList().get(0);
//                      if (approverSet.contains(requestor)) {
//                          obj.put("isuserallowtoapprove", true);
//                      } else {
//                          obj.put("isuserallowtoapprove", false);
//                      }
//                      if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
//                          KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
//                          Product product = (Product) prodresult.getEntityList().get(0);
//                          isLocationForProduct = product.isIslocationforproduct();
//                          isWarehouseForProduct = product.isIswarehouseforproduct();
//                          isBatchForProduct = product.isIsBatchForProduct();
//                          isSerialForProduct = product.isIsSerialForProduct();
//                          isRowForProduct = product.isIsrowforproduct();
//                          isRackForProduct = product.isIsrackforproduct();
//                          isBinForProduct = product.isIsbinforproduct();
//                      }
//                      obj.put("isLocationForProduct", isLocationForProduct);
//                      obj.put("isWarehouseForProduct", isWarehouseForProduct);
//                      obj.put("isBatchForProduct", isBatchForProduct);
//                      obj.put("isSerialForProduct", isSerialForProduct);
//                      obj.put("isRowForProduct", isRowForProduct);
//                      obj.put("isRackForProduct", isRackForProduct);
//                      obj.put("isBinForProduct", isBinForProduct);
//                      obj.put("rate", row.getRate());
//
//                      KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getOrderDate(), 0);
//                      obj.put("orderrate", row.getRate());//obj.put("orderrate", (Double) bAmt.getEntityList().get(0));                 
//                      if (row.getUom() != null) {
//                          obj.put("uomid", row.getUom().getID());
//                      } else {
//                          obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
//                      }
//                      jArr.put(obj);
//                  } 
                  
                  if(row!=null){
                      
                      if(row.isIsLineItemClosed()){
                          continue;  //        remove the items which are closed at line level to avoid patial approve . 
                      }
                      
                      JSONObject obj = new JSONObject();
                      obj.put("billid", so.getID());
                      obj.put("billno", so.getSalesOrderNumber());
                      obj.put("currencysymbol", currency.getSymbol());
                      obj.put("srno", row.getSrno());
                      obj.put("rowid", row.getID());
                      if(so.getSalesperson() != null){
                          obj.put("createdby", so.getSalesperson().getValue());
                      }
                      obj.put("productid", row.getProduct().getID());
                      obj.put("productname", row.getProduct().getName());
                      obj.put("desc", row.getProduct().getDescription());
                      String uom = row.getUom() != null ? row.getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                      obj.put("unitname", uom);
                      obj.put("uomname", uom);
                      obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                      obj.put("pid", row.getProduct().getProductid());
                      obj.put("memo", row.getRemark());
                      obj.put("lockquantity", row.getLockquantity()); //for getting locked  quantity of indivisual so
                      obj.put("lockquantitydue", row.getLockquantitydue()); //for getting locked  quantity due of indivisual so
                      obj.put("islockQuantityflag", so.isLockquantityflag()); //for getting locked flag of indivisual so
                      obj.put("isConsignment", so.isIsconsignment()); //for getting is consignment request 
                      if (so.isIsconsignment()) {
                          obj.put("todate", so.getTodate() == null ? "" : authHandler.getDateOnlyFormat(request).format(so.getTodate()));
                          obj.put("fromdate", so.getFromdate() == null ? "" : authHandler.getDateOnlyFormat(request).format(so.getFromdate()));
                      }
                      Set<User> approverSet = new HashSet<User>();
                      obj.put("quantity", isRejectedItemsOnly ? row.getRejectedQuantity() : (row.getQuantity() - (row.getApprovedQuantity() + row.getRejectedQuantity())));
                      obj.put("serialname", "-");
                      obj.put("batchid", "-");
                      obj.put("batchname", "-");
                      obj.put("warehousename", so.getRequestWarehouse() != null ? so.getRequestWarehouse().getName() : "");
                      obj.put("locationname", so.getRequestLocation() != null ? so.getRequestLocation().getName() : "");
                      obj.put("warehouse", so.getRequestWarehouse() != null ? so.getRequestWarehouse().getId() : "");
                      obj.put("location", so.getRequestLocation() != null ? so.getRequestLocation().getId() : "");
                      if (isRejectedItemsOnly) {
                          obj.put("rejectedby", row.getRejectedby() != null ? row.getRejectedby().getFullName() : "");
                      }
                      obj.put("newbatchserialid", "");
                      obj.put("serialbatchmapid", "");
                      obj.put("status", isRejectedItemsOnly?RequestApprovalStatus.REJECTED:RequestApprovalStatus.PENDING);
                      approverSet = row.getApproverSet();

                      KwlReturnObject result = accProductObj.getObject(User.class.getName(), requestorid);
                      User requestor = (User) result.getEntityList().get(0);
                      if (approverSet.contains(requestor)) {
                          obj.put("isuserallowtoapprove", true);
                      } else {
                          obj.put("isuserallowtoapprove", false);
                      }
                      if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                          KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
                          Product product = (Product) prodresult.getEntityList().get(0);
                          isLocationForProduct = product.isIslocationforproduct();
                          isWarehouseForProduct = product.isIswarehouseforproduct();
                          isBatchForProduct = product.isIsBatchForProduct();
                          isSerialForProduct = product.isIsSerialForProduct();
                          isRowForProduct = product.isIsrowforproduct();
                          isRackForProduct = product.isIsrackforproduct();
                          isBinForProduct = product.isIsbinforproduct();
                      }
                      obj.put("isLocationForProduct", isLocationForProduct);
                      obj.put("isWarehouseForProduct", isWarehouseForProduct);
                      obj.put("isBatchForProduct", isBatchForProduct);
                      obj.put("isSerialForProduct", isSerialForProduct);
                      obj.put("isRowForProduct", isRowForProduct);
                      obj.put("isRackForProduct", isRackForProduct);
                      obj.put("isBinForProduct", isBinForProduct);
                      obj.put("rate", row.getRate());
                      obj.put("customerId", so.getCustomer() != null?so.getCustomer().getID():"");
                      obj.put("customerName", so.getCustomer() != null?so.getCustomer().getName():"");
                      
                      KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getOrderDate(), 0);
                      obj.put("orderrate", row.getRate());//obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                      //                        obj.put("quantity", row.getQuantity());                    
                      if (row.getUom() != null) {
                          obj.put("uomid", row.getUom().getID());
                      } else {
                          obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                      }

                      jArr.put(obj);
                  }
              }
                jobj.put("data", jArr);
                jobj.put("count", podresult.getRecordTotalCount());
              
          }catch(Exception ex){
             Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
          }
          return  jobj;
     }
    public ModelAndView getSalesOrderRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
            jobj = accSalesOrderServiceDAOobj.getSalesOrderRows(paramJobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesOrderController.getSalesOrderRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getSalesOrderRows:" + ex.getMessage();
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
   
    // Method to get advance payments that are linked with SO.
    public ModelAndView getLinkedAdvancePayments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accSalesOrderServiceDAOobj.getLinkedAdvancePayments(paramJobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesOrderController.getLinkedAdvancePayments:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getLinkedAdvancePayments:" + ex.getMessage();
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
    public ModelAndView getSalesOrderRowBatchJSON(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String productid=request.getParameter("productid");
            String documentid=request.getParameter("documentid");
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
            Product product = (Product) returnObject.getEntityList().get(0);
            if(!StringUtil.isNullOrEmpty(documentid) && product != null){
                JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
                String batchdetails = accSalesOrderServiceDAOobj.getNewBatchJson(product, paramJobj, documentid);
                jobj.put("data", new JSONArray(batchdetails));
            }else{
                jobj.put("data", "");
            }
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesOrderController.getSalesOrderRowBatchJSON:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getSalesOrderRowBatchJSON:" + ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
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
    
       public String getBatchJson(ProductBatch productBatch, boolean isFixedAssetDO,boolean isbatch,boolean isBatchForProduct,boolean isserial,boolean isSerialForProduct,HttpServletRequest request) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String purchasebatchid = "";
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("batch.id");
        filter_params.add(productBatch.getId());
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject kmsg = accSalesOrderDAOobj.getSerialForBatch(filterRequestParams);

        List list = kmsg.getEntityList();
        Iterator iter = list.iterator();
        int i = 1;
        while (iter.hasNext()) {
            BatchSerial batchSerial = (BatchSerial) iter.next();
            JSONObject obj = new JSONObject();
            if (i == 1) {
                obj.put("id", productBatch.getId());
                obj.put("batch", productBatch.getName());
                obj.put("batch", productBatch.getName());
                obj.put("batchname", productBatch.getName());
                obj.put("location", productBatch.getLocation().getId());
                obj.put("warehouse", productBatch.getWarehouse().getId());
                obj.put("mfgdate", productBatch.getMfgdate()!=null?authHandler.getDateOnlyFormat(request).format(productBatch.getMfgdate()):"");
                obj.put("expdate",productBatch.getExpdate()!=null?authHandler.getDateOnlyFormat(request).format(productBatch.getExpdate()):"");
                obj.put("quantity", productBatch.getQuantity());
                obj.put("balance", productBatch.getBalance());
                obj.put("balance", productBatch.getBalance());
                obj.put("asset", productBatch.getAsset());
                if (isFixedAssetDO) {
                    obj.put("purchasebatchid", productBatch.getId());
                    purchasebatchid = productBatch.getId();
                }
            } else {
                obj.put("id", "");
                obj.put("batch", "");
                obj.put("batchname", "");
                obj.put("location", "");
                obj.put("warehouse", "");
                obj.put("mfgdate", "");
                obj.put("expdate", "");
                obj.put("quantity", "");
                obj.put("balance", "");
                obj.put("purchasebatchid", "");
            }
            i++;
            obj.put("serialnoid", batchSerial.getId());
            obj.put("serialno", batchSerial.getName());
           obj.put("expstart",batchSerial.getExpfromdate()!=null?authHandler.getDateOnlyFormat(request).format(batchSerial.getExpfromdate()):"");
           obj.put("expend", batchSerial.getExptodate()!=null?authHandler.getDateOnlyFormat(request).format(batchSerial.getExptodate()):"");
            if (isFixedAssetDO) {
                obj.put("purchaseserialid", batchSerial.getId());
            } 
            jSONArray.put(obj);

        }
       if (isBatchForProduct && !isSerialForProduct) //only in batch case
          {
              JSONObject Jobj = new JSONObject();
              Jobj = getOnlyBatchDetail(productBatch, request);
              if (isFixedAssetDO) {
                  purchasebatchid = productBatch.getId();
              }
              if (!StringUtil.isNullOrEmpty(purchasebatchid)) {
                  Jobj.put("purchasebatchid", purchasebatchid);
              }
              jSONArray.put(Jobj);
          }

          return jSONArray.toString();
    }
         public JSONObject getOnlyBatchDetail(ProductBatch productBatch, HttpServletRequest request) throws JSONException, SessionExpiredException {

        JSONObject obj = new JSONObject();
        obj.put("id", productBatch.getId());
        obj.put("batch", productBatch.getName());
        obj.put("batchname", productBatch.getName());
        obj.put("location", productBatch.getLocation().getId());
        obj.put("warehouse", productBatch.getWarehouse().getId());
        obj.put("mfgdate", productBatch.getMfgdate() != null ? authHandler.getDateOnlyFormat(request).format(productBatch.getMfgdate()) : "");
        obj.put("expdate", productBatch.getExpdate() != null ? authHandler.getDateOnlyFormat(request).format(productBatch.getExpdate()) : "");
        obj.put("quantity", productBatch.getQuantity());
        obj.put("balance", productBatch.getBalance());
        obj.put("asset", productBatch.getAsset());
        obj.put("expstart", "");
        obj.put("expend","");
        return obj;
    }
     
     
       //function for showing link information of SO
public ModelAndView getSoLinkedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getSoLinkedInTransaction(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesOrderController.getSalesOrderRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getSalesOrderRows:" + ex.getMessage();
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
    
    public JSONObject getSoLinkedInTransaction(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String soid = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat();
           //  JSONObject obj = new JSONObject();
            String currencyid=sessionHandlerImpl.getCurrencyID(request);
             HashMap<String, Object> requestParams = getSalesOrdersMap(request);
             requestParams.put("soid", soid);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
            KWLCurrency currency = (KWLCurrency) curresult1.getEntityList().get(0);
            requestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(soid)) {
                KwlReturnObject result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {

                    Object[] oj = (Object[]) itr.next();
                    String invid = oj[0].toString();
                    JSONObject obj = new JSONObject();
                    int type=0; // SO->SI
                    //Withoutinventory 0 for normal, 1 for billing
                    boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                    if (withoutinventory) {

                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), invid);
                        BillingInvoice invoice = (BillingInvoice) objItr.getEntityList().get(0);

                        JournalEntry je = invoice.getJournalEntry();
                        obj.put("billid", invoice.getID());
                        obj.put("companyid", invoice.getCompany().getCompanyID());
                        obj.put("withoutinventory", withoutinventory);
                        obj.put("customername", invoice.getCustomer() == null ? "" : invoice.getCustomer().getName());
                        obj.put("transactionNo", invoice.getBillingInvoiceNumber());
                        obj.put("journalEntryNo", je.getEntryNumber());
                        obj.put("date", df.format(je.getEntryDate()));
                        obj.put("mergedCategoryData", "Customer Invoice");  //type of data

                    } else {
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                        Invoice invoice = (Invoice) objItr.getEntityList().get(0);
                        JournalEntry je = invoice.getJournalEntry();
                        JournalEntryDetail d = invoice.getCustomerEntry();
                        Account account = d.getAccount();
                        obj.put("billid", invoice.getID());
                        obj.put(Constants.IsRoundingAdjustmentApplied, invoice.isIsRoundingAdjustmentApplied());
                        obj.put("companyid", invoice.getCompany().getCompanyID());
                        obj.put("withoutinventory", withoutinventory);
                        obj.put("customername", invoice.getCustomer() == null ? "" : invoice.getCustomer().getName());
                        obj.put("transactionNo", invoice.getInvoiceNumber());   //invoice no
                        obj.put("journalEntryNo", je.getEntryNumber());  //journal entry no
//                        obj.put("date", df.format(je.getEntryDate()));  //date 
                        obj.put("date", df.format(invoice.getCreationDate()));  //date 
                        obj.put("mergedCategoryData", "Customer Invoice");  //type of data
                        obj.put("billid", invoice.getID());
                        obj.put("isOpeningBalanceTransaction", false);
                        obj.put("partialinv", invoice.isPartialinv());
                        obj.put("personid", invoice.getCustomer() == null ? account.getID() : invoice.getCustomer().getID());// account.getID());
                        obj.put("personemail", invoice.getCustomer() == null ? "" : invoice.getCustomer().getEmail());
                        obj.put("customername", invoice.getCustomer() == null ? "" : invoice.getCustomer().getName());
                        obj.put("currencyid", currencyid);
                        obj.put("currencyidval", authHandlerDAOObj.getCurrency(sessionHandlerImpl.getCurrencyID(request)));
                        obj.put("currencysymbol", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                        obj.put("currencyname", (invoice.getCurrency() == null ? currency.getName() : invoice.getCurrency().getName()));
                        obj.put("currencycode", (invoice.getCurrency() == null ? currency.getCurrencyCode() : invoice.getCurrency().getCurrencyCode()));
                        obj.put("companyaddress", invoice.getCompany().getAddress());
                        obj.put("isfavourite", invoice.isFavourite());
                        obj.put("billto", invoice.getBillTo());
                        obj.put("shipto", invoice.getShipTo());
                        obj.put("porefno", invoice.getPoRefNumber());
                        obj.put("journalentryid", je.getID());
                        obj.put("entryno", je.getEntryNumber());
                        obj.put("externalcurrencyrate", je.getExternalCurrencyRate());
//                        obj.put("date", df.format(je.getEntryDate()));
                        obj.put("date", df.format(invoice.getCreationDate()));
                        obj.put("shipdate", invoice.getShipDate() == null ? "" : df.format(invoice.getShipDate()));
                        obj.put("duedate", df.format(invoice.getDueDate()));
                        obj.put("personname", invoice.getCustomer() == null ? account.getName() : invoice.getCustomer().getName());
                        obj.put("salesPerson", invoice.getMasterSalesPerson() == null ? "" : invoice.getMasterSalesPerson().getID());
                        obj.put("taxamount", invoice.getTaxEntry() == null ? 0 : invoice.getTaxEntry().getAmount());
                        obj.put("taxincluded", invoice.getTax() == null ? false : true);
                        obj.put("taxid", invoice.getTax() == null ? "" : invoice.getTax().getID());
                        obj.put("taxname", invoice.getTax() == null ? "" : invoice.getTax().getName());
                        obj.put("memo", invoice.getMemo());
                        obj.put("termname", invoice.getCustomer() == null ? "" : ((invoice.getCustomer().getCreditTerm()) == null) ? "" : invoice.getCustomer().getCreditTerm().getTermname());
                        obj.put("deleted", invoice.isDeleted());
                        obj.put("discount", invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscountValue());
                        obj.put("ispercentdiscount", invoice.getDiscount() == null ? false : invoice.getDiscount().isInPercent());
                        obj.put("discountval", invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscount());
                        obj.put("shipvia", invoice.getShipvia() == null ? "" : invoice.getShipvia());
                        obj.put("posttext", invoice.getPostText() == null ? "" : invoice.getPostText());
                        obj.put("costcenterid", (je != null ? je.getCostcenter() == null ? "" : je.getCostcenter().getID() : ""));
                        obj.put("costcenterName", (je != null ? je.getCostcenter() == null ? "" : je.getCostcenter().getName() : ""));
                        obj.put("archieve", 0);
                        obj.put("cashtransaction", invoice.isCashtransaction());
                        boolean includeprotax = false;
                        Set<InvoiceDetail> invoiceDetails = invoice.getRows();
                        for (InvoiceDetail invoiceDetail : invoiceDetails) {
                            if (invoiceDetail.getTax() != null) {
                                includeprotax = true;
                                break;
                            }
                        }
                        obj.put("includeprotax", includeprotax);
                        if (invoice.getModifiedby() != null) {
                            obj.put("lasteditedby", StringUtil.getFullName(invoice.getModifiedby()));
                        }

                        obj.put("fob", invoice.getFob() == null ? "" : invoice.getFob());
                        BillingShippingAddresses addresses = invoice.getBillingShippingAddresses();
                        AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                        obj.put("sequenceformatid", invoice.getSeqformat() == null ? "" : invoice.getSeqformat().getID());
                        obj.put("type",type);
                        

                    }
                    jArr.put(obj);
                }
                
                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                JSONObject paymentjobj = accSalesOrderServiceDAOobj.getLinkedAdvancePayments(paramJobj);
                JSONArray dataArr = (JSONArray) paymentjobj.get("data");
                for (int index = 0; index < dataArr.length(); index++) {
                    JSONObject rowjobj = new JSONObject();
                    rowjobj = dataArr.getJSONObject(index);
                    String receiptAdvanceDetailID = (String) rowjobj.get("billid");
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(ReceiptAdvanceDetail.class.getName(), receiptAdvanceDetailID);
                    ReceiptAdvanceDetail receiptAdvanceDetailobj = (ReceiptAdvanceDetail) objItr.getEntityList().get(0);
                    Receipt receipt = receiptAdvanceDetailobj.getReceipt();
                    JSONObject obj = new JSONObject();
                    int type = 1; // SI->Advance Receipt
                    Customer customer = receipt.getCustomer();
                    String jeNumber = (receipt.isIsOpeningBalenceReceipt()) ? "" : receipt.getJournalEntry().getEntryNumber();
                    String jeIds = (receipt.isIsOpeningBalenceReceipt()) ? "" : receipt.getJournalEntry().getID();
                    if (receipt.getJournalEntryForBankCharges() != null) {
                        jeNumber += "<br>" + receipt.getJournalEntryForBankCharges().getEntryNumber();
                        jeIds += "," + receipt.getJournalEntryForBankCharges().getID();
                    }
                    if (receipt.getJournalEntryForBankInterest() != null) {
                        jeNumber += "<br>" + receipt.getJournalEntryForBankInterest().getEntryNumber();
                        jeIds += "," + receipt.getJournalEntryForBankInterest().getID();
                    }
                    obj.put(Constants.billid, receipt.getID());
                    obj.put(Constants.companyKey, receipt.getCompany().getCompanyID());
                    obj.put("withoutinventory", "");
                    obj.put("transactionNo", receipt.getReceiptNumber());   //payment no
                    obj.put("date", df.format(receipt.getCreationDate()));  //date of delivery order
                    obj.put("journalEntryNo", jeNumber);  //journal entry no
                    obj.put("mergedCategoryData", "Payment Receipt");  //type of data
                    obj.put("personname", customer.getName());
                    obj.put("journalentryid", jeIds);
                    obj.put("paymentwindowtype", receipt.getPaymentWindowType());
                    
                    HashMap<String, Object> fieldrequestParamsGlobalLevel = new HashMap();
                    HashMap<String, String> customFieldMapGlobalLevel = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMapGlobalLevel = new HashMap<String, String>();
                    fieldrequestParamsGlobalLevel.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParamsGlobalLevel.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId));
                    HashMap<String, String> replaceFieldMapGlobalLevel = new HashMap<String, String>();

                    HashMap<String, Integer> FieldMapGlobalLevel = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParamsGlobalLevel, replaceFieldMapGlobalLevel, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel);
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                    Detailfilter_names.add(Constants.companyKey);
                    Detailfilter_params.add(receipt.getCompany().getCompanyID());
                    Detailfilter_names.add("journalentryId");
                    Detailfilter_params.add((receipt.isIsOpeningBalenceReceipt()) ? "" : receipt.getJournalEntry().getID());
                    Detailfilter_names.add("moduleId");
                    Detailfilter_params.add(Constants.Acc_Receive_Payment_ModuleId + "");
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accReceiptDAOobj.getReciptPaymentGlobalCustomData(invDetailRequestParams);
                    if (idcustresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeCustom, FieldMapGlobalLevel, replaceFieldMapGlobalLevel, variableMap);
                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            if (customFieldMapGlobalLevel.containsKey(varEntry.getKey())) {
                                String value = "";
                                String Ids[] = coldata.split(",");
                                for (int i = 0; i < Ids.length; i++) {
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                    if (fieldComboData != null) {
                                        if (fieldComboData.getField().getFieldtype() == 12 || fieldComboData.getField().getFieldtype() == 7) {
                                            value += Ids[i] != null ? Ids[i] + "," : ",";
                                        } else {
                                            value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                            obj.put("Dimension_" + fieldComboData.getField().getFieldlabel(), fieldComboData.getValue() != null ? fieldComboData.getValue() : ""); //to differentiate custom field and dimension in sms payment templates.
                                        }

                                    }
                                }
                                if (!StringUtil.isNullOrEmpty(value)) {
                                    value = value.substring(0, value.length() - 1);
                                }
                                obj.put(varEntry.getKey(), value);
                            } else if (customDateFieldMapGlobalLevel.containsKey(varEntry.getKey())) {
                                DateFormat userDateFormat = (DateFormat) authHandler.getDateFormatter(request);
                                DateFormat defaultDateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
                                Date dateFromDB = null;
                                try {
                                    dateFromDB = defaultDateFormat.parse(coldata);
                                    coldata = userDateFormat.format(dateFromDB);

                                } catch (Exception e) {
                                }
                                obj.put(varEntry.getKey(), coldata);
                            } else {
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    String[] coldataArray = coldata.split(",");
                                    String Coldata = "";
                                    for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                        Coldata += "'" + coldataArray[countArray] + "',";
                                    }
                                    Coldata = Coldata.substring(0, Coldata.length() - 1);
                                    String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
                                    obj.put(varEntry.getKey(), coldata);
                                    obj.put(varEntry.getKey() + "_Values", ColValue);
                                }
                            }
                        }
                    }
                    obj.put("type", type);
                    jArr.put(obj);

                }


                /*
                 * ----- Delivery Order Details when linked info.(Start) ------
                 */

                KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrdersMerged(requestParams);
                List listd = doresult.getEntityList();
                Iterator itr1 = listd.iterator();
                while (itr1.hasNext()) {
                    int contractstatus = 0;
                    String contractId = "";
                    //SalesOrder salesOrder=(SalesOrder)itr.next();
                    Object[] oj = (Object[]) itr1.next();
                    String orderid = oj[0].toString();
                    JSONObject obj = new JSONObject();
                    //Withoutinventory 0 for normal, 1 for billing
                    boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());

                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), orderid);
                    DeliveryOrder deliveryOrder = (DeliveryOrder) objItr.getEntityList().get(0);
                    int type=1; // SO->DO
                    boolean includeprotax = false;
                    Set<String> invoiceno = new HashSet<String>();
                    Customer customer = deliveryOrder.getCustomer();
                    obj.put("billid", deliveryOrder.getID());
                    obj.put("companyid", deliveryOrder.getCompany().getCompanyID());
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("transactionNo", deliveryOrder.getDeliveryOrderNumber());   //delivery order no
                    //obj.put("duedate", authHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
                    obj.put("date", authHandler.getDateOnlyFormat(request).format(deliveryOrder.getOrderDate()));  //date of delivery order
                    if (deliveryOrder.isLeaseDO()) {
                        obj.put("mergedCategoryData", "Lease Delivery Order");
                    } else {
                        obj.put("mergedCategoryData", "Delivery Order");
                    }
                     //type of data
                    obj.put("personname", customer.getName());
                    obj.put("companyname", deliveryOrder.getCompany().getCompanyName());
                    obj.put(Constants.IsRoundingAdjustmentApplied, deliveryOrder.isIsRoundingAdjustmentApplied());
                    obj.put("externalcurrencyrate", deliveryOrder.getExternalCurrencyRate());
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("personid", customer.getID());
                    obj.put("billno", deliveryOrder.getDeliveryOrderNumber());
                    obj.put("isFromPOS", deliveryOrder.isPOSDO());
                    if (deliveryOrder.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(deliveryOrder.getModifiedby()));
                    }
                    obj.put("personname", customer.getName());
                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                    addressParams.put("companyid", deliveryOrder.getCompany().getCompanyID());
                    addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                    addressParams.put("isBillingAddress", true);    //true to get billing address
                    addressParams.put("customerid", customer.getID());
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                    obj.put("personemail", customerAddressDetails != null ? customerAddressDetails.getEmailID() : "");
                    obj.put("customername", customer.getName());
                    obj.put("aliasname", customer.getAliasname());
                    obj.put("customercode", customer.getAcccode() == null ? "" : customer.getAcccode());
                    obj.put("billtoaddress", deliveryOrder.getBillingShippingAddresses() != null ? CommonFunctions.getBillingShippingAddress(deliveryOrder.getBillingShippingAddresses(), true) : "");
                    obj.put("shiptoaddress", deliveryOrder.getBillingShippingAddresses() != null ? CommonFunctions.getBillingShippingAddress(deliveryOrder.getBillingShippingAddresses(), false) : "");
                    obj.put("dateinuserformat", authHandler.getUserDateFormatter(request).format(deliveryOrder.getOrderDate()));
                    obj.put("createdby", deliveryOrder.getCreatedby() == null ? "" : StringUtil.getFullName(deliveryOrder.getCreatedby()));
                    obj.put("termdays", customer.getCreditTerm() == null ? 0 : customer.getCreditTerm().getTermdays());
                    obj.put("salesPerson", deliveryOrder.getSalesperson() == null ? "" : deliveryOrder.getSalesperson().getID());
                    obj.put("salesPersonCode", deliveryOrder.getSalesperson() == null ? "" : deliveryOrder.getSalesperson().getCode());
                    obj.put("createdby", deliveryOrder.getCreatedby() == null ? "" : deliveryOrder.getCreatedby().getFullName());
                    obj.put("mapSalesPersonName", deliveryOrder.getSalesperson() == null ? "" : deliveryOrder.getSalesperson().getValue());
                    obj.put("termname", customer.getCreditTerm() == null ? 0 : customer.getCreditTerm().getTermname());
                    obj.put("memo", deliveryOrder.getMemo());
                    obj.put("posttext", deliveryOrder.getPostText() == null ? "" : deliveryOrder.getPostText());
                    obj.put("costcenterid", deliveryOrder.getCostcenter() == null ? "" : deliveryOrder.getCostcenter().getID());
                    obj.put("costcenterName", deliveryOrder.getCostcenter() == null ? "" : deliveryOrder.getCostcenter().getName());
                    obj.put("statusID", deliveryOrder.getStatus() == null ? "" : deliveryOrder.getStatus().getID());
                    obj.put("status", deliveryOrder.getStatus() == null ? "" : deliveryOrder.getStatus().getValue());
                    if (deliveryOrder.isIsDOClosed()) {
                        obj.put("status", Constants.closedStatus);
                    }
                    obj.put("shipdate", deliveryOrder.getShipdate() == null ? "" : authHandler.getDateOnlyFormat().format(deliveryOrder.getShipdate()));
                    obj.put("shipvia", deliveryOrder.getShipvia() == null ? "" : deliveryOrder.getShipvia());
                    obj.put("fob", deliveryOrder.getFob() == null ? "" : deliveryOrder.getFob());
                    obj.put("isfavourite", deliveryOrder.isFavourite());
                    obj.put("isprinted", deliveryOrder.isPrinted());
                    obj.put("isAppliedForTax", deliveryOrder.isAppliedForTax());
                    obj.put("isautogenerateddo", deliveryOrder.isIsAutoGeneratedDO());
                    obj.put("deleted", deliveryOrder.isDeleted());
                    obj.put("currencyid", (deliveryOrder.getCurrency() == null ? "" : deliveryOrder.getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (deliveryOrder.getCurrency() == null ? "" : deliveryOrder.getCurrency().getSymbol()));
                    obj.put("currencyCode", (deliveryOrder.getCurrency() == null ? "" : deliveryOrder.getCurrency().getCurrencyCode()));
                    obj.put(Constants.SEQUENCEFORMATID, deliveryOrder.getSeqformat() != null ? deliveryOrder.getSeqformat().getID() : "");
                    obj.put("isConsignment", deliveryOrder.isIsconsignment());
                    obj.put("isLeaseFixedAsset", deliveryOrder.isLeaseDO());
                    obj.put("isFixedAsset", deliveryOrder.isFixedAssetDO());
                    if (deliveryOrder.getCustWarehouse() != null) {
                        obj.put("custWarehouse", deliveryOrder.getCustWarehouse().getId());
                    }
                    obj.put("driver", deliveryOrder.getDriver() != null ? deliveryOrder.getDriver().getID() : "");
                    if (deliveryOrder.isIsconsignment()) {
                        obj.put("movementtype", deliveryOrder.getMovementType() != null ? deliveryOrder.getMovementType().getID() : "");
                        obj.put("movementtypename", deliveryOrder.getMovementType() != null ? deliveryOrder.getMovementType().getValue() : "");
                    }
                    BillingShippingAddresses addresses = deliveryOrder.getBillingShippingAddresses();
                    AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    obj.put("includeprotax", includeprotax);
                    Set<DOContractMapping> doContractMapping = deliveryOrder.getdOContractMappings();
                    if (doContractMapping != null && !doContractMapping.isEmpty()) {
                        for (DOContractMapping docomContractMapping : doContractMapping) {
                            if (docomContractMapping.getContract() != null && docomContractMapping.getContract().getCstatus() == 2) {
                                contractstatus = docomContractMapping.getContract().getCstatus();
                            }
                            contractId = docomContractMapping.getContract().getID();
                        }
                    }

                    obj.put("contractstatus", contractstatus);
                    obj.put("contract", contractId);

                    String invoiceNumbers = org.springframework.util.StringUtils.collectionToCommaDelimitedString(invoiceno);
                    KwlReturnObject invoiceResult = accInvoiceDAOobj.getInvoiceNumbersOfDO(deliveryOrder.getID(), deliveryOrder.getCompany().getCompanyID());
                    List invoiceList = invoiceResult.getEntityList();
                    Iterator invoiceItr = invoiceList.iterator();
                    while (invoiceItr.hasNext()) {
                        Invoice invoice = (Invoice) invoiceItr.next();
                        if (invoiceNumbers.length() > 1) {
                            invoiceNumbers += "," + invoice.getInvoiceNumber();
                        } else {
                            invoiceNumbers = invoice.getInvoiceNumber();
                        }
                    }
                    obj.put("invoicenumber", invoiceNumbers);
                    obj.put("invoiceno", invoiceNumbers);
                    obj.put("approvalstatusinfo", deliveryOrder.getApprovestatuslevel() == -1 ? "Rejected" : deliveryOrder.getApprovestatuslevel() < 11 ? "Waiting for Approval at Level - " + deliveryOrder.getApprovestatuslevel() : "Approved");
                    obj.put("approvalstatus", deliveryOrder.getApprovestatuslevel());
                    obj.put("type", type);
                    jArr.put(obj);
                }
                /*
                 * ----- Delivery Order Details when linked info.(Ends) ------
                 */

                //SO Linked in PurchaseOrder
                KwlReturnObject resultpo = accInvoiceDAOobj.getPurchaseOrderMerged(requestParams);
                List listso = resultpo.getEntityList();
                resultpo = accInvoiceDAOobj.getPOlinkedInSO(requestParams);
                List listso1 = resultpo.getEntityList();
                if(listso1!=null && listso1.size()>0){
                    listso.addAll(listso1);
                }
                Iterator itrso = listso.iterator();
                while (itrso.hasNext()) {
                    Object[] oj = (Object[]) itrso.next();
                    String orderid = oj[0].toString();
                    BigInteger type = (BigInteger) oj[1];
                    
                    JSONObject obj = new JSONObject();

                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), orderid);
                    PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
                    Vendor vendor = purchaseOrder.getVendor();
                    obj.put("billid", purchaseOrder.getID());
                    obj.put("companyid", purchaseOrder.getCompany().getCompanyID());
                    obj.put("companyname", purchaseOrder.getCompany().getCompanyName());
                    obj.put("withoutinventory", false);
                    obj.put(Constants.IsRoundingAdjustmentApplied, purchaseOrder.isIsRoundingAdjustmentApplied());
                    obj.put("personid", vendor.getID());
                    obj.put("transactionNo", purchaseOrder.getPurchaseOrderNumber());
                    obj.put("duedate", df.format(purchaseOrder.getDueDate()));
                    obj.put("date", df.format(purchaseOrder.getOrderDate()));
                    obj.put("personname", vendor.getName());
                    if(purchaseOrder.getCurrency() != null){
                        currency = purchaseOrder.getCurrency();
                    } else {
                        currency=purchaseOrder.getVendor().getAccount().getCurrency()==null?kwlcurrency:purchaseOrder.getVendor().getAccount().getCurrency();
                    }
                    obj.put("externalcurrencyrate", purchaseOrder.getExternalCurrencyRate());
                    obj.put("isOpeningBalanceTransaction", purchaseOrder.isIsOpeningBalancePO());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("currencycode", currency.getCurrencyCode()==null?"":currency.getCurrencyCode());
                    obj.put("personid", vendor.getID());
                    obj.put("aliasname", vendor.getAliasname());
                    obj.put("personcode", vendor.getAcccode()==null?"":vendor.getAcccode());
                    obj.put("createdby", purchaseOrder.getCreatedby()==null?"":StringUtil.getFullName(purchaseOrder.getCreatedby()));
                    obj.put("billtoaddress", purchaseOrder.getBillingShippingAddresses()==null? "":CommonFunctions.getBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), true));
                    obj.put("shiptoaddress", purchaseOrder.getBillingShippingAddresses()==null? "":CommonFunctions.getBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), false));
                    obj.put("personemail", vendor.getEmail());
                    obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                    obj.put("duedate", df.format(purchaseOrder.getDueDate()));
                    obj.put("date", df.format(purchaseOrder.getOrderDate()));
                    obj.put("dateinuserformat", userdf.format(purchaseOrder.getOrderDate()));
                    obj.put("shipdate", purchaseOrder.getShipdate()==null? "" : df.format(purchaseOrder.getShipdate()));
                    obj.put("shipdateinuserformat", purchaseOrder.getShipdate()==null? "" : userdf.format(purchaseOrder.getShipdate()));
                    obj.put("shipvia", purchaseOrder.getShipvia()==null? "" : purchaseOrder.getShipvia());
                    obj.put("fob", purchaseOrder.getFob()==null?"" : purchaseOrder.getFob());
                    obj.put("isfavourite", purchaseOrder.isFavourite());
                    obj.put("isprinted", purchaseOrder.isPrinted());
                    obj.put("deleted", purchaseOrder.isDeleted());
                    obj.put("billto", purchaseOrder.getBillTo()==null?"":purchaseOrder.getBillTo());
                    obj.put("shipto", purchaseOrder.getShipTo()==null?"":purchaseOrder.getShipTo());
                    obj.put("agent", purchaseOrder.getMasteragent()==null ? "" : purchaseOrder.getMasteragent().getID());
                    if (purchaseOrder.getApprover() != null) {
                        obj.put("approver", StringUtil.getFullName(purchaseOrder.getApprover()));
                    }
                    boolean gstIncluded = purchaseOrder.isGstIncluded();
                    obj.put("gstIncluded", gstIncluded);
                    obj.put("isConsignment", purchaseOrder.isIsconsignment());
                    obj.put("termid", purchaseOrder.getTerm()==null?"":purchaseOrder.getTerm().getID());
                    obj=AccountingAddressManager.getTransactionAddressJSON(obj,purchaseOrder.getBillingShippingAddresses(),true);
                    obj.put("termdays", purchaseOrder.getTerm()==null ? 0:purchaseOrder.getTerm().getTermdays());
                    obj.put("termname", purchaseOrder.getTerm()==null ? 0:purchaseOrder.getTerm().getTermname());
                    obj.put("personname", vendor.getName());
                    obj.put("memo", purchaseOrder.getMemo());
                    obj.put("posttext",purchaseOrder.getPostText());
                    obj.put("taxid", purchaseOrder.getTax()==null?"":purchaseOrder.getTax().getID());
                    obj.put("taxname", purchaseOrder.getTax()==null?"":purchaseOrder.getTax().getName());
                    obj.put("costcenterid", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getID());
                    obj.put("costcenterName", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getName());
                    obj.put("shiplengthval", purchaseOrder.getShiplength());
                    obj.put("invoicetype", purchaseOrder.getInvoicetype());
                    if (purchaseOrder.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(purchaseOrder.getModifiedby()));
                    } 
                    //obj.put("termdetails", getTermDetails(purchaseOrder.getID(),true));
                    if(purchaseOrder.getTermsincludegst()!=null) {
                        obj.put(Constants.termsincludegst, purchaseOrder.getTermsincludegst());
                    }
                   // obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(purchaseOrder.getID(),true)));  
                    obj.put(Constants.SEQUENCEFORMATID,purchaseOrder.getSeqformat()==null?"":purchaseOrder.getSeqformat().getID()); 
                    
                    obj.put("mergedCategoryData", "Purchase Order");  //type of data
                    obj.put("type",type.intValue());
                    jArr.put(obj);
                }

                /**
                 * Master Contract Linked in So
                 */
                jArr=accContractManagementServiceDAOObj.getMasterContractLinkingInformation(requestParams,jArr);
                
                
                // CQ linked in Sales Order 
                KwlReturnObject resultcq = accInvoiceDAOobj.getCQlinkedInSO(requestParams);
                List listcq = resultcq.getEntityList();
                if(listcq!=null && listcq.size()>0){
                    int type = 4; // CQ->SO
                    jArr=accSalesOrderServiceDAOobj.getCustomerQuotationJsonForLinking(jArr, listcq, currency, userdf, df, type);
                }
   
                jobj.put("count", jArr.length());
                jobj.put("data", jArr);

            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    
    public ModelAndView getCQLinkedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accSalesOrderServiceDAOobj.getCQLinkedInTransaction(paramJobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getCQLinkedInTransaction:" + ex.getMessage();
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
    
    public ModelAndView exportSalesOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";
        try{
            JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> requestParams = accSalesOrderServiceDAOobj.getSalesOrdersMapJson(paramJobj);
            String companyid = paramJobj.getString(Constants.companyKey);
            boolean consolidateFlag = paramJobj.optString(Constants.consolidateFlag, null) != null ? Boolean.parseBoolean(paramJobj.optString(Constants.consolidateFlag, null)) : false;
            boolean isOutstanding = paramJobj.optString(Constants.isOutstanding,null)!=null?Boolean.parseBoolean(paramJobj.getString(Constants.isOutstanding)):false;
            boolean isPendingInvoiced = request.getParameter("isPendingInvoiced")!=null?Boolean.parseBoolean(request.getParameter("isPendingInvoiced")):false;
            boolean isOutstandingproduct = paramJobj.optString(Constants.isOuststandingProduct,null)!=null?Boolean.parseBoolean(paramJobj.getString(Constants.isOuststandingProduct)):false; 
            String[] companyids = (consolidateFlag && paramJobj.optString(Constants.companyids, null) != null) ? paramJobj.optString(Constants.companyids, "").split(",") : companyid.split(",");
            String gcurrencyid = (consolidateFlag && paramJobj.optString(Constants.globalCurrencyKey, null) != null) ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.globalCurrencyKey);
            boolean isForTemplate = false;
            boolean ispendingAproval = paramJobj.optString(Constants.isPendingApproval,null)!=null?Boolean.parseBoolean(paramJobj.optString(Constants.isPendingApproval,"")):false;
            boolean isConsignment = paramJobj.optString(Constants.isConsignment,null)!=null?Boolean.parseBoolean(paramJobj.optString(Constants.isConsignment,"")):false;
            String billid = "";
            String dir = "";
            String sort = "";
            requestParams.put(Constants.isPendingApproval, ispendingAproval);
            if (isConsignment) {
                requestParams.put(Constants.isConsignment, isConsignment);
            }
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            } else if (isConsignment && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.requestStatus, null)) && !paramJobj.optString(Constants.requestStatus, "All").equals("All")) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.dir, null)) && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sort, null))) {
                dir = paramJobj.getString(Constants.dir);
                sort = paramJobj.getString(Constants.sort);
                requestParams.put(Constants.sort, sort);
                requestParams.put(Constants.dir, dir);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.requestStatus))) {
                requestParams.put(Constants.status, request.getParameter(Constants.requestStatus));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isForTemplate, null))) {
                isForTemplate = Boolean.parseBoolean(paramJobj.getString(Constants.isForTemplate));
                requestParams.put(Constants.isForTemplate, isForTemplate);
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.billId, null))) {
                billid = paramJobj.getString(Constants.billId);
                requestParams.put(Constants.billId, billid);
            }
            Boolean isTradingFlow = false;
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            if (pref != null && pref.isWithInvUpdate()) {
                isTradingFlow = true;
            }
            requestParams.put(Constants.isTradingFlow, isTradingFlow);
            requestParams.put(Constants.isOutstanding, isOutstanding);
            requestParams.put(Constants.isPendingInvoiced, isPendingInvoiced);
            requestParams.put(Constants.isOuststandingProduct, isOutstandingproduct);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has permission to view all customers documents,so at that time there is need to filter record according to user&salesperson. 
                     */
                    String userId = sessionHandlerImpl.getUserid(request);
                    requestParams.put(Constants.useridKey, userId);
                    requestParams.put(Constants.enableSalesPersonAgentFlow, extraPref.isEnablesalespersonAgentFlow());
                }
                Map<String, Object> salesPersonParams = new HashMap<>();
                salesPersonParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                salesPersonParams.put(Constants.companyKey, companyid);
                salesPersonParams.put("grID", "15");
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
                    requestParams.put(Constants.salesPersonid, salesPersons);
                }
            }
            KwlReturnObject result = null;
            companyid = "";
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];                
                request.setAttribute(Constants.companyKey, companyid);
                request.setAttribute(Constants.globalCurrencyKey, gcurrencyid);
                request.setAttribute("isExport", true);
                paramJobj.put(Constants.companyKey, companyid);
                paramJobj.put(Constants.globalCurrencyKey, gcurrencyid);
                paramJobj.put("isExport", true);
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
                requestParams.put("salesPersonFilterFlag", true); //To export no of records as per userid  
                int orderLinkedWithDocType = Integer.parseInt(request.getParameter("orderLinkedWithDocType"));
                requestParams.put("orderLinkedWithDocType", orderLinkedWithDocType);
                if (orderLinkedWithDocType != 0) {
                    result = accSalesOrderDAOobj.getRelevantSalesOrderLinkingWise(requestParams);
                } else if (!isOutstanding && !isOutstandingproduct && !isPendingInvoiced) {
                    result = accSalesOrderDAOobj.getSalesOrdersMerged(requestParams);
                } else {
                    result = accSalesOrderDAOobj.getOutstandingSalesOrders(requestParams);
                }
                
                DataJArr = accSalesOrderServiceDAOobj.getSalesOrdersJsonMerged(paramJobj, result.getEntityList(), DataJArr);
                if (paramJobj.optString("type",null) != null && paramJobj.optString("type","detailedXls").equals("detailedXls")) {
                    DataJArr = getDetailExcelJsonSalesOrder(paramJobj, response, requestParams, DataJArr);
                }
            }
            jobj.put("data", DataJArr);
            String fileType = paramJobj.optString("filetype",null);
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(paramJobj).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }else if(fileType.equals("xls")){
               request.setAttribute("isSummaryXls", true);
               paramJobj.put("isSummaryXls", true);
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    //Export SalesOrder For CustomReportBuilder
    public ModelAndView exportSalesOrderforCustomReportBuilder(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        HashMap<String, Object> requestParams = new HashMap();
        String view = "jsonView_ex";
        try {
            String reportID = request.getParameter("reportID");
            String start = request.getParameter("start") != null ? request.getParameter("start") : "";
            String limit = request.getParameter("limit") != null ? request.getParameter("limit") : "";
            Boolean exportInCaps = (request.getParameter("exportInCaps") == null) ? false : Boolean.parseBoolean(request.getParameter("exportInCaps"));
            String fromDate = request.getParameter("fromDate") != null ? request.getParameter("fromDate") : null;
            String toDate = request.getParameter("toDate") != null ? request.getParameter("toDate") : null;
            boolean showRowLevelFieldsflag = Boolean.valueOf((String) request.getParameter("showRowLevelFieldsflag"));
            String filter = request.getParameter("filter") != null ? request.getParameter("filter") : "[]";
            String companyID = AccountingManager.getCompanyidFromRequest(request);
            String eWayFilter = request.getParameter("ewayFilter") != null ? (request.getParameter("ewayFilter")) : "";
            String searchJson = request.getParameter("searchJson") != null ? (request.getParameter("searchJson")) : "";
            boolean isreportloaded = request.getParameter("isreportloaded") != null ? Boolean.parseBoolean(request.getParameter("isreportloaded")) : false;
            DateFormat sdf = new SimpleDateFormat(sessionHandlerImpl.getUserDateFormat(request));
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;           
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);   
            requestParams.put("gcurrencyid", gcurrencyid);
            requestParams.put("reportID", reportID);
            requestParams.put("userdf", sdf);
            requestParams.put("companyID", companyID);
            requestParams.put("eWayFilter", eWayFilter);
            requestParams.put("searchJson", searchJson);
            requestParams.put("isreportloaded", isreportloaded);
            requestParams.put("deleted", request.getParameter("deleted"));
            requestParams.put("nondeleted", request.getParameter("nondeleted"));
            requestParams.put("pendingapproval", request.getParameter("pendingapproval"));
            requestParams.put("userDateFormat", sessionHandlerImpl.getUserDateFormat(request));
            requestParams.put("df", authHandler.getOnlyDateFormat(request));
            requestParams.put("df1", authHandler.getOnlyDateFormat(request));
            requestParams.put("start", start);
            requestParams.put("limit", limit);
            requestParams.put("exportInCaps", exportInCaps);
            requestParams.put("fromDate", fromDate);
            requestParams.put("toDate", toDate);
            requestParams.put("showRowLevelFieldsflag", showRowLevelFieldsflag);
            boolean isLeaseFixedAsset = request.getParameter("isLeaseFixedAsset")!=null?Boolean.valueOf((String)request.getParameter("isLeaseFixedAsset")):false;
            requestParams.put("isLeaseFixedAsset", isLeaseFixedAsset);
            if (!StringUtil.isNullOrEmpty((String) request.getParameter("billid"))) {
                String billid = (String) request.getParameter("billid");
                String[] recarr = billid.split(",");
                StringBuilder recordsbuffer = new StringBuilder();
                String recordids = "";
                for (int i = 0; i < recarr.length; i++) {
                    recordsbuffer.append("'" +recarr[i]+"'").append(",");
                }
                if (recordsbuffer.length() > 0) {
                    recordids = recordsbuffer.substring(0, (recordsbuffer.length() - 1));
                    requestParams.put("billid", recordids);
                }
            }
            requestParams.put("filter", filter);
            requestParams.put("forExport",true);
            JSONArray customReportDataJarr = accCustomReportService.executeCustomReport(requestParams).optJSONArray("data");
            requestParams.put("showRowLevelFieldsflag", true);
            requestParams.put("df", new SimpleDateFormat(Constants.MMMMdyyyy));
            requestParams.put(Constants.dateformatid, sessionHandlerImpl.getDateFormatID(request));
            KWLTimeZone timeZone =(KWLTimeZone) kwlCommonTablesDAOObj.getClassObject(KWLTimeZone.class.getName(), storageHandlerImpl.getDefaultTimeZoneID());
            requestParams.put(Constants.timezonedifference, timeZone.getDifference());
            JSONArray customReportLineDataJarr = accCustomReportService.executeCustomReport(requestParams).optJSONArray("data");
            if ((Boolean) customReportDataJarr.getJSONObject(0).getJSONArray("metaData").getJSONObject(0).get("success") == true && (Boolean) customReportLineDataJarr.getJSONObject(0).getJSONArray("metaData").getJSONObject(0).get("success") == false) {
                jobj.put("data", customReportDataJarr.getJSONObject(1).getJSONArray("reportdata"));
            } else {
                JSONObject DataJson = getDetailExcelJsonCustomSalesOrder(customReportDataJarr, customReportLineDataJarr);
                jobj.put("data", DataJson.getJSONArray("reportdata"));
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    //Arrange data how to show in xlsx
    public JSONObject getDetailExcelJsonCustomSalesOrder(JSONArray customReportDataJarr, JSONArray customReportLineDataJarr) throws SessionExpiredException, JSONException, ServiceException, SessionExpiredException {
        JSONArray tempArray = new JSONArray();
        JSONArray DataJArr = new JSONArray();
        JSONArray DataRowsArr = new JSONArray();
        JSONObject jarrRecordsObject = new JSONObject();
        DataJArr = customReportDataJarr.getJSONObject(1).getJSONArray("reportdata");
        DataRowsArr = customReportLineDataJarr.getJSONObject(1).getJSONArray("reportdata");
        for (int i = 0; i < DataJArr.length(); i++) {
            JSONObject rowjobj = new JSONObject();
            rowjobj = DataJArr.getJSONObject(i);
            String billid = rowjobj.optString("billid", "");
            tempArray.put(rowjobj);
            int count = 1;
            for (int j = 0; j < DataRowsArr.length(); j++) {
                if (DataRowsArr.getJSONObject(j).optString("billid", "").equals(billid)) {
                    JSONObject tempjobj = new JSONObject();
                    tempjobj = DataRowsArr.getJSONObject(j);
                    JSONObject checkingObj = new JSONObject(tempjobj.toString());
                    checkingObj.remove("recordCount");
                    checkingObj.remove("currencysymbol");
                    checkingObj.remove("currencycode");
                    checkingObj.remove("billid");
                    checkingObj.remove("reportID");
                    if(checkingObj.length() > 0){
                        tempjobj.put("S.No.", count);
                        tempArray.put(tempjobj);
                        count++;
                    }
                }
            }
        }
        jarrRecordsObject.put("reportdata", tempArray);
        return jarrRecordsObject;
    }
 public JSONArray getDetailExcelJsonSalesOrder(JSONObject paramJobj, HttpServletResponse response, HashMap<String, Object> requestParams, JSONArray DataJArr) throws SessionExpiredException, JSONException, ServiceException, SessionExpiredException {
        boolean consolidateFlag = (paramJobj.optString("consolidateFlag",null) != null) ? Boolean.parseBoolean((String)paramJobj.get("consolidateFlag")) : false;
        String[] companyids = (consolidateFlag && !StringUtil.isNullOrEmpty(paramJobj.optString("companyids",null))) ? (String[])paramJobj.getString("companyids").split(",") : (String[])paramJobj.getString(Constants.companyid).split(",");
        String gcurrencyid = (consolidateFlag && paramJobj.optString(Constants.globalCurrencyKey,null)!= null) ?paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.globalCurrencyKey);
        String companyid = companyids[0];
        JSONArray tempArray = new JSONArray();
        for (int i = 0; i < DataJArr.length(); i++) {
            JSONObject rowjobj = new JSONObject();
            rowjobj = DataJArr.getJSONObject(i);
            String billid = rowjobj.optString("billid", "");   
            paramJobj.put(Constants.companyid, companyid);
            paramJobj.put(Constants.globalCurrencyKey, gcurrencyid);
            paramJobj.put("billid", billid);
            JSONArray DataRowsArr = null;
            try {
                DataRowsArr = accSalesOrderServiceDAOobj.getSalesOrderRows(paramJobj).getJSONArray("data");
            } catch (ParseException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
            tempArray.put(rowjobj);
            for (int j = 0; j < DataRowsArr.length(); j++) {
                JSONObject tempjobj = new JSONObject();
                tempjobj = DataRowsArr.getJSONObject(j);
                exportDaoObj.editJsonKeyForExcelFile(tempjobj,Constants.Acc_Sales_Order_ModuleId);
                tempArray.put(tempjobj);
            }
        }
        return tempArray;
    } 
    
    public ModelAndView getQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        boolean quotationFilterFlag=false;
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            boolean isForTemplate = false;
            if(!StringUtil.isNullOrEmpty(request.getParameter("isForTemplate"))){
                isForTemplate = Boolean.parseBoolean(request.getParameter("isForTemplate"));
            }
            requestParams.put("isForTemplate", isForTemplate);
             ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has permission to view all customers documents,so at that time there is need to filter record according to user&salesperson. 
                     */
                    String userId = sessionHandlerImpl.getUserid(request);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
                Map<String, Object> salesPersonParams = new HashMap<>();

                salesPersonParams.put("userid", sessionHandlerImpl.getUserid(request));
                salesPersonParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                salesPersonParams.put("grID", "15");
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
            if(StringUtil.isNullOrEmpty(request.getParameter("archieve"))){
                requestParams.put("archieve", 0);
            } else {
                requestParams.put("archieve", Integer.parseInt(request.getParameter("archieve")));
            }            
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;            
            String[] companyids = (consolidateFlag && request.getParameter("companyids")!=null)?request.getParameter("companyids").split(","):sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);            
            
            requestParams.put("closeflag", request.getParameter("closeflag"));
//            String soflag = "";// Not used anywhere
            if (!StringUtil.isNullOrEmpty(request.getParameter("sopolinkflag")) && request.getParameter("sopolinkflag") != null) {
                requestParams.put("sopolinkflag", request.getParameter("sopolinkflag"));
            }else{
                requestParams.put("sopolinkflag", "false");
            }
//            requestParams.put("sopolinkflag", soflag);
            requestParams.put("linkFlagInSO", request.getParameter("linkFlagInSO"));  // Check wether quotation is link with SO 
            requestParams.put("linkFlagInInv", request.getParameter("linkFlagInInv"));  // Check wether quotation is link with  Invoice

            boolean eliminateflag = consolidateFlag;
            if(consolidateFlag) {
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
            
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("linknumber"))){
                requestParams.put("linknumber", request.getParameter("linknumber")); 
            }
            KwlReturnObject result = null;
            String companyid = "";
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                boolean salesPersonFilterFlag = request.getParameter("salesPersonFilterFlag")!=null?Boolean.parseBoolean(request.getParameter("salesPersonFilterFlag")):false;
                if (salesPersonFilterFlag) {
                    requestParams.put("salesPersonFilterFlag", salesPersonFilterFlag);
                }
                requestParams.put("userId", sessionHandlerImpl.getUserid(request));
//                KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
//                ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
                if (request.getParameter("requestModuleid") != null && !StringUtil.isNullOrEmpty(request.getParameter("requestModuleid"))) {
                    int requestModuleID = Integer.parseInt(request.getParameter("requestModuleid"));
                    if (extraPref.isEnableLinkToSelWin()) {
                        requestParams.put("requestModuleid", requestModuleID);
                    }
                    if (extraPref.isEnableLinkToSelWin() && !Boolean.parseBoolean(request.getParameter("isGrid")) && (requestModuleID == Constants.Acc_Invoice_ModuleId || requestModuleID == Constants.Acc_Sales_Order_ModuleId || requestModuleID== Constants.Acc_Lease_Order_ModuleId)) {
                        requestParams.put("start", "0");
                        requestParams.put("limit", "10");
                    }
                }
                if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
                    requestParams.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
                }
           
                /*------customerQuotationsWithInvoiceAndDOStatus parameter comes when we apply linking filter in Quotation Report----- */
                if (!StringUtil.isNullOrEmpty(request.getParameter("customerQuotationsWithInvoiceAndDOStatus")) && request.getParameter("customerQuotationsWithInvoiceAndDOStatus") != null) {
                    requestParams.put("customerQuotationsWithInvoiceAndDOStatus", request.getParameter("customerQuotationsWithInvoiceAndDOStatus"));
                    if (!request.getParameter("customerQuotationsWithInvoiceAndDOStatus").equalsIgnoreCase("0")) {
                        quotationFilterFlag = true;
                    }
                }
                result = accSalesOrderDAOobj.getQuotations(requestParams);
                DataJArr = accSalesOrderServiceDAOobj.getQuotationsJson(requestParams, result.getEntityList(), DataJArr);
            }
            int cnt = (quotationFilterFlag || consolidateFlag)?DataJArr.length():result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            if(consolidateFlag || quotationFilterFlag) {
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
    
    public ModelAndView getVersionQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
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
                requestParams.put("versionid",request.getParameter("versionid"));
                result = accSalesOrderDAOobj.getVersionQuotations(requestParams);
                DataJArr = getVersionQuotationsJson(request, result.getEntityList(), DataJArr);
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
    
    public JSONArray getVersionQuotationsJson(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException {
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get("companyid");
            boolean closeflag = Boolean.TRUE.parseBoolean(request.getParameter("closeflag"));
            boolean soflag = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("sopolinkflag")) && request.getParameter("sopolinkflag") != null) {
                soflag = Boolean.FALSE.parseBoolean(request.getParameter("sopolinkflag"));
            }
            boolean linkFlagInSO = Boolean.FALSE.parseBoolean(request.getParameter("linkFlagInSO"));  // Check wether quotation is link with SO 
            boolean linkFlagInInv = Boolean.FALSE.parseBoolean(request.getParameter("linkFlagInInv"));  // Check wether quotation is link with  Invoice
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Customer_Quotation_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                String qid = (String) itr.next();
                if (!StringUtil.isNullOrEmpty(qid)) {
                    KwlReturnObject reqResult = accountingHandlerDAOobj.getObject(QuotationVersion.class.getName(), qid);
                    QuotationVersion salesOrder = (QuotationVersion) reqResult.getEntityList().get(0);
                    KWLCurrency currency = null;

                    if (salesOrder.getCurrency() != null) {
                        currency = salesOrder.getCurrency();
                    } else {
                        currency = salesOrder.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : salesOrder.getCustomer().getAccount().getCurrency();
                    }
                    Customer customer = salesOrder.getCustomer();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", salesOrder.getID());
                    obj.put("companyid", salesOrder.getCompany().getCompanyID());
                    obj.put("companyname", salesOrder.getCompany().getCompanyName());
                    obj.put("personid", customer.getID());
                    obj.put("personemail", salesOrder.getCustomer() == null ? "" : salesOrder.getCustomer().getEmail());
                    obj.put("billno", salesOrder.getquotationNumber());
                    obj.put("contract", (salesOrder.getContract() != null) ? salesOrder.getContract().getID() : "");
                    obj.put("duedate", authHandler.getDateOnlyFormat(request).format(salesOrder.getDueDate()));
                    obj.put("date", authHandler.getDateOnlyFormat(request).format(salesOrder.getQuotationDate()));
                    obj.put("shipdate", salesOrder.getShipdate() == null ? "" : authHandler.getDateOnlyFormat(request).format(salesOrder.getShipdate()));
                    obj.put("validdate", salesOrder.getValiddate() == null ? "" : authHandler.getDateOnlyFormat(request).format(salesOrder.getValiddate()));
                    obj.put("shipvia", salesOrder.getShipvia() == null ? "" : salesOrder.getShipvia());
                    obj.put("fob", salesOrder.getFob() == null ? "" : salesOrder.getFob());
                    obj.put("archieve", salesOrder.getArchieve());
                    obj.put("billto", salesOrder.getBillTo());
                    obj.put("shipto", salesOrder.getShipTo());
                    obj.put("deleted", salesOrder.isDeleted());
                    obj.put("salesPerson", salesOrder.getSalesperson() != null ? salesOrder.getSalesperson().getID() : "");
                    obj.put("salespersonname", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getValue());
                    obj.put("isfavourite", salesOrder.isFavourite());
                    obj.put("isprinted", salesOrder.isPrinted());
                    obj.put("termdetails", accSalesOrderServiceDAOobj.getTermDetails(salesOrder.getID(), false));
                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(accSalesOrderServiceDAOobj.getTermDetails(salesOrder.getID(), false)));
                    obj.put("discountval", (salesOrder.getDiscount() == 0) ? 0 : salesOrder.getDiscount());
                    obj.put("gstIncluded", salesOrder.isGstIncluded());
                    obj.put("shiplengthval", salesOrder.getShiplength());
                    obj.put("invoicetype", salesOrder.getInvoicetype());
                    obj.put("quotationtype", salesOrder.getQuotationType());
                    obj.put("approvalstatusinfo", salesOrder.getApprovestatuslevel() == -1 ? "Rejected" : salesOrder.getApprovestatuslevel() < 11 ? "Waiting for Approval at Level - " + salesOrder.getApprovestatuslevel() : "Approved");
                    obj.put("approvalstatus", salesOrder.getApprovestatuslevel());
                    BillingShippingAddresses addresses = salesOrder.getBillingShippingAddresses();
                    AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    obj.put(Constants.SEQUENCEFORMATID, salesOrder.getSeqformat() == null ? "" : salesOrder.getSeqformat().getID());
                    obj.put("version", salesOrder.getVersion());
                    boolean incProTax = false;
                    Iterator itrRow = salesOrder.getRows().iterator();
                    double amount = 0, amountinbase = 0, totalDiscount = 0, discountPrice = 0;
                    while (itrRow.hasNext()) {
                        QuotationVersionDetail sod = (QuotationVersionDetail) itrRow.next();
                        double rowTaxPercent = 0;
                        if (sod.getTax() != null) {
                            requestParams.put("transactiondate", salesOrder.getQuotationDate());
                            requestParams.put("taxid", sod.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            incProTax = true;
                        }
                        KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, sod.getRate(), currency.getCurrencyID(), salesOrder.getQuotationDate(), 0);
                        double qrate = salesOrder.isGstIncluded() ? authHandler.round(sod.getRateincludegst(), companyid) : authHandler.round(sod.getRate(), companyid);
                        double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);
                        double quotationPrice = authHandler.round(quantity * qrate, companyid);
                        double discountQD = authHandler.round(sod.getDiscount(), companyid);
                        if (sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountQD / 100), companyid);;
                        } else {
                            discountPrice = quotationPrice - discountQD;
                        }
                        amount += discountPrice + (salesOrder.isGstIncluded() ? 0 : authHandler.round(sod.getRowTaxAmount(), companyid));//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                    }
                    String status = "";
                    if (linkFlagInSO || linkFlagInInv) {
                        Iterator itr1 = salesOrder.getRows().iterator();
                        status = "Closed";
                        while (itr1.hasNext()) {
                            QuotationDetail row = (QuotationDetail) itr1.next();
                            double addobj = soflag ? accSalesOrderServiceDAOobj.getQuotationDetailStatusSO(row) : accSalesOrderServiceDAOobj.getQuotationDetailStatusINV(row);
                            if (addobj > 0) {
                                status = "Open";
                                break;
                            }
                        }
                    }
                    double discountQ = authHandler.round(salesOrder.getDiscount(), companyid);
                    obj.put("includeprotax", incProTax);
                    if (salesOrder.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(salesOrder.getModifiedby()));
                    }
                    if (discountQ != 0) {
                        if (salesOrder.isPerDiscount()) {
                            totalDiscount = amount * discountQ / 100;
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - discountQ;
                            totalDiscount = discountQ;
                        }
                        obj.put("discounttotal", discountQ);
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    obj.put("discount", totalDiscount);
                    obj.put("discountispertotal", salesOrder.isPerDiscount());
                    obj.put("ispercentdiscount", salesOrder.isPerDiscount());
                    if (salesOrder.getTax() != null) {
                        requestParams.put("transactiondate", salesOrder.getQuotationDate());
                        requestParams.put("taxid", salesOrder.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        double TaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        amountinbase = amount + authHandler.round(amount * TaxPercent / 100, companyid);
                    }	
                    obj.put("amount", amount);
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                    obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                    double taxPercent = 0;
                    double totalTermAmount = 0;
                    HashMap<String, Object> requestParam = new HashMap();
                    requestParam.put("quotation", salesOrder.getID());
                    KwlReturnObject quotationResult = null;
                    quotationResult = accSalesOrderDAOobj.getQuotationTermMap(requestParam);
                    List<QuotationTermMap> termMap = quotationResult.getEntityList();
                    for (QuotationTermMap quotationTermMap : termMap) {
                        InvoiceTermsSales mt = quotationTermMap.getTerm();
                        double termAmnt = quotationTermMap.getTermamount();
                        totalTermAmount += authHandler.round(termAmnt, companyid);
                    }
                    totalTermAmount = authHandler.round(totalTermAmount, companyid);
                    if (salesOrder.getTax() != null) {
                        requestParams.put("transactiondate", salesOrder.getQuotationDate());
                        requestParams.put("taxid", salesOrder.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                    }
                    double orderAmount = amount;//(Double) bAmt.getEntityList().get(0);
                    KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getQuotationDate(), salesOrder.getExternalCurrencyRate());
                    amountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                    KwlReturnObject bAmtTax = accCurrencyobj.getCurrencyToBaseAmount(requestParams, authHandler.round((orderAmount * taxPercent / 100), companyid), currency.getCurrencyID(), salesOrder.getQuotationDate(), salesOrder.getExternalCurrencyRate());
                    double ordertaxamountBase = authHandler.round((Double) bAmtTax.getEntityList().get(0), companyid);
                    double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round((orderAmount * taxPercent / 100), companyid));
                    amountinbase += totalTermAmount;
                    obj.put("amountinbase", amountinbase + ordertaxamountBase);
                    obj.put("taxpercent", taxPercent);
                    obj.put("taxamount", ordertaxamount);
                    amount += totalTermAmount;
                    orderAmount += totalTermAmount;
                    obj.put("orderamount", orderAmount);
                    obj.put("orderamountwithTax", orderAmount + ordertaxamount);
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("personname", customer.getName());
                    obj.put("memo", salesOrder.getMemo());
                    obj.put("posttext", salesOrder.getPostText());
                    
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    QuotationVersionCustomData quotationDetailCustomData = (QuotationVersionCustomData) salesOrder.getQuotationCustomData();
                    AccountingManager.setCustomColumnValues(quotationDetailCustomData, FieldMap, replaceFieldMap, variableMap);
                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        String valueForReport = "";
                        if (customFieldMap.containsKey(varEntry.getKey())  && coldata != null) {
                            try {
                                String[] valueData = coldata.split(",");
                                for (String value : valueData) {
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                    if (fieldComboData != null) {
                                        valueForReport += fieldComboData.getValue() + ",";
                                    }
                                }
                                if (valueForReport.length() > 1) {
                                    valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                                }
                                obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                            } catch (Exception ex) {
                                obj.put(varEntry.getKey(), coldata);
                            }
                        } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                            DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                dateFromDB = defaultDateFormat.parse(coldata);
                                coldata = df2.format(dateFromDB);

                            } catch (Exception e) {
                            }
                            obj.put(varEntry.getKey(), coldata);
                        } else {
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                obj.put(varEntry.getKey(), coldata);
                            }
                        }
                    }
                    boolean addFlag = true;
                    if (closeflag && status.equalsIgnoreCase("Closed")) {
                        addFlag = false;
                    }
                    if (addFlag) {
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getVersionQuotationsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getReplacementRequestRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {

            jobj = getReplacementRequestRowsJSON(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesOrderController.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getQuotationRows:" + ex.getMessage();
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

    public ModelAndView getReplacementRequestsDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {

            jobj = getReplacementRequestsDetailsJSON(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesOrderController.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getQuotationRows:" + ex.getMessage();
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
    
    

    public ModelAndView getQuotationRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            String closeflag = request.getParameter("closeflag");
            requestParams.put("closeflag", closeflag);
            boolean isForLinking = Boolean.FALSE.parseBoolean(request.getParameter("isForLinking"));
            requestParams.put("isForLinking", isForLinking);
            String soflag = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("sopolinkflag")) && request.getParameter("sopolinkflag") != null) {
                requestParams.put("sopolinkflag", request.getParameter("sopolinkflag"));
            }else{
                requestParams.put("sopolinkflag", "false");
            }
            requestParams.put("bills", request.getParameter("bills"));
            requestParams.put("dtype",request.getParameter("dtype"));
            boolean isOrder = false;
            String isorder = request.getParameter("isOrder");
            if (!StringUtil.isNullOrEmpty(isorder) && StringUtil.equal(isorder, "true")) {
                isOrder = true;
            }
            requestParams.put("isOrder",isOrder);
            requestParams.put("copyInvoice", request.getParameter("copyInvoice"));
            requestParams.put("dataFormatValue", authHandler.getDateOnlyFormat(request));
            requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(request));
            requestParams.put("isLeaseFixedAsset",Boolean.FALSE.parseBoolean(request.getParameter("isLeaseFixedAsset")));
            
            
            jobj = accSalesOrderServiceDAOobj.getQuotationRows(requestParams);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesOrderController.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getQuotationRows:" + ex.getMessage();
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
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getQuotationVersionRows(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesOrderController.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getQuotationRows:" + ex.getMessage();
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
    
    public JSONObject getReplacementRequestRowsJSON(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            String replacementRequestId = request.getParameter("bills");
            JSONArray jArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(replacementRequestId)) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(ProductReplacement.class.getName(), replacementRequestId);
                ProductReplacement pr = (ProductReplacement) result.getEntityList().get(0);

                if (pr != null) {

                    // get Product Replacement Rows and iterate

                    Set<ProductReplacementDetail> productReplacementDetails = pr.getProductReplacementDetails();

                    Iterator it = productReplacementDetails.iterator();

                    while (it.hasNext()) {
                        ProductReplacementDetail row = (ProductReplacementDetail) it.next();

                        JSONObject obj = new JSONObject();
                        obj.put("billid", pr.getId());
                        obj.put("billno", pr.getReplacementRequestNumber());
                        obj.put("srno", "1");
                        obj.put("rowid", row.getId());
                        obj.put("productid", row.getProduct().getID());
                        obj.put("productname", row.getProduct().getName());
                        obj.put("pid", row.getProduct().getProductid());
                        obj.put("desc", row.getProduct().getDescription());
                        
                        String uom = row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                        obj.put("uomname", uom);
                        obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                        obj.put("multiuom", row.getProduct().isMultiuom());
                        obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                        obj.put("type",row.getProduct().getProducttype()==null?"":row.getProduct().getProducttype().getName());

                        double availabeQuantity = getProductReplacementRequestQuantitiesForSO(row);
                        obj.put("quantity", availabeQuantity);
                        obj.put("copyquantity", availabeQuantity);
                        obj.put("dquantity", availabeQuantity);
                        obj.put("discountispercent", 1);
                        obj.put("prdiscount", 0.0);


                        obj.put("rate", 0.0);
                        obj.put("baseuomquantity", 1);
                        obj.put("baseuomrate", 1);
                        obj.put("isAsset", row.isIsAsset());//"isAsset" Flag to identify whether Replacement is made from product or Asset Group
                        jArr.put(obj);
                    }
                }
                jobj.put("data", jArr);
            }

        } catch (JSONException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jobj;
    }
    
    public JSONObject getReplacementRequestsDetailsJSON(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            String replacementRequestId = request.getParameter("replacementId");
            JSONArray jArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(replacementRequestId)) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(ProductReplacement.class.getName(), replacementRequestId);
                ProductReplacement pr = (ProductReplacement) result.getEntityList().get(0);

                if (pr != null) {

                    // get Product Replacement Rows and iterate

                    Set<ProductReplacementDetail> productReplacementDetails = pr.getProductReplacementDetails();

                    Iterator it = productReplacementDetails.iterator();

                    while (it.hasNext()) {
                        ProductReplacementDetail row = (ProductReplacementDetail) it.next();

                        JSONObject obj = new JSONObject();
                        obj.put("productName", row.getProduct().getName());
                        obj.put("replacementQuantity", row.getReplacementQuantity());
                        obj.put("replacedQuantity", row.getReplacedQuantity());
                        jArr.put(obj);
                    }
                }
                jobj.put("data", jArr);
            }

        } catch (JSONException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jobj;
    }

    public JSONObject getQuotationVersionRows(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj=new JSONObject();
        try {
            HashMap<String,Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            String companyid=sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            String closeflag = request.getParameter("closeflag");
            boolean soflag =false;
            String description="";
            if(!StringUtil.isNullOrEmpty(request.getParameter("sopolinkflag")) && request.getParameter("sopolinkflag")!=null){
               soflag =Boolean.FALSE.parseBoolean( request.getParameter("sopolinkflag")); 
            }
            String[] sos=(String[])request.getParameter("bills").split(",");
            String dType=request.getParameter("dtype");
             boolean isOrder = false;
            String isorder = request.getParameter("isOrder");
            if(!StringUtil.isNullOrEmpty(isorder) && StringUtil.equal(isorder, "true")){
                isOrder = true;
            }
            boolean isReport = false;
            boolean customIsReport = false;
            if(!StringUtil.isNullOrEmpty(dType) && StringUtil.equal(dType, "report")){
                isReport = true;
                customIsReport = true;
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("copyInvoice")) && Boolean.parseBoolean(request.getParameter("copyInvoice"))){                
                isReport = true;
            }
            int i=0;
            JSONArray jArr=new JSONArray();
            double addobj = 1;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request),Constants.Acc_Customer_Quotation_ModuleId,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("quotationversion.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);

            while(sos!=null&&i<sos.length){
                KwlReturnObject result = accountingHandlerDAOobj.getObject(QuotationVersion.class.getName(), sos[i]);
                QuotationVersion so = (QuotationVersion) result.getEntityList().get(0);
                KWLCurrency currency = null;
                if(so.getCurrency() != null){
                    currency = so.getCurrency();
                } else {
                    currency=so.getCustomer().getAccount().getCurrency()==null?kwlcurrency:so.getCustomer().getAccount().getCurrency();
                }
                filter_params.clear();
                filter_params.add(so.getID());
                KwlReturnObject podresult = accSalesOrderDAOobj.getQuotationVersionDetails(soRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                
                while(itr.hasNext()) {
                    QuotationVersionDetail row=(QuotationVersionDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", so.getID());
                    obj.put("billno", so.getquotationNumber());
                    obj.put("currencysymbol",currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("originalTransactionRowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("productname",row.getProduct().getName());
                    String uom=row.getUom()!=null?row.getUom().getNameEmptyforNA():row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    obj.put("unitname", uom );
                    obj.put("uomname", uom);
                    obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("multiuom", row.getProduct().isMultiuom());
                    if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                        description = row.getDescription();
                    } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                        description = row.getProduct().getDescription();
                    } else {
                        description = "";
                    }
                    obj.put("desc", description);
                    obj.put("type",row.getProduct().getProducttype()==null?"":row.getProduct().getProducttype().getName());
                    obj.put("pid",row.getProduct().getProductid());
                    obj.put("memo", row.getRemark());
                    obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
                    obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());
                     if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {  //This is sats specific code 	 
                        obj.put("invoicetype", so.getInvoicetype());

                        obj.put("dependentType", row.getDependentType() == null ? "" : row.getDependentType());
                      
                            obj.put("showquantity", StringUtil.DecodeText(row.getShowquantity() == null ? "" : row.getShowquantity()));
                        
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
                       
                            obj.put("showquantity",  StringUtil.DecodeText(row.getShowquantity() == null ? "" : row.getShowquantity()));
                                            }
                    if (row.getVendorquotationdetails() != null) {
                           KwlReturnObject vqdetailsresult = accSalesOrderDAOobj.getVendorQuotationDetails(row.getVendorquotationdetails(),sessionHandlerImpl.getCompanyid(request));
                           Object vq[]=(Object[])vqdetailsresult.getEntityList().get(0);
                           obj.put("linkto",vq[1] );
                           obj.put("linkid",vq[0]);
                           obj.put("rowid", row.getVendorquotationdetails());
                           obj.put("savedrowid", row.getID());
                    }
                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable=false;
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
                        amountWithoutTax = authHandler.round(row.getRate()*row.getQuantity(), companyid);
                    }
                    obj.put("rateIncludingGst", authHandler.roundUnitPrice(row.getRateincludegst(), companyid));
                    obj.put("israteIncludingGst", so.isGstIncluded());
                    obj.put("prtaxid",row.getTax()==null?"": row.getTax().getID());
                    
                    if(!isReport && row.getDiscount() > 0 && isOrder){//In Sales order creation, we need to display Unit Price including row discount
                        double discount = (row.getDiscountispercent() == 1)?(row.getRate() * (row.getDiscount()/100)) : row.getDiscount();
                        obj.put("rate", (row.getRate() - discount));
                        obj.put("discountispercent", 1);
                        obj.put("prdiscount", 0);
                    } else {
                         obj.put("rate", row.getRate());
                        obj.put("discountispercent", row.getDiscountispercent());
                        obj.put("prdiscount", row.getDiscount());
                    }
                    
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    QuotationVersionDetailCustomData quotationDetailCustomData = (QuotationVersionDetailCustomData) row.getQuotationDetailCustomData();
                    AccountingManager.setCustomColumnValues(quotationDetailCustomData, FieldMap, replaceFieldMap, variableMap);
                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        String valueForReport = "";
                        if (customFieldMap.containsKey(varEntry.getKey()) && customIsReport && coldata != null) {
                            try {
                                String[] valueData = coldata.split(",");
                                for (String value : valueData) {
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                    if (fieldComboData != null) {
                                        valueForReport += fieldComboData.getValue() + ",";
                                    }
                                }
                                if (valueForReport.length() > 1) {
                                    valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                                }
                                obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                            } catch (Exception ex) {
                                obj.put(varEntry.getKey(), coldata);
                            }
                        } else if (customDateFieldMap.containsKey(varEntry.getKey()) && customIsReport) {
                            DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                dateFromDB = defaultDateFormat.parse(coldata);
                                coldata = df2.format(dateFromDB);

                            } catch (Exception e) {
                            }
                            obj.put(varEntry.getKey(), coldata);
                        } else {
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                obj.put(varEntry.getKey(), coldata);
                            }
                        }
                    }

                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getQuotationDate(), 0);
                    obj.put("orderrate", row.getRate());//obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                    double baseuomrate = row.getBaseuomrate();
                    double quantity = 0;

                    quantity = row.getQuantity();
                    obj.put("quantity", quantity);
                    obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, baseuomrate, companyid));
                   if(row.getUom()!=null) {
                        obj.put("uomid", row.getUom().getID());
                        obj.put("baseuomquantity",  authHandler.calculateBaseUOMQuatity(quantity,row.getBaseuomrate(), companyid));
                        obj.put("baseuomrate", row.getBaseuomrate());
                    } else {
                        obj.put("uomid", row.getProduct().getUnitOfMeasure()!=null?row.getProduct().getUnitOfMeasure().getID():"");
                        obj.put("baseuomquantity",  authHandler.calculateBaseUOMQuatity(quantity,row.getBaseuomrate(), companyid));
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

    public ModelAndView exportQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
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
                int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has permission to view all customers documents,so at that time there is need to filter record according to user&salesperson. 
                     */
                    String userId = sessionHandlerImpl.getUserid(request);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
            }
            requestParams.put("closeflag", request.getParameter("closeflag"));
            String soflag = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("sopolinkflag")) && request.getParameter("sopolinkflag") != null) {
                soflag = request.getParameter("sopolinkflag");
            }
            requestParams.put("sopolinkflag", soflag);
            requestParams.put("linkFlagInSO",request.getParameter("linkFlagInSO"));  // Check wether quotation is link with SO 
            requestParams.put("linkFlagInInv", request.getParameter("linkFlagInInv"));  // Check wether quotation is link with  Invoice
            requestParams.put("isExport", true);
            KwlReturnObject result = null;
            String companyid = "";
            Map<String, Object> salesPersonParams = new HashMap<>();

            salesPersonParams.put("userid", sessionHandlerImpl.getUserid(request));
            salesPersonParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            salesPersonParams.put("grID", "15");
            KwlReturnObject masterItemByUserList = accountingHandlerDAOobj.getMasterItemByUserID(salesPersonParams);
            List<MasterItem> masterItems = masterItemByUserList.getEntityList();
            String salesPersons = "";
            StringBuilder salesPersonids = new StringBuilder();
            for (Object obj : masterItems) {
                if (obj != null) {
                    salesPersonids.append(obj.toString() + ",");
                }
            }
            if (salesPersonids.length() > 0) {
                salesPersons = salesPersonids.substring(0, (salesPersonids.length() - 1));
                requestParams.put("salesPersonid", salesPersons);
            }
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("salesPersonFilterFlag", true); //To export no of records as per userid  
                result = accSalesOrderDAOobj.getQuotations(requestParams);
                DataJArr = accSalesOrderServiceDAOobj.getQuotationsJson(requestParams, result.getEntityList(), DataJArr);
                if (request.getParameter("type") != null && request.getParameter("type").equals("detailedXls")) {
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
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public JSONArray getDetailExcelJsonQuotation(HttpServletRequest request, HttpServletResponse response, HashMap<String, Object> requestParams, JSONArray DataJArr) throws JSONException, SessionExpiredException, ServiceException, SessionExpiredException, SessionExpiredException {
        boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
        String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
        String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
        String companyid = companyids[0];
        JSONArray tempArray = new JSONArray();
        for (int i = 0; i < DataJArr.length(); i++) {
            JSONObject rowjobj = new JSONObject();
            rowjobj = DataJArr.getJSONObject(i);
            String billid = rowjobj.optString("billid", "");   //Invoice ID 
            requestParams.put("billId", billid);
            requestParams.put("bills", billid);
            request.setAttribute("companyid", companyid);
            request.setAttribute("gcurrencyid", gcurrencyid);
            request.setAttribute("billid", billid);
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", gcurrencyid);
            requestParams.put("closeflag", null);
            requestParams.put("dateFormatValue", authHandler.getDateOnlyFormat(request));
            requestParams.put(Constants.userdf, authHandler.getUserDateFormatter(request));
            if (StringUtil.isNullOrEmpty(request.getParameter("archieve"))) {
                requestParams.put("archieve", 0);
            } else {
                requestParams.put("archieve", Integer.parseInt(request.getParameter("archieve")));
            }
            requestParams.put("closeflag", null);
            requestParams.put("sopolinkflag", null);
            requestParams.put("linkFlagInSO", null);
            requestParams.put("linkFlagInInv", null);
            requestParams.put("dtype", request.getParameter("dtype"));
            boolean isOrder = false;
            String isorder = request.getParameter("isOrder");
            if (!StringUtil.isNullOrEmpty(isorder) && StringUtil.equal(isorder, "true")) {
                isOrder = true;
            }
            requestParams.put("isOrder", isOrder);
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            JSONArray DataRowsArr = null;
            DataRowsArr = accSalesOrderServiceDAOobj.getQuotationRows(requestParams).getJSONArray("data");
            tempArray.put(rowjobj);
            for (int j = 0; j < DataRowsArr.length(); j++) {
                JSONObject tempjobj = new JSONObject();
                tempjobj = DataRowsArr.getJSONObject(j);
                exportDaoObj.editJsonKeyForExcelFile(tempjobj, Constants.Acc_Customer_Quotation_ModuleId);
                tempArray.put(tempjobj);
            }
        }
        return tempArray;
    }
   public void exportSingleSO(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException, IOException, JSONException {
        
            String SOID = request.getParameter("bills");
            HashMap<String, Object>otherconfigrequestParams = new HashMap();
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            boolean isConsignment = Boolean.parseBoolean(request.getParameter("isConsignment"));
            boolean isLeaseFixedAsset = request.getParameter("isLeaseFixedAsset")!=null?Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")):false;
            int moduleid = Integer.parseInt(request.getParameter(Constants.moduleid));
            if (isConsignment) {
                moduleid = Constants.Acc_ConsignmentRequest_ModuleId;
            } else if(isLeaseFixedAsset){
                moduleid = Constants.Acc_Lease_Order_ModuleId;
            }
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), SOID);
            SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
            AccCustomData  accCustomData = null;
            if (salesOrder.getSoCustomData()!=null) {
                accCustomData = salesOrder.getSoCustomData();
            }
            String recordids = "";
            String templateSubType = "0";
            templateSubType = request.getParameter("templatesubtype") != null ? request.getParameter("templatesubtype") : "0";
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("recordids")))
                recordids = request.getParameter("recordids");
            ArrayList<String> SOIDList = CustomDesignHandler.getSelectedBillIDs(recordids);            
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid,1));
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
            JSONObject requestObj = StringUtil.convertRequestToJsonObject(request);
            
            HashMap<String, JSONArray>  itemDataSO = new HashMap<String, JSONArray>();
            boolean isJobOrderFlow = false;
            if(templateSubType.equals(Constants.SUBTYPE_JOB_ORDER) || templateSubType.equals(Constants.SUBTYPE_JOB_ORDER_LABEL)){ // Job Order Flow
                isJobOrderFlow = true;
                for(int count=0 ; count < SOIDList.size() ; count++ ){
                    HashMap<String, JSONArray>  itemData = new HashMap<String, JSONArray>();
                    itemData =  accSalesOrderServiceDAOobj.getSODetailsJobOrderFlowItemJSON(requestObj, SOIDList.get(count), paramMap);
                    if(!itemData.isEmpty()){
                        // get recordids
                        String tempRecordIds = itemData.get("recordids").toString();
                        tempRecordIds = tempRecordIds.substring(1, tempRecordIds.length()-1);
                        tempRecordIds = tempRecordIds.replaceAll("\"", "");
                        // append recordids to existing recordids
                        if(count == 0){// if first iteration no existing recordids
                            recordids = tempRecordIds;
                        } else{// If existing recordids
                            recordids = recordids + "," + tempRecordIds;
                        }
                        itemDataSO.putAll(itemData);

                        // Below Function called to update print flag for SO Report
                        accCommonTablesDAO.updatePrintFlag(moduleid, SOIDList.get(count), companyid);
                    }
                }
                itemDataSO.remove("recordids"); // Remove recordids field
                otherconfigrequestParams.put("recordids", recordids);// Put recordids
            } else{
                for(int count=0 ; count < SOIDList.size() ; count++ ){
                    JSONArray lineItemsArr =  accSalesOrderServiceDAOobj.getSODetailsItemJSON(requestObj, SOIDList.get(count), paramMap);
                    itemDataSO.put(SOIDList.get(count), lineItemsArr);

                    // Below Function called to update print flag for SO Report
                    accCommonTablesDAO.updatePrintFlag(moduleid, SOIDList.get(count), companyid);
                }
            }

            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            otherconfigrequestParams.put(Constants.isConsignment, isConsignment);
            otherconfigrequestParams.put(Constants.isJobOrderFlow, isJobOrderFlow);
            
            String invoicePostText=salesOrder.getPostText()==null?"":salesOrder.getPostText();
            if(!itemDataSO.isEmpty()){
                ExportRecordHandler.exportSingleGeneric(request, response,itemDataSO,accCustomData,customDesignDAOObj,accCommonTablesDAO,accAccountDAOobj, accountingHandlerDAOobj,velocityEngine,invoicePostText,otherconfigrequestParams,accInvoiceServiceDAOObj,accGoodsReceiptServiceDAOobj);
            }else{
                String errHTML = CustomDesignHandler.getErrorHtmlForDD(request);
                response.setContentType("text/html;charset=UTF-8");
                response.getOutputStream().write(errHTML.getBytes());
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
    }
   
    public void exportVHQSalesOrderJasper(HttpServletRequest request, HttpServletResponse response) {
        try {
            List jasperPrint = accExportReportsServiceDAOobj.exportVHQSalesOrderJasper(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
     public void exportSBISalesOrderJasper(HttpServletRequest request, HttpServletResponse response) {
        try {
            List jasperPrint = accExportOtherReportsServiceDAOobj.exportSBISalesOrderJasper(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
      public void exportSBICustomerQoutationJasper(HttpServletRequest request, HttpServletResponse response) {
        try {
            List jasperPrint = accExportOtherReportsServiceDAOobj.exportSBICustomerQoutationJasper(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    public ModelAndView getContractDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("contractid", request.getParameter("contractid"));
            
            KwlReturnObject result = accSalesOrderDAOobj.getContractDetails(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getContractDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
            
    public JSONArray getContractDetailsJsonMerged(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            double amount;
            double quantity=0;

            if (list != null && !list.isEmpty()) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Contract contract = (Contract) itr.next();

                    JSONObject jobj = new JSONObject();
                    jobj.put("cid", contract.getID());
                    jobj.put("customername", (contract.getCustomer() != null)? contract.getCustomer().getName() : "");
                    jobj.put("contractid", contract.getContractNumber());
                    jobj.put("currencysymbol", (contract.getCurrency() != null)? contract.getCurrency().getSymbol() : "");
                    
                    KwlReturnObject contractDateResult = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
                    List<Object[]> contractDateList = contractDateResult.getEntityList();
                    for (Object[] row : contractDateList) {
                        jobj.put("contractExpireyDate", row[1] != null ? df.format(row[1]) : null);
                    }
                    
                    int status = contract.getCstatus();
                    String statusName = "";
                    if (status == 1) {
                        statusName = messageSource.getMessage("acc.field.Active", null, RequestContextUtils.getLocale(request)); // "Active";
                    } else if (status == 2) {
                        statusName = messageSource.getMessage("acc.field.Terminate", null, RequestContextUtils.getLocale(request)); // "Terminate";
                    } else if (status == 3) {
                        statusName = messageSource.getMessage("acc.field.Expire", null, RequestContextUtils.getLocale(request)); // "Expire";
                    } else if (status == 4) {
                        statusName = messageSource.getMessage("acc.field.Renew", null, RequestContextUtils.getLocale(request)); // "Renew";
                    }
                    jobj.put("status", statusName);

                    String termType = contract.getTermType();
                    String termTypeName = "";
                    if (!StringUtil.isNullOrEmpty(termType)) {
                        if (termType.equals("1")) {
                            termTypeName = messageSource.getMessage("acc.field.Day", null, RequestContextUtils.getLocale(request)); // "Day";
                        } else if (termType.equals("2")) {
                            termTypeName = messageSource.getMessage("acc.field.Week", null, RequestContextUtils.getLocale(request)); // "Week";
                        } else if (termType.equals("3")) {
                            termTypeName = messageSource.getMessage("acc.field.Month", null, RequestContextUtils.getLocale(request)); // "Month";
                        } else if (termType.equals("4")) {
                            termTypeName = messageSource.getMessage("acc.field.Year", null, RequestContextUtils.getLocale(request)); // "Year";
                        }
                    }
                    jobj.put("contractTerm", contract.getTermValue() + " " + termTypeName);
                    
                    jobj.put("renewContract", "-"); // not added in pojo
                    jobj.put("terminateContract", "-"); // not added in pojo
                    jobj.put("tenureDetails", "-"); // not added in pojo
                    jobj.put("totalAmount", contract.getAmount());

                    jArr.put(jobj);
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getContractDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getContractOtherDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("contractid", request.getParameter("contractid"));
            
            KwlReturnObject result = accSalesOrderDAOobj.getContractOtherDetails(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractOtherDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getContractDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getContractOtherDetailsJsonMerged(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);

            if (list != null && !list.isEmpty()) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Contract contract = (Contract) itr.next();

                    JSONObject jobj = new JSONObject();
                    jobj.put("cid", contract.getSeqnumber());
                    
                    KwlReturnObject contractDateResult = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
                    List<Object[]> contractDateList = contractDateResult.getEntityList();
                    for(Object[] row : contractDateList) {
                        jobj.put("from", row[0] != null ? df.format(row[0]) : null);
                        jobj.put("to", row[1] != null ? df.format(row[1]) : null);
                    }
                    
                    int status = contract.getCstatus();
                    String statusName = "";
                    if(status == 1) {
                        statusName = messageSource.getMessage("acc.field.Active", null, RequestContextUtils.getLocale(request)); // "Active";
                    } else if(status == 2){
                        statusName = messageSource.getMessage("acc.field.Terminate", null, RequestContextUtils.getLocale(request)); // "Terminate";
                    } else if(status == 3){
                        statusName = messageSource.getMessage("acc.field.Expire", null, RequestContextUtils.getLocale(request)); // "Expire";
                    } else if(status == 4){
                        statusName = messageSource.getMessage("acc.field.Renew", null, RequestContextUtils.getLocale(request)); // "Renew";
                    }
                    jobj.put("status", statusName);
                    
                    String termType = contract.getTermType();
                    String termTypeName = "";
                    if(!StringUtil.isNullOrEmpty(termType)) {
                        if(termType.equals("1")) {
                            termTypeName = messageSource.getMessage("acc.field.Day", null, RequestContextUtils.getLocale(request)); // "Day";
                        } else if(termType.equals("2")){
                            termTypeName = messageSource.getMessage("acc.field.Week", null, RequestContextUtils.getLocale(request)); // "Week";
                        } else if(termType.equals("3")){
                            termTypeName = messageSource.getMessage("acc.field.Month", null, RequestContextUtils.getLocale(request)); // "Month";
                        } else if(termType.equals("4")){
                            termTypeName = messageSource.getMessage("acc.field.Year", null, RequestContextUtils.getLocale(request)); // "Year";
                        }
                    }
                    jobj.put("contractTerm", contract.getTermValue()+" "+termTypeName);
                    
                    jobj.put("lastRenewedDate", ""); // not added in pojo
                    jobj.put("originalEndDate", (contract.getOriginalEndDate() !=null)? df.format(contract.getOriginalEndDate()) : "");
                    jobj.put("signInDate", (contract.getSignDate() !=null)? df.format(contract.getSignDate()) : "");
                    jobj.put("moveInDate", (contract.getMoveDate() != null)? df.format(contract.getMoveDate()) : "");
                    jobj.put("moveOutDate", (contract.getMoveOutDate() != null)? df.format(contract.getMoveOutDate()) : "");

                    jArr.put(jobj);
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getContractOtherDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getContractNormalInvoiceDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("contractid", request.getParameter("contractid"));
            
            boolean isNormalContract = false;
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNormalContract"))) {
                isNormalContract = Boolean.parseBoolean(request.getParameter("isNormalContract"));
            }
            
            requestParams.put("isNormalContract", isNormalContract);
            
            KwlReturnObject result = accSalesOrderDAOobj.getContractNormalInvoiceDetails(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractNormalInvoiceDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getContractNormalInvoiceDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getContractNormalInvoiceDetailsJsonMerged(HttpServletRequest request, List<String> list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            double amount=0;
            double quantity = 0;
            
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Set<String> invoiceSet = new HashSet<String>();

            for (String invContrMapID : list) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(InvoiceContractMapping.class.getName(), invContrMapID);

                InvoiceContractMapping InvMap = (InvoiceContractMapping) result.getEntityList().get(0);
                Invoice inv = (Invoice) InvMap.getInvoice();
                amount=0;
                if (!invoiceSet.contains(inv.getID())) {
                    
                    invoiceSet.add(inv.getID());

                    JSONObject jobj = new JSONObject();
                    jobj.put("cid", InvMap.getContract().getID());
                    jobj.put("documentID", inv.getID());
                    jobj.put("documentNumber", inv.getInvoiceNumber());
                    jobj.put("description", inv.getMemo());
//                    jobj.put("date", df.format(inv.getJournalEntry().getEntryDate()));
                    jobj.put("date", df.format(inv.getCreationDate()));
                    Set<InvoiceDetail> invRows = inv.getRows();
                    JournalEntryDetail d = inv.getCustomerEntry();
                    amount +=d.getAmount();
//                    if (invRows != null && !invRows.isEmpty()) {
//                        for (InvoiceDetail temp : invRows) {
//                            quantity = temp.getInventory().getQuantity();
//                            amount += temp.getRate() * quantity;
//                        }
//                    }
                    jobj.put("amount", authHandler.round(amount, companyid));
                    jobj.put("currencysymbol", (inv.getCurrency() != null) ? inv.getCurrency().getSymbol() : "");
                    jArr.put(jobj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getContractNormalInvoiceDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getContractRenewDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("contractid", request.getParameter("contractid"));
            
            KwlReturnObject result = accSalesOrderDAOobj.getContractDates(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractRenewDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getContractRenewDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getContractRenewDetailsJsonMerged(HttpServletRequest request, List<String> list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            int amount;
            double quantity = 0;

            for (String contractDateID : list) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(ContractDates.class.getName(), contractDateID);

                ContractDates contractDate = (ContractDates) result.getEntityList().get(0);

                JSONObject jobj = new JSONObject();
                jobj.put("cid", contractDate.getContract().getID());
                jobj.put("startdate", df.format(contractDate.getStartdate()));
                jobj.put("enddate", df.format(contractDate.getEnddate()));

                jArr.put(jobj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getContractNormalInvoiceDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getContractReplacementInvoiceDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("contractid", request.getParameter("contractid"));
            
            KwlReturnObject result = accSalesOrderDAOobj.getContractReplacementInvoiceDetails(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractReplacementInvoiceDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getContractReplacementInvoiceDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getContractReplacementInvoiceDetailsJsonMerged(HttpServletRequest request, List<String> list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            int amount;
            double quantity = 0;
            
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Set<String> invoiceSet = new HashSet<String>();
            
            for (String invContrMapID : list) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(InvoiceContractMapping.class.getName(), invContrMapID);

                InvoiceContractMapping InvMap = (InvoiceContractMapping) result.getEntityList().get(0);
                Invoice inv = (Invoice) InvMap.getInvoice();
                
                if (!invoiceSet.contains(inv.getID())) {

                    invoiceSet.add(inv.getID());

                    JSONObject jobj = new JSONObject();
                    jobj.put("cid", InvMap.getContract().getID());
                    jobj.put("documentID", inv.getID());
                    jobj.put("documentNumber", inv.getInvoiceNumber());
                    jobj.put("description", inv.getMemo());
//                    jobj.put("date", df.format(inv.getJournalEntry().getEntryDate()));
                    jobj.put("date", df.format(inv.getCreationDate()));
                    Set<InvoiceDetail> invRows = inv.getRows();
                    amount = 0;
                    if (invRows != null && !invRows.isEmpty()) {
                        for (InvoiceDetail temp : invRows) {
                            quantity = temp.getInventory().getQuantity();
                            amount += authHandler.round(temp.getRate() * quantity, companyid);
                        }
                    }
                    jobj.put("amount", authHandler.round(amount, companyid));
                    jobj.put("currencysymbol", (inv.getCurrency() != null) ? inv.getCurrency().getSymbol() : "");

                    jArr.put(jobj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getContractReplacementInvoiceDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getContractMaintenanceInvoiceDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("contractid", request.getParameter("contractid"));
            
            KwlReturnObject result = accSalesOrderDAOobj.getContractMaintenanceInvoiceDetails(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractMaintenanceInvoiceDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getContractMaintenanceInvoiceDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getContractMaintenanceInvoiceDetailsJsonMerged(HttpServletRequest request, List<String> list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            int amount;
            double quantity = 0;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (String invID : list) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invID);
                Invoice inv = (Invoice) result.getEntityList().get(0);

                JSONObject jobj = new JSONObject();
                jobj.put("documentID", inv.getID());
                jobj.put("documentNumber", inv.getInvoiceNumber());
                jobj.put("description", inv.getMemo());
//                jobj.put("date", df.format(inv.getJournalEntry().getEntryDate()));
                jobj.put("date", df.format(inv.getCreationDate()));
                Set<InvoiceDetail> invRows = inv.getRows();
                amount = 0;
                if (invRows != null && !invRows.isEmpty()) {
                    for (InvoiceDetail temp : invRows) {
                        quantity = temp.getInventory().getQuantity();
                        amount += authHandler.round(temp.getRate() * quantity, companyid);
                    }
                }
                jobj.put("amount", authHandler.round(amount, companyid));
                jobj.put("currencysymbol", (inv.getCurrency() != null)? inv.getCurrency().getSymbol() : "");

                jArr.put(jobj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getContractMaintenanceInvoiceDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getContractSalesReturnDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("contractid", request.getParameter("contractid"));
            
            KwlReturnObject result = accSalesOrderDAOobj.getContractSalesReturnDetails(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractSalesReturnDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getContractSalesReturnDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getContractSalesReturnDetailsJsonMerged(HttpServletRequest request, List<SalesReturn> list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            int amount;
            double quantity = 0;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (SalesReturn salesReturn : list) {

                JSONObject jobj = new JSONObject();
                jobj.put("documentID", salesReturn.getID());
                jobj.put("documentNumber", salesReturn.getSalesReturnNumber());
                jobj.put("description", salesReturn.getMemo());
                jobj.put("date", df.format(salesReturn.getOrderDate()));
                Set<SalesReturnDetail> srRows = salesReturn.getRows();
                amount = 0;
                if (srRows != null && !srRows.isEmpty()) {
                    for (SalesReturnDetail temp : srRows) {
                        quantity = temp.getInventory().getQuantity();
                        amount += temp.getRate() * quantity;
                    }
                }
                jobj.put("amount", authHandler.round(amount, companyid));
                jobj.put("currencysymbol", (salesReturn.getCurrency() != null)? salesReturn.getCurrency().getSymbol() : "");

                jArr.put(jobj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getContractSalesReturnDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getContractNormalDOItemDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            
            boolean isNormalContract = false;
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNormalContract"))) {
                isNormalContract = Boolean.parseBoolean(request.getParameter("isNormalContract"));
            }
            
            requestParams.put("isNormalContract", isNormalContract);
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("contractid", request.getParameter("contractid"));
            
            KwlReturnObject result = accSalesOrderDAOobj.getContractNormalDOItemDetails(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractNormalDOItemDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getContractNormalDOItemDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getContractNormalDOItemDetailsJsonMerged(HttpServletRequest request, List<String> list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            
            for (String dodid : list) {
                
                KwlReturnObject result = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), dodid);
                DeliveryOrderDetail doDetail = (DeliveryOrderDetail) result.getEntityList().get(0);
                
                JSONObject jobj = new JSONObject();
                jobj.put("pid", (doDetail.getProduct() != null)? doDetail.getProduct().getID() : "");
                jobj.put("type", (doDetail.getProduct() != null)? doDetail.getProduct().getProducttype() : "");
                jobj.put("itemName", (doDetail.getProduct() != null)? doDetail.getProduct().getName() : "");
                jobj.put("doid", (doDetail.getDeliveryOrder() != null)? doDetail.getDeliveryOrder().getID() : "");
                jobj.put("itemCode", (doDetail.getProduct() != null) ? doDetail.getProduct().getProductid() : "");
                jobj.put("itemDescription", (doDetail.getProduct() !=null)? doDetail.getProduct().getDescription() : "");
                jobj.put("quantity", doDetail.getActualQuantity());
                String uom = doDetail.getUom() != null ? doDetail.getUom().getNameEmptyforNA() : doDetail.getProduct().getUnitOfMeasure() == null ? "" : doDetail.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                jobj.put("uomname", uom);
                
                jArr.put(jobj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getContractNormalDOItemDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getContractNormalDOItemDetailsRow(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getContractNormalDOItemDetailsRow(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getContractNormalDOItemDetailsRow:" + ex.getMessage();
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getContractNormalDOItemDetailsRow(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
            
            String pid = request.getParameter("pid");
            String doid = request.getParameter("doid");
            String contractid = request.getParameter("contractid");
            JSONArray jArr = new JSONArray();

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("contractid", contractid);
            requestParams.put("pid", pid);
            requestParams.put("doid", doid);
           
            KwlReturnObject doresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doid);

            if (doresult != null && doresult.getEntityList() != null) {
                DeliveryOrder deliveryOrder = (DeliveryOrder) doresult.getEntityList().get(0);
                Set<DeliveryOrderDetail> deliveryOrderDetails = deliveryOrder.getRows();
                if (deliveryOrderDetails != null && !deliveryOrderDetails.isEmpty()) {
                    for (DeliveryOrderDetail temp : deliveryOrderDetails) {
                        String documentid = temp.getID();


                        KwlReturnObject result = accountingHandlerDAOobj.getObject(Product.class.getName(), pid);
                        KwlReturnObject kmsg = null;

                        Product product = (Product) result.getEntityList().get(0);
                        boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag"))) ? false : Boolean.parseBoolean(request.getParameter("linkingFlag"));
                        boolean isEdit=(StringUtil.isNullOrEmpty(request.getParameter("isEdit")))?false:Boolean.parseBoolean(request.getParameter("isEdit"));
                        String moduleID = request.getParameter("moduleid");
                        if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && product.isIsSerialForProduct()) {
                            kmsg = accCommonTablesDAO.getOnlySerialDetails(documentid, linkingFlag, moduleID,false,isEdit);
                        } else {
                            kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid, !product.isIsSerialForProduct(), linkingFlag, moduleID,false,isEdit,"");
                        }
                        List batchserialdetails = kmsg.getEntityList();
                        Iterator iter = batchserialdetails.iterator();
                        while (iter.hasNext()) {
                            Object[] objArr = (Object[]) iter.next();
                            JSONObject obj = new JSONObject();
                            obj.put("id", objArr[0] != null ? (String) objArr[0] : "");
                            obj.put("batch", objArr[1] != null ? (String) objArr[1] : "");
                            obj.put("batchname", objArr[1] != null ? (String) objArr[1] : "");
                            obj.put("location", objArr[2] != null ? (String) objArr[2] : "");
                            obj.put("warehouse", objArr[3] != null ? (String) objArr[3] : "");
                            obj.put("srid", objArr[7] != null ? (String) objArr[7] : "");
                            obj.put("srname", objArr[8] != null ? (String) objArr[8] : "");
                            if (objArr[1] != null) {
                                KwlReturnObject batchResult = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), (String) objArr[0]);
                                NewProductBatch newProductBatch = (NewProductBatch) batchResult.getEntityList().get(0);
                                if (newProductBatch != null) {
                                    obj.put("warrentyExpireyDate", (newProductBatch.getExpdate() != null) ? df.format(newProductBatch.getExpdate()) : "");
                                }else{
                                     obj.put("warrentyExpireyDate", "");
                                } 
                            }

                            String serialid = objArr[7] != null ? (String) objArr[7] : "";

                            if (!StringUtil.isNullOrEmpty(serialid)) {
                                KwlReturnObject batchResult = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), serialid);
                                NewBatchSerial newBatchSerial = (NewBatchSerial) batchResult.getEntityList().get(0);
                                obj.put("vendorWarrentyDate", (newBatchSerial.getExptodate() != null) ? df.format(newBatchSerial.getExptodate()) : "");

                            }
                            jArr.put(obj);
                        }




                    }
                }

            }
           /* KwlReturnObject codresult = accSalesOrderDAOobj.getContractNormalDOItemDetailsRow(requestParams);
            Iterator itr = codresult.getEntityList().iterator();

            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JSONObject obj = new JSONObject();
                String serialnoid = (row[0]!=null)? (String) row[0] : "";
                obj.put("srid", serialnoid);
                obj.put("srname", (row[1]!=null)? (String) row[1] : "");
                if (row[2] != null) {
                    String batchname = (String) row[2];
                    if (!StringUtil.isNullOrEmpty(batchname)) {
                        obj.put("batchname", batchname);
                    } else {
                        obj.put("batchname", "N/A");
                    }
                }
                obj.put("warrentyExpireyDate", (row[3]!=null)? df.format((Date) row[3]) : "");
                
                
                if (!StringUtil.isNullOrEmpty(serialnoid)) {
                    Date vendorExpDate = accCommonTablesDAO.getVendorExpDateForSerial(serialnoid,false);
                    if (vendorExpDate != null) {
                        obj.put("vendorWarrentyDate", sdf.format(vendorExpDate));
                    } else {
                        obj.put("vendorExpDate", "N/A");
                    }
                }
                
                jArr.put(obj);
            }*/
            jobj.put("data", jArr);
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    
    public ModelAndView getContractReplacementDOItemDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("contractid", request.getParameter("contractid"));
            
            KwlReturnObject result = accSalesOrderDAOobj.getContractReplacementDOItemDetails(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractReplacementDOItemDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getContractReplacementDOItemDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getContractReplacementDOItemDetailsJsonMerged(HttpServletRequest request, List<Object[]> list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            
            for (Object[] objRow : list) {
                
                String dodid = (String) objRow[1];
                KwlReturnObject result = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), dodid);
                DeliveryOrderDetail doDetail = (DeliveryOrderDetail) result.getEntityList().get(0);
                
                JSONObject jobj = new JSONObject();
                jobj.put("productReplacementID", (objRow[0] != null)? objRow[0] : "");
                jobj.put("pid", (doDetail.getProduct() != null)? doDetail.getProduct().getID() : "");
                jobj.put("type", (doDetail.getProduct() != null)? doDetail.getProduct().getProducttype() : "");
                jobj.put("itemName", (doDetail.getProduct() != null)? doDetail.getProduct().getName() : "");
                jobj.put("dateOfReplacement", (doDetail.getDeliveryOrder() != null)? df.format(doDetail.getDeliveryOrder().getOrderDate()) : "");
                jobj.put("itemCode", (doDetail.getProduct() != null) ? doDetail.getProduct().getProductid() : "");
                jobj.put("itemDescription", (doDetail.getProduct() !=null)? doDetail.getProduct().getDescription() : "");
                jobj.put("quantity", doDetail.getActualQuantity());
                String uom = doDetail.getUom() != null ? doDetail.getUom().getNameEmptyforNA() : doDetail.getProduct().getUnitOfMeasure() == null ? "" : doDetail.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                jobj.put("uomname", uom);
                
                jArr.put(jobj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getContractReplacementDOItemDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getContractReplacementDOItemDetailsRow(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getContractReplacementDOItemDetailsRow(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getContractReplacementDOItemDetailsRow:" + ex.getMessage();
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getContractReplacementDOItemDetailsRow(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            
            String pid = request.getParameter("pid");
            String productReplacementID = request.getParameter("productReplacementID");
            String contractid = request.getParameter("contractid");
            JSONArray jArr = new JSONArray();

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("contractid", contractid);
            requestParams.put("pid", pid);
            requestParams.put("productReplacementID", productReplacementID);

            KwlReturnObject codresult = accSalesOrderDAOobj.getContractReplacementDOItemDetailsRow(requestParams);
            Iterator itr = codresult.getEntityList().iterator();

            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JSONObject obj = new JSONObject();
                String serialnoid = (row[0]!=null)? (String) row[0] : "";
                obj.put("srid", serialnoid);
                obj.put("srname", (row[1]!=null)? (String) row[1] : "");
                obj.put("batchname", (row[2]!=null)? (String) row[2] : "");
                obj.put("warrentyExpireyDate", (row[3]!=null)? df.format((Date) row[3]) : "");
                
                
                if (!StringUtil.isNullOrEmpty(serialnoid)) {
                    Date vendorExpDate = accCommonTablesDAO.getVendorExpDateForSerial(serialnoid,false);
                    if (vendorExpDate != null) {
                        obj.put("vendorWarrentyDate", vendorExpDate);
                    }
                }
                
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    
    public ModelAndView getCustomerContractsAgreementDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("customerid", request.getParameter("customerid"));
            
            KwlReturnObject result = accSalesOrderDAOobj.getCustomerContractsAgreementDetails(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractAgreementDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getCustomerContractsAgreementDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getContractAgreementDetailsJsonMerged(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);

            if (list != null && !list.isEmpty()) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Contract contract = (Contract) itr.next();

                    JSONObject jobj = new JSONObject();
                    jobj.put("contractid", contract.getContractNumber());
                    jobj.put("contactperson", (contract.getContactPerson() != null)? contract.getContactPerson() : "");
                    jobj.put("agreementtype", ""); // not aded in pojo
                    
                    KwlReturnObject contractDateResult = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
                    List<Object[]> contractDateList = contractDateResult.getEntityList();
                    for (Object[] row : contractDateList) {
                        jobj.put("fromdate", row[0] != null ? df.format(row[0]) : null);
                        jobj.put("todate", row[1] != null ? df.format(row[1]) : null);
                    }
                    
                    int status = contract.getCstatus();
                    String statusName = "";
                    if(status == 1) {
                        statusName = messageSource.getMessage("acc.field.Active", null, RequestContextUtils.getLocale(request)); // "Active";
                    } else if(status == 2){
                        statusName = messageSource.getMessage("acc.field.Terminate", null, RequestContextUtils.getLocale(request)); // "Terminate";
                    } else if(status == 3){
                        statusName = messageSource.getMessage("acc.field.Expire", null, RequestContextUtils.getLocale(request)); // "Expire";
                    } else if(status == 4){
                        statusName = messageSource.getMessage("acc.field.Renew", null, RequestContextUtils.getLocale(request)); // "Renew";
                    }
                    jobj.put("status", statusName);

                    String termType = contract.getTermType();
                    String termTypeName = "";
                    if (!StringUtil.isNullOrEmpty(termType)) {
                        if (termType.equals("1")) {
                            termTypeName = messageSource.getMessage("acc.field.Day", null, RequestContextUtils.getLocale(request)); // "Day";
                        } else if (termType.equals("2")) {
                            termTypeName = messageSource.getMessage("acc.field.Week", null, RequestContextUtils.getLocale(request)); // "Week";
                        } else if (termType.equals("3")) {
                            termTypeName = messageSource.getMessage("acc.field.Month", null, RequestContextUtils.getLocale(request)); // "Month";
                        } else if (termType.equals("4")) {
                            termTypeName = messageSource.getMessage("acc.field.Year", null, RequestContextUtils.getLocale(request)); // "Year";
                        }
                    }
                    jobj.put("leaseterm", contract.getTermValue() + " " + termTypeName);
                    
                    jobj.put("lastrenewdate", "-"); // not added in pojo
                    jobj.put("orgenddate", (contract.getOriginalEndDate() != null)? df.format(contract.getOriginalEndDate()) : "");
                    jobj.put("signindate", (contract.getSignDate() != null)? df.format(contract.getSignDate()) : "");
                    jobj.put("moveindate", (contract.getMoveDate() != null)? df.format(contract.getMoveDate()) : "");
                    jobj.put("moveoutdate", (contract.getMoveOutDate() != null)? df.format(contract.getMoveOutDate()) : "");

                    jArr.put(jobj);
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getCustomerContractDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getCustomerContractCostAgreementDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("customerid", request.getParameter("customerid"));
            
            KwlReturnObject result = accSalesOrderDAOobj.getCustomerContractCostAgreementDetails(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getCustomerContractCostAgreementDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getCustomerContractCostAgreementDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getCustomerContractCostAgreementDetailsJsonMerged(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (list != null && !list.isEmpty()) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Contract contract = (Contract) itr.next();

                    JSONObject jobj = new JSONObject();
                    jobj.put("contractid", contract.getContractNumber());
                    jobj.put("leaseamount", contract.getAmount());
                    jobj.put("currencysymbol", (contract.getCurrency() != null)? contract.getCurrency().getSymbol() : "");
                    jobj.put("securitydepos", ""); // not aded in pojo
                    
                    // For getting Outstanding amount
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    requestParams.put("contractid", contract.getID());
                    
                    KwlReturnObject invoiceResult = accSalesOrderDAOobj.getContractInvoiceDetails(requestParams);
                    List invoiceList = invoiceResult.getEntityList();
                    Iterator invoiceListItr = invoiceList.iterator();
                    double amount = 0;
                    double quantity = 0;
                    
                    while (invoiceListItr.hasNext()) {
                        InvoiceContractMapping InvMap = (InvoiceContractMapping) invoiceListItr.next();
                        Invoice inv = (Invoice) InvMap.getInvoice();
                        
                        Set<InvoiceDetail> invRows = inv.getRows();
                        if (invRows != null && !invRows.isEmpty()) {
                            for (InvoiceDetail temp : invRows) {
                                quantity = temp.getInventory().getQuantity();
                                amount += authHandler.round(temp.getRate() * quantity, companyid);
                            }
                        }
                    }
                    
                    double contractAmount = contract.getAmount();
                    double outstandingAmount = contractAmount - amount;
                    jobj.put("outstandings", outstandingAmount);
                    
                    jobj.put("monthlyrent", ""); // not aded in pojo
                    
                    jArr.put(jobj);
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getCustomerContractCostAgreementDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getCustomerContractsServiceAgreementDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("customerid", request.getParameter("customerid"));
            
            KwlReturnObject result = accSalesOrderDAOobj.getContractsOfCompany(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getCustomerContractsServiceAgreementDetailsJsonMerged(request, list, DataJArr);
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accSalesOrderControllerCMN.getCustomerContractsServiceAgreementDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getCustomerContractsServiceAgreementDetailsJsonMerged(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            
            if (list != null && !list.isEmpty()) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Contract contract = (Contract) itr.next();

                    JSONObject jobj = new JSONObject();
                    jobj.put("contractid", contract.getContractNumber());
                    jobj.put("agreedservices", contract.getAgreedServices());
                    
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    requestParams.put("contractid", contract.getID());
                    
                    KwlReturnObject resultOfNextServiceDate = accSalesOrderDAOobj.getNextServiceDateOfContract(requestParams);
                    if(!resultOfNextServiceDate.getEntityList().isEmpty()) {
                        Date nextServiceDate = (Date) resultOfNextServiceDate.getEntityList().get(0);
                        jobj.put("nextservicedate", df.format(nextServiceDate));
                    }
                    
                    KwlReturnObject resultOfPreviousServiceDate = accSalesOrderDAOobj.getPreviousServiceDateOfContract(requestParams);
                    if (!resultOfPreviousServiceDate.getEntityList().isEmpty()) {
                        Date previousServiceDate = (Date) resultOfPreviousServiceDate.getEntityList().get(0);
                        jobj.put("lastservicedate", df.format(previousServiceDate));
                    }
                    
                    jobj.put("oncallservices", ""); // not aded in pojo
                    jobj.put("ongoingservices", ""); // not aded in pojo

                    jArr.put(jobj);
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCMN.getCustomerContractsServiceAgreementDetailsJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getContractActivityDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        //Session session=null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String action = "210";
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/activity";            
            String customerid = request.getParameter("customerid");
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
            Customer cust = (Customer) cpresult.getEntityList().get(0);
            
            JSONObject userData = new JSONObject();
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("action", action);
            userData.put("accountid", (cust.getCrmaccountid() != null)? cust.getCrmaccountid() : "");
            userData.put("contractid", request.getParameter("contractid"));
            userData.put("timeZoneDiff", sessionHandlerImpl.getTimeZoneDifference(request));
            
            //session = HibernateUtil.getCurrentSession();
            
            jobj = apiCallHandlerService.restGetMethod(crmURL, userData.toString());
//            jobj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
//            HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getMaintainanceFormListDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        //Session session=null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String action = "211";
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/maintainance";                       
            JSONObject userData = new JSONObject();
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("action", action);
            userData.put("contractid", request.getParameter("contractid"));
            
            //session = HibernateUtil.getCurrentSession();
            
            jobj = apiCallHandlerService.restGetMethod(crmURL, userData.toString());
//            jobj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
            //HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void exportHengguanCustomerQuotationReport(HttpServletRequest request, HttpServletResponse response) {

        try {
            List jasperPrint = accExportReportsServiceDAOobj.exportHengguanCustomerQuotation(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            response.setHeader("Content-Disposition", "attachment;filename=" + "Quotation.pdf");            
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
     public void exportSalesContractreport(HttpServletRequest request, HttpServletResponse response) {

        try {
            List jasperPrint = accExportOtherReportsServiceDAOobj.exportSalesContractreport(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
//                response.setHeader("Content-Disposition", "attachment;filename=" + "SalesContract.pdf");
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    public ModelAndView getSalesByCustomerForSalesOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            String userid=sessionHandlerImpl.getUserid(request);
            requestParams.put("userid", userid);
            int salesOrderTypeId = -1;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(request.getParameter("salesordertypeid"))) {
                salesOrderTypeId = Integer.parseInt(request.getParameter("salesordertypeid"));
            }
            if (salesOrderTypeId == Constants.CONSIGNMENT_SALES_ORDER_TYPE) {
                salesOrderTypeId = Integer.parseInt(request.getParameter("salesordertypeid"));
            } else {
                requestParams.put("start", start);
                requestParams.put("limit", limit);
            }
            requestParams.put("salesOrderTypeId", salesOrderTypeId);
            KwlReturnObject result = accSalesOrderDAOobj.getSalesByCustomer(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();
            JSONArray DataJArr = getSalesByCustomerJson(request, list).getJSONArray("data");
            if (salesOrderTypeId == Constants.CONSIGNMENT_SALES_ORDER_TYPE) {
                JSONArray pagedJson = DataJArr;
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
                jobj.put("data", pagedJson);
                jobj.put("count", DataJArr.length());
            } else {
                jobj.put("data", DataJArr);
                jobj.put("count", count);
            }
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView exportDailyBookingsReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            JSONArray jArr = new JSONArray();
            
            /*
             * Get Daily Bookings Report Related Parameters
             */
            Map<String, Object> requestParams = getDailySalesReportCommonParameters(request);
            requestParams.put("isForExport", true);

            /*
             * Get Daily Bookings Report Details
             */

            jobj = accSalesOrderServiceDAOobj.getDailySalesReportByCustomer(request, requestParams);
            if (jobj.has("data") && jobj.get("data") != null) {
                jArr = jobj.getJSONArray("data");
            }
            
            jobj.put("data", jArr);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String stDate = request.getParameter("stdate");
                String edDate = request.getParameter("enddate");
                if (!StringUtil.isNullOrEmpty(stDate) && !StringUtil.isNullOrEmpty(edDate)) {
                    DateFormat df = authHandler.getDateOnlyFormat();
                    Date startDate = df.parse(request.getParameter("stdate"));
                    Date endDate = df.parse(request.getParameter("enddate"));
                    /*
                     * Calculation of Date Ranges for the Month.
                     */
                    LocalDate localStartDate = new LocalDate(startDate);
                    LocalDate localEndDate = new LocalDate(endDate);

                    startDate = localStartDate.toDateTimeAtCurrentTime().dayOfMonth().withMinimumValue().toDate();
                    endDate = localEndDate.toDateTimeAtCurrentTime().dayOfMonth().withMaximumValue().toDate();
                    
                    startDate.setHours(00);
                    startDate.setMinutes(00);
                    startDate.setSeconds(00);

                    endDate.setHours(00);
                    endDate.setMinutes(00);
                    endDate.setSeconds(00);
                    
                    stDate = authHandler.getDateOnlyFormat(request).format(startDate);
                    edDate = authHandler.getDateOnlyFormat(request).format(endDate);
                    jobj.put("isFromToDateRequired", true);
                    jobj.put("stdate", stDate);
                    jobj.put("enddate", edDate);
                } else {
                    String GenerateDate = authHandler.getDateOnlyFormat(request).format(new Date());
                    jobj.put("GenerateDate", GenerateDate);
                }
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView exportMonthlyBookingsReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            JSONArray jArr = new JSONArray();
            
            /*
             * Get Monthly Bookings Report Related Parameters
             */
            Map<String, Object> requestParams = getDailySalesReportCommonParameters(request);
            requestParams.put("isForExport", true);

            /*
             * Get Monthly Bookings Report Details
             */

            jobj = accSalesOrderServiceDAOobj.getMonthlySalesOrdesByCustomer(request, requestParams);
            if (jobj.has("data") && jobj.get("data") != null) {
                jArr = jobj.getJSONArray("data");
            }
            
            jobj.put("data", jArr);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String stDate = request.getParameter("stdate");
                String edDate = request.getParameter("enddate");
                if (!StringUtil.isNullOrEmpty(stDate) && !StringUtil.isNullOrEmpty(edDate)) {
                    DateFormat df = authHandler.getDateOnlyFormat();
                    Date startDate = df.parse(request.getParameter("stdate"));
                    Date endDate = df.parse(request.getParameter("enddate"));
                    /*
                     * Calculation of Date Ranges for the Month.
                     */
                    LocalDate localStartDate = new LocalDate(startDate);
                    LocalDate localEndDate = new LocalDate(endDate);

                    startDate = localStartDate.toDateTimeAtCurrentTime().dayOfMonth().withMinimumValue().toDate();
                    endDate = localEndDate.toDateTimeAtCurrentTime().dayOfMonth().withMaximumValue().toDate();
                    
                    startDate.setHours(00);
                    startDate.setMinutes(00);
                    startDate.setSeconds(00);

                    endDate.setHours(00);
                    endDate.setMinutes(00);
                    endDate.setSeconds(00);
                    
                    stDate = authHandler.getDateOnlyFormat(request).format(startDate);
                    edDate = authHandler.getDateOnlyFormat(request).format(endDate);
                    jobj.put("isFromToDateRequired", true);
                    jobj.put("stdate", stDate);
                    jobj.put("enddate", edDate);
                } else {
                    String GenerateDate = authHandler.getDateOnlyFormat(request).format(new Date());
                    jobj.put("GenerateDate", GenerateDate);
                }
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView exportYearlyBookingsReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            JSONArray jArr = new JSONArray();
            
            /*
             * Get Monthly Bookings Report Related Parameters
             */
            Map<String, Object> requestParams = getDailySalesReportCommonParameters(request);
            requestParams.put("isForExport", true);

            /*
             * Get Monthly Bookings Report Details
             */

            jobj = accSalesOrderServiceDAOobj.getYearlySalesOrdersByCustomer(request, requestParams);
            if (jobj.has("data") && jobj.get("data") != null) {
                jArr = jobj.getJSONArray("data");
            }
            
            jobj.put("data", jArr);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String stDate = request.getParameter("stdate");
                String edDate = request.getParameter("enddate");
                if (!StringUtil.isNullOrEmpty(stDate) && !StringUtil.isNullOrEmpty(edDate)) {
                    DateFormat df = authHandler.getDateOnlyFormat();
                    Date startDate = df.parse(request.getParameter("stdate"));
                    Date endDate = df.parse(request.getParameter("enddate"));
                    /*
                     * Calculation of Date Ranges for the Month.
                     */
                    LocalDate localStartDate = new LocalDate(startDate);
                    LocalDate localEndDate = new LocalDate(endDate);

                    startDate = localStartDate.toDateTimeAtCurrentTime().dayOfMonth().withMinimumValue().toDate();
                    endDate = localEndDate.toDateTimeAtCurrentTime().dayOfMonth().withMaximumValue().toDate();
                    
                    startDate.setHours(00);
                    startDate.setMinutes(00);
                    startDate.setSeconds(00);

                    endDate.setHours(00);
                    endDate.setMinutes(00);
                    endDate.setSeconds(00);
                    
                    stDate = authHandler.getDateOnlyFormat(request).format(startDate);
                    edDate = authHandler.getDateOnlyFormat(request).format(endDate);
                    jobj.put("isFromToDateRequired", true);
                    jobj.put("stdate", stDate);
                    jobj.put("enddate", edDate);
                } else {
                    String GenerateDate = authHandler.getDateOnlyFormat(request).format(new Date());
                    jobj.put("GenerateDate", GenerateDate);
                }
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView getDailySalesReportByCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {

            /*
             * Get Daily Sales Report Related Parameters
             */
            Map<String, Object> requestParams = getDailySalesReportCommonParameters(request);

            /*
             * Get Daily Sales Report Details
             */

            jobj = accSalesOrderServiceDAOobj.getDailySalesReportByCustomer(request, requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }
    
    public ModelAndView getMonthlySalesOrdesByCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {

            /*
             * Get Monthly Sales Bookings Related Parameters
             */
            Map<String, Object> requestParams = getDailySalesReportCommonParameters(request);

            /*
             * Get Monthly Sales Bookings Details
             */

            jobj = accSalesOrderServiceDAOobj.getMonthlySalesOrdesByCustomer(request, requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }

    public ModelAndView getYearlySalesOrdersByCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {

            /*
             * Get Year Sales Bookings Related Parameters
             */
            Map<String, Object> requestParams = getDailySalesReportCommonParameters(request);

            /*
             * Get Year Sales Bookings Details
             */

            jobj = accSalesOrderServiceDAOobj.getYearlySalesOrdersByCustomer(request, requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }

    public Map<String, Object> getDailySalesReportCommonParameters(HttpServletRequest request) {
        Map<String, Object> requestParams = new HashMap<>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            requestParams.put("billid", request.getParameter("billid"));

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            requestParams.put("start", start);
            requestParams.put("limit", limit);

            requestParams.put("companyid", companyid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));


        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }
    
    public ModelAndView exportSalesByCustomerForSalesOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            int salesOrderTypeId = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("salesordertypeid"))) {
                salesOrderTypeId = Integer.parseInt(request.getParameter("salesordertypeid"));
            }
            requestParams.put("salesOrderTypeId", salesOrderTypeId);
            KwlReturnObject result = accSalesOrderDAOobj.getSalesByCustomer(requestParams);
            jobj = getSalesByCustomerJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public JSONObject getSalesByCustomerJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            Iterator itr = list.iterator();
            int salesOrderTypeId = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("salesordertypeid"))) {
                /**
                 * leaseOrMaintenanceSO == 0 means it is a normal SO
                 * leaseOrMaintenanceSO == 1 means this is a Lease SO,
                 * leaseOrMaintenanceSO == 2 means this is an maintenance SO,
                 * leaseOrMaintenanceSO == 3 means it is a consignment SO.
                 */
                salesOrderTypeId = Integer.parseInt(request.getParameter("salesordertypeid"));
            }
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String invid = oj[0].toString();

                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), invid);
                SalesOrder invoice = (SalesOrder) objItr.getEntityList().get(0);
                String currencyid = (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
                JSONObject obj = new JSONObject();
               
                obj.put("billid", invoice.getID());
                obj.put("customername", invoice.getCustomer() == null ? "" : invoice.getCustomer().getName());
                obj.put("customerid", invoice.getCustomer() == null ? "" : invoice.getCustomer().getID());
                obj.put("personname", invoice.getCustomer() == null ? "" : invoice.getCustomer().getName());
                obj.put("personid", invoice.getCustomer() == null ? "" : invoice.getCustomer().getID());
                obj.put(Constants.SEQUENCEFORMATID, invoice.getSeqformat() == null ? "" : invoice.getSeqformat().getID());
                obj.put("duedate", invoice.getDueDate() == null ? "" : df.format(invoice.getDueDate()));
                obj.put("memo", invoice.getMemo() == null ? "" : invoice.getMemo());
                obj.put("shipvia", invoice.getShipvia() == null ? "" : invoice.getShipvia());
                obj.put("fob", invoice.getFob() == null ? "" : invoice.getFob());
                boolean includeprotax = false;
                obj.put("includeprotax", includeprotax);
                obj.put("taxincluded", invoice.getTax() != null);
                obj.put("taxid", invoice.getTax() == null ? "" : invoice.getTax().getID());
                obj.put("taxname", invoice.getTax() == null ? "" : invoice.getTax().getName());
                obj.put("salesPerson", invoice.getSalesperson() == null ? "" : invoice.getSalesperson().getID());
                obj.put("salespersonname", invoice.getSalesperson() == null ? "" : invoice.getSalesperson().getValue());
                obj.put("billno", invoice.getSalesOrderNumber());
                obj.put("currencyid", currencyid);
                obj.put("currencysymbol", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                obj.put("currencycode", (invoice.getCurrency() == null ? currency.getCurrencyCode() : invoice.getCurrency().getCurrencyCode()));
                KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, invoice.getOrderDate(), 0);
                obj.put("oldcurrencyrate", (Double) bAmt.getEntityList().get(0));
                obj.put("date", df.format(invoice.getOrderDate()));
                BillingShippingAddresses addresses = invoice.getBillingShippingAddresses();
                AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);

                String idvString = !StringUtil.isNullOrEmpty(oj[3].toString()) ? oj[3].toString() : ""; //as in list invoiedetail id comes 4th
                KwlReturnObject objItrID = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), idvString);
                SalesOrderDetail idvObj = (SalesOrderDetail) objItrID.getEntityList().get(0);
                double rowbaseuomquantity=0d;
                if (idvObj != null) {
                    Product product = idvObj.getProduct();
                    obj.put("rowproductname", product.getName());
                    obj.put("rowproductid", product.getProductid());
                    obj.put("productid", product.getID());
                    obj.put("rowproductdescription", StringUtil.isNullOrEmpty(idvObj.getDescription()) ? "" : idvObj.getDescription());
                    double quantity = idvObj.getQuantity();
                    obj.put("rowquantity", authHandler.formattedQuantity(quantity, companyid)); //To show quantity with four decimal point in PDF & Print
                    double baseumrate = 1;
                    baseumrate = idvObj.getBaseuomrate();
                    rowbaseuomquantity=authHandler.calculateBaseUOMQuatity(quantity, baseumrate, companyid);
                    obj.put("rowbaseuomquantity", authHandler.formattedQuantity(rowbaseuomquantity, companyid));
                    double lockqty=idvObj.getLockquantity();
                    obj.put("lockquantity", lockqty); 
                    if (Constants.CONSIGNMENT_SALES_ORDER_TYPE == salesOrderTypeId) {
                        double lockQuantity = idvObj.getLockquantity();
                        if (product.isIsSerialForProduct()) {
                            lockQuantity = accCommonTablesDAO.getserialAssignedQty(idvObj.getID());
                        } else {
                            lockQuantity = accCommonTablesDAO.getbatchAssignedQty(idvObj.getID());
                        }
                        KwlReturnObject deliveredQtyResult = accSalesOrderDAOobj.getDeliveredQuantityForSalesOrder(idvObj.getID(), companyid);
                        double deliveredOty = 0.0;
                        if (deliveredQtyResult != null && deliveredQtyResult.getEntityList() != null && !deliveredQtyResult.getEntityList().isEmpty()) {
                            deliveredOty = (Double) deliveredQtyResult.getEntityList().get(0);
                        }
                        double remainingQty = lockQuantity - deliveredOty;
                        if (!(remainingQty > 0)) {
                            continue;
                        }
                        obj.put("lockquantity", remainingQty);
                    }
                    obj.put("uomname", product.getUnitOfMeasure() == null ? "" : product.getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("rowrate", idvObj.getRate());

                    double amount = idvObj.getRate() * quantity;
                    double rdisc = 0;
                    if (idvObj.getDiscountispercent() == 1) {
                        rdisc = (amount * idvObj.getDiscount()) / 100;
                    } else {
                        rdisc = idvObj.getDiscount();
                    }

                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable = false;
                    if (idvObj.getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(invoice.getCompany().getCompanyID(), invoice.getOrderDate(), idvObj.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                    }
                    rowTaxAmount = idvObj.getRowTaxAmount();
                    rowTaxAmount = (amount - rdisc) * rowTaxPercent / 100;
                    double ramount = amount - rdisc;
                    double amountWithoutTax = amount - rdisc;
                    ramount += rowTaxAmount;//ramount+=ramount*rowTaxPercent/100;
                    obj.put("amount", authHandler.round(amount, companyid));
                    double amountInBase = (Double) accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountWithoutTax, currencyid, idvObj.getSalesOrder().getOrderDate(), idvObj.getSalesOrder().getExternalCurrencyRate()).getEntityList().get(0);
                    obj.put("amountinbase", amountInBase);

                    obj.put("rowprtaxpercent", rowTaxPercent);

                    if (rowTaxPercent > 0) {
                        obj.put("amountinbasewithtax", amountInBase + (rowTaxAmount));//obj.put("amountinbasewithtax", amountInBase + (amountInBase * rowTaxPercent/100));
                    } else {
                        obj.put("amountinbasewithtax", amountInBase);
                    }
                }

                requestParams.put("taxtype", 2);
                double percent = 0.0;
                KwlReturnObject result = accTaxObj.getTax(requestParams);
                List<Object[]> listTax = result.getEntityList();

                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getInvoiceJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
  
    public ModelAndView unlinkCustomerQuotationDocuments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        StringBuffer msg = new StringBuffer();
        boolean issuccess = false;
        try {
            int linkType = -1; 
            String linkedDocNo = "";
            String billid = (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) ? (String) request.getParameter("billid") : "";
            String quotationid = request.getParameter("billid");

            JSONArray documentArr = new JSONArray(request.getParameter("data"));

            String salesInvoiceNoLinkedWithCQ = "";
            String salesOrderNoLinkedWithCQ = "";
            String vendorQuotationLinkedWithCQ="";
            String cqNo = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isAvalaraIntegration = false;
            KwlReturnObject result = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty() && result.getEntityList().get(0) != null) {
                ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) result.getEntityList().get(0);
                isAvalaraIntegration = extraCompanyPreferences.isAvalaraIntegration();
            }
            if (!StringUtil.isNullOrEmpty(billid) && documentArr != null && documentArr.length() > 0) {
                HashMap<String, Object> linkingrequestParams = new HashMap<String, Object>();

                result = accountingHandlerDAOobj.getObject(Quotation.class.getName(), billid);
                Quotation quotation = (Quotation) result.getEntityList().get(0);
                cqNo = quotation.getQuotationNumber();

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("quotation.ID");
                filter_params.add(quotation.getID());
                requestParams.put("filter_names", filter_names);
                requestParams.put("filter_params", filter_params);
                for (int i = 0; i < documentArr.length(); i++) {
                    JSONObject document = documentArr.getJSONObject(i);
                    int type = document.optInt("type", -1);
                    String linkedDocumentID = document.optString("billid", "");

                    if (!StringUtil.isNullOrEmpty(billid) && type != -1) {
                        QuotationDetail quotatioDetail = null;
                        if (type == 7) {//CQ->SI

                            /**
                             * passing isAvalaraIntegration flag because deleted invoices are also to be unlinked in case of Avalara integration
                             */
                            KwlReturnObject invDetailsResult = accInvoiceDAOobj.getInvoiceLinkedWithCQ(linkedDocumentID, quotationid, companyid, isAvalaraIntegration);
                            List<InvoiceDetail> invoicedetails = invDetailsResult.getEntityList();
                            if (invoicedetails != null) {
                                for (InvoiceDetail invoiceDetail : invoicedetails) {
                                    if (invoiceDetail.getQuotationDetail() != null) {
                                        quotatioDetail = invoiceDetail.getQuotationDetail();
                                        invoiceDetail.setQuotationDetail(null);
                                        quotatioDetail.getQuotation().setIsopen(true);

                                        if (salesInvoiceNoLinkedWithCQ.indexOf(invoiceDetail.getInvoice().getInvoiceNumber()) == -1) {
                                            salesInvoiceNoLinkedWithCQ += invoiceDetail.getInvoice().getInvoiceNumber() + ",";
                                        }
                                    }

                                }
                                
                                KwlReturnObject resultso = accSalesOrderDAOobj.checkQuotationLinkedWithAnotherInvoice(quotationid);//Returning no of Invoice linked with Quaotation

                                Long count =(Long) resultso.getEntityList().get(0);
                                if (count == 1) {
                                    quotatioDetail.getQuotation().setLinkflag(0);
                                }
                                linkingrequestParams.clear();
                                linkingrequestParams.put("billid", linkedDocumentID);
                                linkingrequestParams.put("linkedTransactionID", quotationid);
                                linkingrequestParams.put("type", type);
                                linkingrequestParams.put("unlinkflag", true);
                                accInvoiceDAOobj.deleteLinkingInformationOfSI(linkingrequestParams);//Deleting linking information of DO during Unlinking DO.
                            }
                        } else if (type == 4) {//CQ->SO
                            KwlReturnObject soDetailsResult = accInvoiceDAOobj.getSalesOrderLinkedWithCQ(linkedDocumentID, quotationid, companyid);
                            List<SalesOrderDetail> salesorderdetails = soDetailsResult.getEntityList();
                            if (salesorderdetails != null) {
                                for (SalesOrderDetail salesorderdetail : salesorderdetails) {
                                    if (salesorderdetail.getQuotationDetail() != null) {
                                        quotatioDetail = salesorderdetail.getQuotationDetail();
                                        salesorderdetail.setQuotationDetail(null);
                                        quotatioDetail.getQuotation().setIsopen(true);

                                        if (salesOrderNoLinkedWithCQ.indexOf(salesorderdetail.getSalesOrder().getSalesOrderNumber()) == -1) {
                                            salesOrderNoLinkedWithCQ += salesorderdetail.getSalesOrder().getSalesOrderNumber() + ",";
                                        }
                                    }
                                }
                                KwlReturnObject resultso = accSalesOrderDAOobj.checkQuotationLinkedWithAnotherSalesOrder(quotationid);

                                Long count =(Long) resultso.getEntityList().get(0);
                                if (count==1) {
                                    quotatioDetail.getQuotation().setLinkflag(0);
                                }
                                linkingrequestParams.clear();
                                linkingrequestParams.put("soid", linkedDocumentID);
                                linkingrequestParams.put("linkedTransactionID", billid);
                                linkingrequestParams.put("type", type);
                                linkingrequestParams.put("unlinkflag", true);
                                accSalesOrderDAOobj.deleteLinkingInformationOfSO(linkingrequestParams);//Deleting linking information of DO during Unlinking DO.
                            }
                        } else if (type == 5) {//CQ->VQ
                            KwlReturnObject quotationDetailsResult = accSalesOrderDAOobj.getQuotationDetails(requestParams);
                            List<QuotationDetail> quotationDetailList = quotationDetailsResult.getEntityList();

                            for (QuotationDetail quotationDetail : quotationDetailList) {
                                if (quotationDetail.getVendorquotationdetails() != null) {
                                    KwlReturnObject objResult = accountingHandlerDAOobj.getObject(VendorQuotationDetail.class.getName(), quotationDetail.getVendorquotationdetails());
                                    VendorQuotationDetail vendorQuotationDetail = (VendorQuotationDetail) objResult.getEntityList().get(0);

                                    if (vendorQuotationDetail.getVendorquotation().getID().equals(linkedDocumentID)) {
                                        if (vendorQuotationDetail != null && vendorQuotationLinkedWithCQ.indexOf(vendorQuotationDetail.getVendorquotation().getQuotationNumber()) == -1) {
                                            vendorQuotationLinkedWithCQ += vendorQuotationDetail.getVendorquotation().getQuotationNumber() + ",";
                                        }
                                        quotationDetail.setVendorquotationdetails(null);
                                        quotationDetail.getQuotation().setIsopen(true);
                                        quotationDetail.getQuotation().setLinkflag(0);

                                    }
                                }
                            }
                            
                            linkingrequestParams.clear();
                            linkingrequestParams.put("linkedTransactionID", linkedDocumentID);
                            linkingrequestParams.put("qid", billid);
                            linkingrequestParams.put("type", type);
                            linkingrequestParams.put("unlinkflag", true);
                            accSalesOrderDAOobj.deleteLinkingInformationOfCQ(linkingrequestParams);
                        }
                    }
                }

                if (!StringUtil.isNullOrEmpty(cqNo) && !StringUtil.isNullOrEmpty(vendorQuotationLinkedWithCQ)) {
                    msg.append(messageSource.getMessage("acc.field.vendorQuotation(s)", null, RequestContextUtils.getLocale(request)) + " " + vendorQuotationLinkedWithCQ.substring(0, vendorQuotationLinkedWithCQ.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.accPref.autoCQN", null, RequestContextUtils.getLocale(request)) + " " + cqNo + ".");
                    issuccess = true;
                    msg.append("<br>");
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_VQ_FROM_CQ, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Customer Quotation " + cqNo + " from the Vendor Quotation(s) " + vendorQuotationLinkedWithCQ.substring(0, vendorQuotationLinkedWithCQ.length() - 1) + ".", request, cqNo);
                }

               if (!StringUtil.isNullOrEmpty(cqNo) && !StringUtil.isNullOrEmpty(salesInvoiceNoLinkedWithCQ)) {
                    msg.append(messageSource.getMessage("acc.field.SalesInvoice(s)", null, RequestContextUtils.getLocale(request)) + " " + salesInvoiceNoLinkedWithCQ.substring(0, salesInvoiceNoLinkedWithCQ.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.accPref.autoCQN", null, RequestContextUtils.getLocale(request)) + " " + cqNo + ".");
                    issuccess = true;
                    msg.append("<br>");
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_CQ_FROM_SI, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Customer Quotation " + cqNo + " from Sales Invoice " + salesInvoiceNoLinkedWithCQ.substring(0, salesInvoiceNoLinkedWithCQ.length() - 1) + ".", request, cqNo);
                }
                if (!StringUtil.isNullOrEmpty(cqNo) && !StringUtil.isNullOrEmpty(salesOrderNoLinkedWithCQ)) {
                    msg.append(messageSource.getMessage("acc.field.salesOrder(s)", null, RequestContextUtils.getLocale(request)) + " " + salesOrderNoLinkedWithCQ.substring(0, salesOrderNoLinkedWithCQ.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.accPref.autoCQN", null, RequestContextUtils.getLocale(request)) + " " + cqNo + ".");
                    issuccess = true;
                    msg.append("<br>");
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_CQ_FROM_SO, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Customer Quotation " + cqNo + " from Sales Order(s) " + salesOrderNoLinkedWithCQ.substring(0, salesOrderNoLinkedWithCQ.length() - 1) + ".", request, cqNo);
                }
            }
        } catch (Exception ex) {
            msg.append("accSalesOrderControllerCMN.unlinkCustomerQuotationDocuments:" + ex.getMessage());
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /*Function to be used to unlink linked documents to/from sales order
     type=0 SO->SI
     type=1 SO->DO
     type=2 SO->PO
     type=3 PO->SO
     type=4 CQ->SO
     */
    public ModelAndView unlinkSalesOrderDocuments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
//        String msg = "";
        StringBuffer msg = new StringBuffer();
        boolean issuccess = false;
        try {
//            int linkType = -1; // linkType = 1 - Customer Quotation, 2 - Purchase Order and -1 - No linking available
//            String linkedDocNo = "";
            JSONArray documentArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String billid = (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) ? (String) request.getParameter("billid") : "";
            if (!StringUtil.isNullOrEmpty(billid) && documentArr != null && documentArr.length() > 0) {
                String cqNo = "", doNo = "", poNo = "", siNo = "";
                KwlReturnObject result = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), billid);
                SalesOrder salesOrder = (SalesOrder) result.getEntityList().get(0);
                String soNo = salesOrder.getSalesOrderNumber();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                HashMap<String, Object> linkingrequestParams = new HashMap<String, Object>();
                for (int i = 0; i < documentArr.length(); i++) {
                    JSONObject document = documentArr.getJSONObject(i);
                    int type = document.optInt("type", -1);
                    String linkedTransactionID = document.optString("billid", "");
                    if (!StringUtil.isNullOrEmpty(linkedTransactionID) && type != -1) {
                        if (type == 0) {// SO->SI
                            filter_names.clear();
                            filter_params.clear();
                            filter_names.add("invoice.ID");
                            filter_params.add(linkedTransactionID);
                            filter_names.add("salesorderdetail.salesOrder.ID");
                            filter_params.add(billid);
                            requestParams.put("filter_names", filter_names);
                            requestParams.put("filter_params", filter_params);
                            KwlReturnObject invDetailsResult = accInvoiceDAOobj.getInvoiceDetails(requestParams);
                            List<InvoiceDetail> invoiceDetailList = invDetailsResult.getEntityList();
                            if (invoiceDetailList != null && invoiceDetailList.size() > 0) {
                                for (InvoiceDetail invoiceDetail : invoiceDetailList) {
                                    if (invoiceDetail.getSalesorderdetail() != null) {
                                        SalesOrderDetail salesOrderDetail = invoiceDetail.getSalesorderdetail();
                                        if (siNo.indexOf(invoiceDetail.getInvoice().getInvoiceNumber()) == -1) {
                                            siNo += invoiceDetail.getInvoice().getInvoiceNumber() + ",";
                                        }
              
                                         /* updating isLineItemClosed & isSOClosed flag to true if SO is unlinked from SI that was linked with DO i.e SO->SI->DO*/
                                        KwlReturnObject doresult = accSalesOrderDAOobj.checkWhetherSOIsUsedInDOOrNot(salesOrderDetail.getID(), companyid);
                                        List list1 = doresult.getEntityList();
                                        if (list1.size() > 0) {
                                           invoiceDetail.getSalesorderdetail().getSalesOrder().setIsSOClosed(false);
                                           invoiceDetail.getSalesorderdetail().setIsLineItemClosed(false);
                                           
                                            /*Updating balance quantity of SO, if SI is linked with SO and SI is linked with DO */
                                            String doDetailId = (String) doresult.getEntityList().get(0);
                                            result = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), doDetailId);
                                            DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) result.getEntityList().get(0);
                                            
                                            HashMap soMap = new HashMap();
                                            soMap.put("sodetails", salesOrderDetail.getID());
                                            soMap.put("companyid", companyid);
                                            soMap.put("balanceqty", deliveryOrderDetail.getDeliveredQuantity());
                                            soMap.put("add", true);
                                            accCommonTablesDAO.updateSalesorderOrderStatus(soMap);
                                        }
                                        invoiceDetail.setSalesorderdetail(null);
                                        salesOrderDetail.getSalesOrder().setLinkflag(0);
                                        salesOrderDetail.getSalesOrder().setIsopen(true);
                                    }
                                }
                                linkingrequestParams.clear();
                                linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                                linkingrequestParams.put("soid", billid);
                                linkingrequestParams.put("type", type);
                                linkingrequestParams.put("unlinkflag", true);
                                accSalesOrderDAOobj.deleteLinkingInformationOfSO(linkingrequestParams);
                                }
                        } else if (type == 1) { // SO->DO
                            KwlReturnObject doDetailsResult = accInvoiceDAOobj.getDODetailsFromSalesOrder(billid, linkedTransactionID, companyid);
                            // update the salesorder order balance quantity
                            accInvoiceDAOobj.updateSOBalanceQtyAfterDO(linkedTransactionID,billid, companyid);
            
                            /* updating isSOClosed flag to false if SO is unlinked from DO i.e SO->DO*/
                            salesOrder.setIsSOClosed(false);
                            
                            List<DeliveryOrderDetail> dodetails = doDetailsResult.getEntityList();
                            for (DeliveryOrderDetail deliveryOrderDetail : dodetails) {
                                if (deliveryOrderDetail.getSodetails() != null) { // SO linked in DO
                                    SalesOrderDetail salesOrderDetail = deliveryOrderDetail.getSodetails();
                                    if (doNo.indexOf(deliveryOrderDetail.getDeliveryOrder().getDeliveryOrderNumber()) == -1) {
                                        doNo += deliveryOrderDetail.getDeliveryOrder().getDeliveryOrderNumber() + ",";
                                    }
                                    deliveryOrderDetail.setSodetails(null);
                                    salesOrderDetail.getSalesOrder().setLinkflag(0);
                                    salesOrderDetail.getSalesOrder().setIsopen(true);
                                    salesOrderDetail.setIsLineItemClosed(false);
                                }
                                linkingrequestParams.clear();
                                linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                                linkingrequestParams.put("soid", billid);
                                linkingrequestParams.put("type", type);
                                linkingrequestParams.put("unlinkflag", true);
                                accSalesOrderDAOobj.deleteLinkingInformationOfSO(linkingrequestParams);
                            }
                        } else if (type == 2 || type == 3 || type == 4) { // type=2, type=3 PO->SO, type=4 CQ->SO
                            filter_names.clear();
                            filter_params.clear();
                            filter_names.add("salesOrder.ID");
                            filter_params.add(salesOrder.getID());
                            if (type == 4) {
                                filter_names.add("quotationDetail.quotation.ID");
                                filter_params.add(linkedTransactionID);
                            }
                            requestParams.put("filter_names", filter_names);
                            requestParams.put("filter_params", filter_params);
                            KwlReturnObject soDetailResult = accSalesOrderDAOobj.getSalesOrderDetails(requestParams);
                            List<SalesOrderDetail> soDetailList = soDetailResult.getEntityList();
                            if (soDetailList != null && soDetailList.size() > 0) {
                                if (type == 2 || type == 3) { // type=2  SO->PO. type=3 PO->SO
                                    KwlReturnObject poObj = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), linkedTransactionID);
                                    PurchaseOrder purchaseOrder = (PurchaseOrder) poObj.getEntityList().get(0);
                                    if (purchaseOrder != null) {
                                        Set<PurchaseOrderDetail> podetails = purchaseOrder.getRows();
                                        if (podetails != null && podetails.size() > 0) {
                                            if (type == 3) {
                                                for (SalesOrderDetail salesOrderDetail : soDetailList) {
                                                    if (salesOrderDetail.getPurchaseorderdetailid() != null && !StringUtil.isNullOrEmpty(salesOrderDetail.getPurchaseorderdetailid())) {
                                                        for (PurchaseOrderDetail purchaseOrderDetail : podetails) {
                                                            if (purchaseOrderDetail!=null && !StringUtil.isNullOrEmpty(salesOrderDetail.getPurchaseorderdetailid()) && salesOrderDetail.getPurchaseorderdetailid().equals(purchaseOrderDetail.getID())) {
                                                                if (purchaseOrderDetail != null && poNo.indexOf(purchaseOrderDetail.getPurchaseOrder().getPurchaseOrderNumber()) == -1) {
                                                                    poNo += purchaseOrderDetail.getPurchaseOrder().getPurchaseOrderNumber() + ",";
                                                                }
                                                                salesOrderDetail.setPurchaseorderdetailid(null);
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                                linkingrequestParams.clear();
                                                linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                                                linkingrequestParams.put("soid", billid);
                                                linkingrequestParams.put("type", type);
                                                linkingrequestParams.put("unlinkflag", true);
                                                accSalesOrderDAOobj.deleteLinkingInformationOfSO(linkingrequestParams);
                                            } else if (type == 2) {
                                                for (PurchaseOrderDetail purchaseOrderDetail : podetails) {
                                                    if (purchaseOrderDetail.getSalesorderdetailid() != null && !StringUtil.isNullOrEmpty(purchaseOrderDetail.getSalesorderdetailid())) {
                                                        for (SalesOrderDetail salesOrderDetail : soDetailList) {
                                                            if (salesOrderDetail!=null && !StringUtil.isNullOrEmpty(purchaseOrderDetail.getSalesorderdetailid()) && purchaseOrderDetail.getSalesorderdetailid().equals(salesOrderDetail.getID())) {
                                                                purchaseOrderDetail.setSalesorderdetailid(null);
                                                                if (purchaseOrderDetail != null && poNo.indexOf(purchaseOrderDetail.getPurchaseOrder().getPurchaseOrderNumber()) == -1) {
                                                                    poNo += purchaseOrderDetail.getPurchaseOrder().getPurchaseOrderNumber() + ",";
                                                                }
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                                linkingrequestParams.clear();
                                                linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                                                linkingrequestParams.put("soid", billid);
                                                linkingrequestParams.put("type", type);
                                                linkingrequestParams.put("unlinkflag", true);
                                                accSalesOrderDAOobj.deleteLinkingInformationOfSO(linkingrequestParams);
                                            }

                                        }
                                    }
                                } else if (type == 4) {// CQ->SO
                                    for (SalesOrderDetail salesOrderDetail : soDetailList) {
                                        if (salesOrderDetail.getQuotationDetail() != null) {
                                            QuotationDetail quotationDetail = salesOrderDetail.getQuotationDetail();
                                            if (cqNo.indexOf(quotationDetail.getQuotation().getQuotationNumber()) == -1) {
                                                cqNo += quotationDetail.getQuotation().getQuotationNumber() + ",";
                                            }
                                            salesOrderDetail.setQuotationDetail(null);
                                            quotationDetail.getQuotation().setLinkflag(0);
                                            quotationDetail.getQuotation().setIsopen(true);
                                        }
                                    }
                                    linkingrequestParams.clear();
                                    linkingrequestParams.put("linkedTransactionID", linkedTransactionID);
                                    linkingrequestParams.put("soid", billid);
                                    linkingrequestParams.put("type", type);
                                    linkingrequestParams.put("unlinkflag", true);
                                    accSalesOrderDAOobj.deleteLinkingInformationOfSO(linkingrequestParams);
                                }

                            }
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(soNo) && !StringUtil.isNullOrEmpty(siNo)) { // type=0 SO linked in SI
                    msg.append(messageSource.getMessage("acc.field.salesOrder(s)", null, RequestContextUtils.getLocale(request)) + " " + soNo + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.field.CustomerInvoice", null, RequestContextUtils.getLocale(request)) + " " + siNo.substring(0, siNo.length() - 1) + ".");
                    issuccess = true;
                    msg.append("<br>");
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_SO_FROM_SI, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlink " + "Sales Invoice " + siNo.substring(0, siNo.length() - 1) + " from the Sales Order(s) " + soNo + ".", request, siNo.substring(0, siNo.length() - 1));
                }
                if (!StringUtil.isNullOrEmpty(doNo) && !StringUtil.isNullOrEmpty(soNo)) { // type=1 SO linked in DO
                    msg.append(messageSource.getMessage("acc.field.salesOrder(s)", null, RequestContextUtils.getLocale(request)) + " " + soNo + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.wtfTrans.do", null, RequestContextUtils.getLocale(request)) + " " + doNo.substring(0, doNo.length() - 1) + ".");
                    msg.append("<br>");
                    issuccess = true;
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_SO_FROM_DO, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlink " + "Delivery Order " + doNo.substring(0, doNo.length() - 1) + " from the Sales Order(s) " + soNo + ".", request, doNo.substring(0, doNo.length() - 1));
                }
                if (!StringUtil.isNullOrEmpty(poNo) && !StringUtil.isNullOrEmpty(soNo)) { // type=3 PO linked in SO
                    msg.append(messageSource.getMessage("acc.field.purchaseOrder(s)", null, RequestContextUtils.getLocale(request)) + " " + poNo.substring(0, poNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.wtfTrans.so", null, RequestContextUtils.getLocale(request)) + " " + soNo + ".");
                    msg.append("<br>");
                    issuccess = true;
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_PO_FROM_SO, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlink " + "Sales Order " + soNo + " from the Purchase Order(s) " + poNo.substring(0, poNo.length() - 1) + ".", request, soNo);
                }
                if (!StringUtil.isNullOrEmpty(cqNo) && !StringUtil.isNullOrEmpty(soNo)) { // type=4 CQ linked in SO
                    msg.append(messageSource.getMessage("acc.field.CustomerQuotation(s)", null, RequestContextUtils.getLocale(request)) + " " + cqNo.substring(0, cqNo.length() - 1) + " " + messageSource.getMessage("acc.field.hasBeenUnlinkedFromSelected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.wtfTrans.so", null, RequestContextUtils.getLocale(request)) + " " + soNo + ".");
                    msg.append("<br>");
                    issuccess = true;
                    auditTrailObj.insertAuditLog(AuditAction.UNLINK_CQ_FROM_SO, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlink " + "Sales Order " + soNo + " from the Customer Quotation(s) " + cqNo.substring(0, cqNo.length() - 1) + ".", request, soNo);
                }
            }
        } catch (Exception ex) {
            String exceptionMSG = "accSalesOrderControllerCMN.unlinkSalesOrderDocuments:" + ex.getMessage();
            msg = new StringBuffer(exceptionMSG);
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg.toString());
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getMonthlyCommissionOfSalesPersonReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("itemid", request.getParameter("itemid"));
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            KwlReturnObject result = accInvoiceDAOobj.getSalesOrderForSalesPerson(requestParams);
            
            JSONArray DataJArr = getMonthlyCommissionOfSalesPersonReport(request, result.getEntityList());
            int count = DataJArr.length();
            JSONArray pagedJson = DataJArr;
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", count);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getMonthlyCommissionOfSalesPersonReport(HttpServletRequest request, List<SalesOrder> list) throws SessionExpiredException, ServiceException {
        JSONArray dataJArr = new JSONArray();
        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (SalesOrder salesOrder : list) {
                boolean isAdd = true;
                JSONObject obj = new JSONObject();
                obj.put("salesPersonName", salesOrder.getSalesperson().getValue());
                obj.put("salesOrderNo", salesOrder.getSalesOrderNumber());
                obj.put("salesOrderID", salesOrder.getID());
                obj.put("customerName", salesOrder.getCustomer().getName());
                obj.put("currencysymboltransaction", salesOrder.getCurrency() != null ? salesOrder.getCurrency().getSymbol() : "");
                
                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
                requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
                requestParams.put("soid", salesOrder.getID());
                KwlReturnObject result = accInvoiceDAOobj.getInvoicesFromSOForSalesCommission(requestParams);
                List<String> invList = result.getEntityList();
                String invNo = "";
                String invoiceID = "";
                for (String invID : invList) {
                    KwlReturnObject invObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invID);
                    Invoice invoice = (Invoice) invObj.getEntityList().get(0);
                    
                    invNo += invoice.getInvoiceNumber() + ", ";
                    invoiceID += invoice.getID() + ", ";
                    if (invoice.getInvoiceamountdue() > 0) {
                        isAdd = false;
                        break;
                    }
                }
                obj.put("invoiceNo", StringUtil.isNullOrEmpty(invNo)?"":invNo.substring(0, invNo.length() - 2));
                obj.put("doId", StringUtil.isNullOrEmpty(invoiceID)?"":invoiceID.substring(0, invoiceID.length() - 2));
                
                double amountInBase = 0;
                double amount= 0;
                double totalCost = 0;
                double totalCostinDocumentCurr = 0;
                Set<SalesOrderDetail> salesOrderDetails = salesOrder.getRows();
                for (SalesOrderDetail salesOrderDetail : salesOrderDetails) {
                    KwlReturnObject sodVenRes = accSalesOrderDAOobj.getSalesOrderDetailsVendorMapping(salesOrderDetail.getID());
                    if (sodVenRes.getEntityList().size() > 0) {
                        SODetailsVendorMapping sodVendObj = (SODetailsVendorMapping) sodVenRes.getEntityList().get(0);
                        totalCost += sodVendObj.getTotalcost();
                        KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, sodVendObj.getTotalcost(), salesOrder.getCurrency().getCurrencyID(), salesOrderDetail.getSalesOrder().getOrderDate(), 0);
                        double amt = (Double) bAmt.getEntityList().get(0);
                        totalCostinDocumentCurr += amt;
                    }
                    
                    String currencyid = (salesOrder.getCurrency() == null ? currency.getCurrencyID() : salesOrder.getCurrency().getCurrencyID());
                    double discountPrice = 0;
                    double soPrice = salesOrderDetail.getQuantity() * salesOrderDetail.getRate();
                    double discountSOD = salesOrderDetail.getDiscount();

                    if (salesOrderDetail.getDiscountispercent() == 1) {
                        discountPrice = (soPrice) - authHandler.round((soPrice * discountSOD / 100), companyid);
                    } else {
                        discountPrice = soPrice - discountSOD;
                    }
                    double amountWithoutTax = authHandler.round((Double) discountPrice, companyid);
                    amount += amountWithoutTax;
                    amountInBase += (Double) accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountWithoutTax, currencyid, salesOrderDetail.getSalesOrder().getOrderDate(), salesOrderDetail.getSalesOrder().getExternalCurrencyRate()).getEntityList().get(0);
                }
                
                double gp = amountInBase - totalCost;
                double gpInDocumentCurr = amount - totalCostinDocumentCurr;
                gp = authHandler.round(gp, companyid);
                double gpPercent = (totalCost==0) ? (gp!=0 ? 100 : 0) : (gp / amountInBase) * 100;
                gpPercent = authHandler.round(gpPercent, companyid);
                
                obj.put("totalBilledAmount", amountInBase);
                obj.put("totalBilledAmountInDoc", amount);
                obj.put("totalCost", totalCost);
                obj.put("totalCostInDoc", totalCostinDocumentCurr);
                obj.put("gp", gp);
                obj.put("gpInDoc", gpInDocumentCurr);
                obj.put("gpPercent", gpPercent);
                // To Show the Sales Person 2 and Project Manager Column in Monthly Commission of the Sales Person Report
                if (storageHandlerImpl.SBICompanyId()!=null && storageHandlerImpl.SBICompanyId().toString().equals(companyid)) {

                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();

                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Sales_Order_ModuleId));

                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    SalesOrderCustomData jeDetailCustom = (SalesOrderCustomData) salesOrder.getSoCustomData();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyid);
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }

                }
                
                if (!StringUtil.isNullOrEmpty(invNo) && isAdd) {
                    dataJArr.put(obj);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataJArr;
    }
    
    public ModelAndView exportMonthlyCommissionOfSalesPersonReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(Constants.REQ_startdate, request.getParameter("startdate"));
            requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
            requestParams.put("itemid", request.getParameter("itemid"));
            
            KwlReturnObject result = accInvoiceDAOobj.getSalesOrderForSalesPerson(requestParams);

            List list = result.getEntityList();
            JSONArray DataJArr = getMonthlyCommissionOfSalesPersonReport(request, list);
            jobj.put("data", DataJArr);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public ModelAndView approveSalesOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
            String currentUser = sessionHandlerImpl.getUserid(request);
            String remark = request.getParameter("remark");
            String doID = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String gcurrencyid = paramJobj.optString(Constants.globalCurrencyKey);
//            double totalOrderAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), doID);
            SalesOrder cqObj = (SalesOrder) CQObj.getEntityList().get(0);
            /**
             * This function is called at the time of approval of SO, SO can be
             * approved from Dashboard pending report or SO Pending report to
             * identify the call is from dashboard report
             * isDocumentApprovedFromDashboradReport is set as true. 
             * Case 1: user approves from Dashboard pending report then totalSUM i.e
             * SO Total Amount and totalOrderAmount i.e SO amount in base is not
             * passed in parameter so getting it in from salesOrderMerged
             * function of single so by passing its id, and ispendingAproval as
             * true and pending as true as we need only pending SO we cannot
             * pass it in parameter as it is not available on JS in store
             * ERP-38444.
             */
            HashMap requestParams=null;
            boolean isDocumentApprovedFromDashboradReport = false;
            KwlReturnObject result = null;
            JSONArray DataJArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(request.getParameter("isDocumentApprovedFromDashboradReport")) && (Boolean.parseBoolean(request.getParameter("isDocumentApprovedFromDashboradReport").toString()))) {
                isDocumentApprovedFromDashboradReport = Boolean.parseBoolean(request.getParameter("isDocumentApprovedFromDashboradReport").toString());
                requestParams = new HashMap();
                requestParams.put("ispendingAproval", true);
                requestParams.put("pendingapproval", true);
                requestParams.put("billId", doID);
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
//                requestParams.put("paramJobj", paramJobj);

//                result = accSalesOrderDAOobj.getSalesOrdersMerged(requestParams);
//                DataJArr = accSalesOrderServiceDAOobj.getSalesOrdersJsonMerged(paramJobj, result.getEntityList(), DataJArr);
                
                DataJArr = accSalesOrderServiceDAOobj.getSalesOrdersJson(requestParams, DataJArr,paramJobj);
            }
            double totalOrderAmount = 0;
            JSONObject resultJSON =new JSONObject();
            if (isDocumentApprovedFromDashboradReport){
                if(DataJArr.length()>0 && DataJArr.getJSONObject(0)!=null){
                    resultJSON = DataJArr.optJSONObject(0);
                    
                }
            } 
            if (isDocumentApprovedFromDashboradReport && StringUtil.isNullOrEmpty(request.getParameter("totalorderamount"))) {
                totalOrderAmount = resultJSON.optDouble("amountinbase");
            } else {
                totalOrderAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
            }

            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject extraPrefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCMPPreferences = (ExtraCompanyPreferences) extraPrefresult.getEntityList().get(0);

            double totalProfitMargin = 0;
            double totalProfitMarginPerc = 0;
            HashMap<String, Object> qApproveMap = new HashMap<String, Object>();
            totalProfitMargin = cqObj.getTotalProfitMargin();
            totalProfitMarginPerc = cqObj.getTotalProfitMarginPercent();
            int level = cqObj.getApprovestatuslevel();
            String currencyid=cqObj.getCurrency()!=null?cqObj.getCurrency().getCurrencyID():sessionHandlerImpl.getCurrencyID(request);
            // Add Product and discounts mapping
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            JSONArray productDiscountJArr=new JSONArray();
            Set<SalesOrderDetail> salesOrderDetails = cqObj.getRows();
            for (SalesOrderDetail soDetail : salesOrderDetails) {
                String productId = soDetail.getProduct().getID();
                double discountVal = soDetail.getDiscount();
                int isDiscountPercent = soDetail.getDiscountispercent();
                if(isDiscountPercent==1){
                    discountVal = (soDetail.getQuantity()*soDetail.getRate())*(discountVal/100);
                }
                KwlReturnObject dAmount = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, cqObj.getOrderDate(), cqObj.getExternalCurrencyRate());
                double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                discAmountinBase = authHandler.round(discAmountinBase, companyid);
                JSONObject productDiscountObj=new JSONObject();
                productDiscountObj.put("productId", productId);
                productDiscountObj.put("discountAmount", discAmountinBase);
                productDiscountJArr.put(productDiscountObj);
            }
            qApproveMap.put(Constants.companyKey, companyid);
            qApproveMap.put("level", level);
            qApproveMap.put("totalAmount", String.valueOf(totalOrderAmount));
            qApproveMap.put("totalProfitMargin", totalProfitMargin);
            qApproveMap.put("totalProfitMarginPerc", totalProfitMarginPerc);
            qApproveMap.put("currentUser", currentUser);
            qApproveMap.put("fromCreate", false);
            qApproveMap.put("productDiscountMapList", productDiscountJArr);
            qApproveMap.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
            qApproveMap.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
            
            
            /**
             * Checking whether 'Include Current Transaction Amount for Credit
             * Limit Check' of Customer So Credit Control is enabled in Company
             * Preferences. If yes then Checking amount due of customer is
             * greater than credit limit of customer if yes then sending SO for
             * approval.
             */
            Customer customer=cqObj.getCustomer();
            double customerCreditLimit = customer.getCreditlimit();
            double salesOrderAmount=cqObj.getTotalamountinbase();
            boolean isLimitExceeding=false;
            double totalSOAmt = cqObj.getTotalamount();
            paramJobj.put("isCallFromApproveSalesOrder", true);           //ERM-396
            if (isDocumentApprovedFromDashboradReport && StringUtil.isNullOrEmpty(request.getParameter("totalSUM"))) {
                double amount = resultJSON.optDouble("amount");
                paramJobj.put("totalSUM",request.getParameter("totalSUM"));
            }
            if (request.getParameter("customer") != null ){
            paramJobj.put("customer",request.getParameter("customer"));
            }
            

            JSONObject data = accCustomerMainAccountingService.getCustomerExceedingCreditLimit(paramJobj);
            double amountDueOfCustomer = 0.0;
            if(data.has("data") && data.getJSONArray("data").length()>0 && extraCMPPreferences.isIncludeAmountInLimitSO()){
                amountDueOfCustomer = data.getJSONArray("data").getJSONObject(0).optDouble("totalAmountDueOfCustomer");
            }
            
            if ((amountDueOfCustomer > customerCreditLimit)) {
                isLimitExceeding = true;
            }
            
            qApproveMap.put("isLimitExceeding", isLimitExceeding);             //ERM-396
            
              List approvedLevelList = accSalesOrderServiceobj.approveSalesOrder(cqObj, qApproveMap, true);
              int approvedLevel= (Integer) approvedLevelList.get(0);

            if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                String userName = sessionHandlerImpl.getUserFullName(request);
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String creatormail = company.getCreator().getEmailID();
                String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
                String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
                String creatorname = fname + " " + lname;
                String[] emails = {creatormail};
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                if (emails.length > 0) {
                    sendApproveRejctionOfSalesOrderMail(request,preferences,companyid,cqObj,Constants.APPROVAL_EMAIL,qApproveMap);
//                    accountingHandlerDAOobj.sendApprovedEmails(cqObj.getSalesOrderNumber(), userName, emails, sendorInfo, Constants.SALESORDER, creatorname);
                }
            }

            // Save Approval History
            if (approvedLevel != Constants.NoAuthorityToApprove) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("transtype", Constants.SALES_ORDER_APPROVAL);
                hashMap.put("transid", cqObj.getID());
                hashMap.put("approvallevel", cqObj.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
                hashMap.put("remark", remark);
                hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                hashMap.put(Constants.companyKey, companyid);
                accountingHandlerDAOobj.updateApprovalHistory(hashMap);

                // Audit log entry
                auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a Sales Order " + cqObj.getSalesOrderNumber()+ " at Level-" + cqObj.getApprovestatuslevel(), request, cqObj.getID());
                txnManager.commit(status);
                issuccess = true;
                KwlReturnObject kmsg = null;
                String roleName = "Company User";
                kmsg = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
                Iterator ite2 = kmsg.getEntityList().iterator();
                while (ite2.hasNext()) {
                    Object[] row = (Object[]) ite2.next();
                    roleName = row[1].toString();
                }
                msg = messageSource.getMessage("acc.field.SalesOrderhasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request)) + " by " + roleName + " " + sessionHandlerImpl.getUserFullName(request) + " at Level " + cqObj.getApprovestatuslevel() + ".";
            } else {
                txnManager.commit(status);
                issuccess = true;
                msg = messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, RequestContextUtils.getLocale(request)) + cqObj.getApprovestatuslevel() + ".";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public void sendApproveRejctionOfSalesOrderMail(HttpServletRequest request, CompanyAccountPreferences preferences, String companyid, SalesOrder soObj, String fieldid,HashMap<String, Object> soApproveMap) throws ServiceException {
        try {
            String userName = sessionHandlerImpl.getUserFullName(request);
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) returnObject.getEntityList().get(0);
            String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
            String creatormail = company.getCreator().getEmailID();
            String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
            String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
            String creatorname = fname + " " + lname;
            String approvalpendingStatusmsg = "";
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            String documentcreatoremail = (soObj != null && soObj.getCreatedby() != null) ? soObj.getCreatedby().getEmailID() : "";
            int level = soObj.getApprovestatuslevel();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            ArrayList<String> emailArray = new ArrayList<>();
            qdDataMap.put(Constants.companyKey, companyid);
            qdDataMap.put("level", level);
            qdDataMap.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
//            emailArray = commonFnControllerService.getUserApprovalEmail(qdDataMap);
            emailArray.add(creatormail);
            if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
                emailArray.add(documentcreatoremail);
            }
            String[] emails = {};
            emails = emailArray.toArray(emails);
            if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
            }
            if (emails.length > 0) {
                String salesordernumber = soObj.getSalesOrderNumber();
                KwlReturnObject result = accountingHandlerDAOobj.getEmailTemplateTosendApprovalMail(companyid, fieldid, Constants.Acc_Sales_Order_ModuleId);
                NotificationRules dft = (NotificationRules) result.getEntityList().get(0);
                //get email ids of selected users
                String users = dft.getUsers();
                if (dft.isMailToAssignedTo() && !StringUtil.isNullOrEmpty(users) && users.split(",").length > 0) {
                    String usersArr[] = users.split(",");
                    String[] userEmailIds = new String[usersArr.length];
                    for (int j = 0; j < usersArr.length; j++) {
                        User userObj = (User) kwlCommonTablesDAOObj.getClassObject(User.class.getName(), usersArr[j]);
                        if (userObj != null && !StringUtil.isNullOrEmpty(userObj.getEmailID())) {
                            userEmailIds[j] = userObj.getEmailID();
                        }
                    }
                    if (userEmailIds.length > 0) {
                        emails = AccountingManager.getMergedMailIds(emails, userEmailIds);
                    }
                }
                //get email id of document creator
                String[] docCreatorEmailid={};
                if (dft.isMailToCreator() && soObj.getCreatedby() != null) {
                    User userObj = (User) kwlCommonTablesDAOObj.getClassObject(User.class.getName(), soObj.getCreatedby().getUserID() != null ? soObj.getCreatedby().getUserID() : "");
                    if (userObj != null && !StringUtil.isNullOrEmpty(userObj.getEmailID())) {
                        docCreatorEmailid = userObj.getEmailID().split(",");
                    }
                    if (docCreatorEmailid.length > 0) {
                        emails = AccountingManager.getMergedMailIds(emails, docCreatorEmailid);
                    }
                }
                
                //to get email ids entered in send a copy to
                String[] otherEmailIds = {};
                if (!StringUtil.isNullOrEmpty(dft.getEmailids())) {
                    otherEmailIds = dft.getEmailids().split(",");
                    if (otherEmailIds.length > 0) {
                        emails = AccountingManager.getMergedMailIds(emails, otherEmailIds);
                    }
                }
                qdDataMap.put("totalAmount", soObj.getTotalamountinbase());
                qdDataMap.put("ApproveMap", soApproveMap);
                if (soObj.getApprovestatuslevel() < 11) {
//                qdDataMap.put("level", creditNote.getApprovestatuslevel()+1);
                    approvalpendingStatusmsg = commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
                String emailBody = dft.getMailcontent();
                String emailBody1 = "";
                String emailBody2 = "";
                int usernamelength=userName.length();
                int sonumberlength=soObj.getSalesOrderNumber().length();
                String subject = dft.getMailsubject();
                emailBody = emailBody.replaceAll("#UserName#", userName);
                emailBody = emailBody.replaceAll("#ModuleName#", Constants.moduleID_NameMap.get(Constants.Acc_Sales_Order_ModuleId));
                emailBody = emailBody.replaceAll("#SalesOrderNo#", soObj.getSalesOrderNumber());
                emailBody = emailBody.replaceAll("#Approvalstatuslevel#", String.valueOf(level));
                emailBody = emailBody.replaceAll("#baseurl#",baseUrl);
                emailBody =emailBody.replaceAll("#approvalpendingStatusmsg#",approvalpendingStatusmsg);
                subject = subject.replaceAll("#SalesOrderNo#", soObj.getSalesOrderNumber());
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
//                accountingHandlerDAOobj.sendApprovedEmails(soObj.getSalesOrderNumber(), userName, emails, sendorInfo, Constants.SALESORDER, "All", companyid,baseUrl); 
                try {
                    SendMailHandler.postMail(emails, subject, emailBody, emailBody, sendorInfo, smtpConfigMap);
                } catch (Exception ex) {
                    System.out.printf("Error occured while sending Approval/Rejection mail for transaction : " + soObj.getSalesOrderNumber());
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
        }
    }
    
    // From SalesOrder Report when selecting multiple SalesOrder generates multiple PO on the basis of Vendor Mapped Products
    public ModelAndView generatePOFromMultipleSO(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        JSONObject returnJObj = accSalesOrderServiceDAOobj.generatePOFromMultipleSO(paramJobj);
        return new ModelAndView("jsonView", "model", returnJObj.toString());
    }
    
}
