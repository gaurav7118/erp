/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.documentdesigner;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.Constants;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */

public class DocumentDesignController extends MultiActionController {

    private exportMPXDAOImpl exportDaoObj;
    public ImportHandler importHandler;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    DocumentDsignerDAO dao;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private AccDocumentDesignService accDocumentDesignService;

    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setImportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public DocumentDsignerDAO getDao() {
        return dao;
    }

    public void setDao(DocumentDsignerDAO dao) {
        this.dao = dao;
    }

    public void setAccDocumentDesignService(AccDocumentDesignService accDocumentDesignService) {
        this.accDocumentDesignService = accDocumentDesignService;
    }

 public void createNewDocument(HttpServletRequest request,HttpServletResponse response){
     try {
         String result="";
         String moduleid=request.getParameter(Constants.moduleid);
         String templatename=request.getParameter("templatename");
         String templatesubtype=request.getParameter("templatesubtype");
         String moduleName = accDocumentDesignService.getModuleName(Integer.valueOf(moduleid));
 
         String companyid = AccountingManager.getCompanyidFromRequest(request);
         String userid = sessionHandlerImpl.getUserid(request);
         boolean isDuplicate = dao.isDuplicateTemplate(companyid, moduleid, templatename);
         if(!isDuplicate){
             result=dao.createNewDocument(moduleid, templatename, templatesubtype,companyid,userid);
             auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has created Template " + templatename + " for " + moduleName, request, companyid);
             response.getWriter().println(result);
         } else{
             result="{'valid':true,'data':{'msg':'Duplicate Template Name.','success':false},'success':false}";
             response.getWriter().println(result);
         }

     } catch (Exception e) {
         e.printStackTrace();
     }
 }
 public void renameNewDocument(HttpServletRequest request,HttpServletResponse response){
     try {
         String result="";
         String moduleid=request.getParameter(Constants.moduleid);
         String templatename=request.getParameter("templatename");
         String templateid=request.getParameter("templateid");
         String companyid = AccountingManager.getCompanyidFromRequest(request);
         String userid = sessionHandlerImpl.getUserid(request);
         String moduleName = accDocumentDesignService.getModuleName(Integer.valueOf(moduleid));
         
         boolean isDuplicate = dao.isDuplicateTemplate(companyid, moduleid, templatename);
         
         if (!isDuplicate) {
             result = dao.renameNewDocument(moduleid, templatename, templateid, companyid, userid);
             auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has renamed Template " + templatename + " for " + moduleName, request, companyid);
             response.getWriter().println(result);
         } else {
             result = "{'valid':true,'data':{'msg':'Duplicate Template Name.','success':false},'success':false}";
             response.getWriter().println(result);
         }
         } catch (Exception e) {
         e.printStackTrace();
     }
 }

 public void saveDocument(HttpServletRequest request,HttpServletResponse response){
     try {
        
         String moduleid=request.getParameter(Constants.moduleid);
         String templateid=request.getParameter("templateid");
         String json=request.getParameter("json");
//         String isdefault=request.getParameter("isdefault");
         String html=request.getParameter("html");
         String moduleName = accDocumentDesignService.getModuleName(Integer.valueOf(moduleid));
         String headerjson=request.getParameter("headerjson");
         String headerhtml=request.getParameter("headerhtml");
         String footerjson=request.getParameter("footerjson");
         String footerhtml=request.getParameter("footerhtml");
         String pagelayoutproperty=request.getParameter("pagelayoutproperty");  


         String companyid = AccountingManager.getCompanyidFromRequest(request);
         String userid = sessionHandlerImpl.getUserid(request);
         String templatesubtype=request.getParameter("templatesubtype");
         Boolean isPreview = false;
         if(!StringUtil.isNullOrEmpty(request.getParameter("ispreview"))){
             isPreview = Boolean.parseBoolean(request.getParameter("ispreview"));
         }
        String countryid = new String(); 
        String stateid = new String(); 
        KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company companyObj = (Company) companyresult.getEntityList().get(0);
        if(companyObj.getCountry() != null){
            countryid = companyObj.getCountry().getID();
        }
        if(companyObj.getState()!= null){
            stateid = companyObj.getState().getID();
        }
            
         String sqlquery = "";
         int moduleId = Integer.valueOf(moduleid);
         // If module is QA Approval then change moduleid with respective main module
         if(moduleId == Constants.Acc_QA_APPROVAL_MODULE_ID){
             switch(templatesubtype){
                 case "0":
                     moduleId = Constants.Acc_Delivery_Order_ModuleId;
                     break;
             }
         }
         
         if (moduleId == Constants.Acc_Invoice_ModuleId) {
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "invoice",moduleId);
         } else if (moduleId == Constants.Acc_Customer_Quotation_ModuleId) {
             sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "quotation",moduleId);
         } else if (moduleId == Constants.Acc_Vendor_Quotation_ModuleId) {
             sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "vendorquotation",moduleId);
         } else if(moduleId == Constants.Acc_Credit_Note_ModuleId) {
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "creditnote",moduleId);
         } else if (moduleId == Constants.Acc_Delivery_Order_ModuleId) {
             sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "deliveryorder",moduleId);
         } else if (moduleId == Constants.Acc_Sales_Order_ModuleId) {
             sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "salesorder",moduleId);
         } else if (moduleId == Constants.Acc_Purchase_Order_ModuleId) {
             sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "purchaseorder",moduleId);
         } else if (moduleId == Constants.Acc_Vendor_Invoice_ModuleId) {
             sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "goodsreceipt",moduleId);
         } else if (moduleId == Constants.Acc_Sales_Return_ModuleId) {
             sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "salesreturn",moduleId);
         } else if (moduleId == Constants.Acc_Goods_Receipt_ModuleId) {
             sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "grorder",moduleId);
         } else if(moduleId == Constants.Acc_Debit_Note_ModuleId) {
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "debitnote",moduleId);
         }else if(moduleId == Constants.Acc_Receive_Payment_ModuleId) {
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "receipt",moduleId);
         }else if(moduleId == Constants.Acc_Make_Payment_ModuleId) {
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "payment",moduleId);
         }else if(moduleId == Constants.Acc_Purchase_Return_ModuleId) {
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "purchasereturn",moduleId);
         }else if(moduleId == Constants.Acc_Stock_Request_ModuleId || moduleId == Constants.Inventory_ModuleId) {
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "in_goodsrequest",moduleId);
        }else if(moduleId ==Constants.Acc_Stock_Adjustment_ModuleId) {
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "in_stockadjustment",moduleId);
        }else if(moduleId ==Constants.Acc_InterStore_ModuleId||moduleId == Constants.Acc_InterLocation_ModuleId) {
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "in_interstoretransfer",moduleId);
        }else if(moduleId ==Constants.Acc_RFQ_ModuleId) {
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "requestforquotation",moduleId);
        }else if(moduleId ==Constants.Acc_Purchase_Requisition_ModuleId) { //ERP-19851
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "purchaserequisition",moduleId);
        } else if (moduleId == Constants.Build_Assembly_Module_Id) { //ERM-26
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "productbuild", moduleId);
        } else if (moduleId == Constants.MRP_WORK_ORDER_MODULEID) { //ERM-558 Work Order module
            sqlquery = CustomDesignHandler.buildSqlQueryNew(json, "workorder", moduleId);
        }
        if (!dao.isDefaultTemplate(templateid)) {
            String result = dao.saveDocument(moduleid, templateid, json, html, companyid, userid, headerjson, headerhtml, footerjson, footerhtml, templatesubtype, sqlquery, pagelayoutproperty,isPreview,countryid,stateid);
            String templatename=dao.getTemplateName(templateid);
            auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has edited Template " + templatename + " for " + moduleName, request, companyid);
            response.getWriter().print("{'success':true,'msg':'"+ messageSource.getMessage("acc.field.Templatesavedsuccessfully", null, RequestContextUtils.getLocale(request)) +"'}");
        } else {
            response.getWriter().print("{'success':false,'msg':'You cannot change default template.'}");
        }

     } catch (Exception e) {
         e.printStackTrace();
     }
 }


 public void previewDocument(HttpServletRequest request,HttpServletResponse response){

     try {
         String templateid=request.getParameter("templateid");
         String companyid=request.getParameter(Constants.companyKey);
         String result=dao.loadDocument(templateid,companyid);
         response.getWriter().println(result);
     } catch (Exception e) {
     }
 }

 public void loadDocument(HttpServletRequest request,HttpServletResponse response){

     try {
         String templateid=request.getParameter("templateid");
         String companyid=request.getParameter(Constants.companyKey);
         String result=dao.loadDocument(templateid,companyid);
         response.getWriter().println(result);
     } catch (Exception e) {
     }
 }


 public void saveGlobalHeaderFooter(HttpServletRequest request,HttpServletResponse response){
     try {

         String companyid = AccountingManager.getCompanyidFromRequest(request);
         String json=request.getParameter("json");
         String html=request.getParameter("html");
         String isheader=request.getParameter("isheader");
         String result=dao.saveGlobalHeaderFooter(companyid,json,html,isheader);
         response.getWriter().println(result);

     } catch (Exception e) {
         e.printStackTrace();
     }
 }


 public void importGlobalHeader(HttpServletRequest request,HttpServletResponse response){

     try {
         String companyid = AccountingManager.getCompanyidFromRequest(request);
         String result=dao.importGlobalHeader(companyid);
         response.getWriter().println(result);
     } catch (Exception e) {
     }
 }



 public void importGlobalFooter(HttpServletRequest request,HttpServletResponse response){

     try {
         String companyid = AccountingManager.getCompanyidFromRequest(request);
         String result=dao.importGlobalFooter(companyid);
         response.getWriter().println(result);
     } catch (Exception e) {
     }
 }
 public void importGlobalHeaderFooter(HttpServletRequest request,HttpServletResponse response){

     try {
         String companyid = AccountingManager.getCompanyidFromRequest(request);
         int header = 1;
         if ( !StringUtil.isNullOrEmpty(request.getParameter("header")) ) {
             header = Integer.parseInt(request.getParameter("header"));
         }
         String result=dao.importGlobalHeaderFooter(companyid, header);
         response.getWriter().println(result);
     } catch (Exception e) {
     }
 }
 
 public ModelAndView exportTemplates(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            boolean isExportMultiple = false;
            Map<String,String> requestParams = new HashMap<>();
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            if ( !StringUtil.isNullOrEmpty(request.getParameter("isExportMultiple"))) {
                isExportMultiple = Boolean.parseBoolean(request.getParameter("isExportMultiple").toString());
            }
            if ( isExportMultiple ) {
                JSONArray jarr = new JSONArray();
                JSONObject retJobj = new  JSONObject();
                JSONArray selectedTemplates = new JSONArray(request.getParameter("selectedTemplates").toString());
                for (int index = 0; index < selectedTemplates.length(); index++) {
                    String moduleid = selectedTemplates.getJSONObject(index).optString(Constants.moduleid,"");
                    String templateid = selectedTemplates.getJSONObject(index).optString("templateid","");
                    requestParams.put(Constants.companyKey, companyid);
                    requestParams.put(Constants.moduleid,moduleid);
                    requestParams.put("templateid", templateid);
                    retJobj = accDocumentDesignService.getCustomTemplatesJsonForExport(requestParams);
                    jarr.put(retJobj.getJSONArray(Constants.RES_data).getJSONObject(0));
                }
                jobj.put(Constants.RES_data,jarr);
            } else {
                String moduleid = request.getParameter(Constants.moduleid);
                String templateid = request.getParameter("templateid");
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put(Constants.moduleid,moduleid);
                requestParams.put("templateid", templateid);
                jobj = accDocumentDesignService.getCustomTemplatesJsonForExport(requestParams);
            }
            jobj.put("isFromDocumentDesigner", true);
            exportDaoObj.processRequest(request, response, jobj);
            if (isExportMultiple) {
                JSONArray selectedTemplates = new JSONArray(request.getParameter("selectedTemplates").toString());
                for (int index = 0; index < selectedTemplates.length(); index++) {
                    String moduleid = selectedTemplates.getJSONObject(index).optString(Constants.moduleid, "");
                    String templateid = selectedTemplates.getJSONObject(index).optString("templateid", "");
                    String moduleName = accDocumentDesignService.getModuleName(Integer.valueOf(moduleid));
                    String templatename = dao.getTemplateName(templateid);
                    auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has exported Template " + templatename + " for " + moduleName, request, companyid);
                }
            } else {
                String moduleid = request.getParameter(Constants.moduleid);
                String templateid = request.getParameter("templateid");
                String moduleName = accDocumentDesignService.getModuleName(Integer.valueOf(moduleid));
                String templatename = dao.getTemplateName(templateid);
                auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has exported Template " + templatename + " for " + moduleName, request, companyid);
            }
            jobj.put(Constants.RES_success, true);
        } catch (Exception ex) {
            Logger.getLogger(DocumentDesignController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView importTemplates(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans/";
            String doAction = request.getParameter("method");
            System.out.println("A(( " + doAction + " start : " + new Date());
            JSONObject datajobj = new JSONObject();
            String filePath = "";
            if (doAction.compareToIgnoreCase("getMapXLS") == 0) {
                JSONObject customjob = new JSONObject(request.getParameter("customFieldJson")); //fetching selected Custom Fields Present in the Template.
                JSONArray jarr = customjob.optJSONArray(Constants.RES_data);
                filePath = request.getParameter("filePath");
                datajobj = importHandler.parseXLSX(filePath,0);
                if (jarr.length() > 0) {      //  If custom fields are present then create those custom fields else continue.
//                  datajobj = accDocumentDesignService.createCustomFields(request,datajobj);
                  datajobj = accDocumentDesignService.createCustomFields(paramJobj,datajobj);
                }
//                datajobj = accDocumentDesignService.modifyHtmlJsonofTemplate(request,datajobj);
                datajobj = accDocumentDesignService.modifyHtmlJsonofTemplate(paramJobj,datajobj);
//                jobj = accDocumentDesignService.ImportCustomTemplates(request, datajobj);
                jobj = accDocumentDesignService.ImportCustomTemplates(paramJobj, datajobj);
                issuccess = true;
            } else if (doAction.compareToIgnoreCase("upload")==0) {
                String fileid = UUID.randomUUID().toString();
                fileid = fileid.replaceAll("-", ""); 
                filePath = importHandler.uploadDocument(request, fileid);
                filePath = destinationDirectory + filePath;
                datajobj = importHandler.parseXLSX(filePath,0);
                JSONArray jSONArray = datajobj.getJSONArray("Header");
//                accDocumentDesignService.ValidateHeadersCustomTemplates(request,jSONArray);
                accDocumentDesignService.ValidateHeadersCustomTemplates(paramJobj,jSONArray);
                JSONArray dataArr = datajobj.getJSONArray(Constants.RES_data);
                String custJsonStr = dataArr.getJSONObject(1).optString("M","");
                jobj.put("customFieldJsonStr", custJsonStr);
                jobj.put("filePath", filePath);
                jobj.put(Constants.RES_success, true);
            }
        } 
        catch (JSONException ex) {
            issuccess = false;
            msg = "Invalid file. Please provide valid xlsx file and try again.";
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException e) {
                Logger.getLogger(DocumentDesignController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
            Logger.getLogger(DocumentDesignController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException e) {
                Logger.getLogger(DocumentDesignController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(DocumentDesignController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
}
