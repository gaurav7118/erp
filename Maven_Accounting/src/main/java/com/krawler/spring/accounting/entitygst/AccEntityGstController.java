/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.entitygst;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.accounting.gst.services.GSTRConstants;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.gst.AccGstController;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Suhas.Chaware
 */
public class AccEntityGstController extends MultiActionController implements MessageSourceAware {

    private MessageSource messageSource;
    private AccEntityGstService accEntityGstService;
    private ImportHandler importHandler;
    private exportMPXDAOImpl exportDaoObj;;

    public void setAccEntityGstService(AccEntityGstService accEntityGstService) {
        this.accEntityGstService = accEntityGstService;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    /*
     * Method for Get GST Rule Setup.
     */

    public ModelAndView getGSTRuleSetup(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        try {
            JSONObject requestParams = new JSONObject();
            requestParams = StringUtil.convertRequestToJsonObject(request);
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            jobj = accEntityGstService.getGSTRuleSetup(requestParams);
            isSuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put(Constants.RES_success, isSuccess);
            jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
        }
        //String aa = " {'valid':true,'totalCount':5,'coldata':[{'State':'MAH','City':'Mumbai'},{'State':'MAH','City':'Pune'},{'State':'KAR','City':'Banglore'}],'columns':[{'width':150,'align':'center','pdfwidth':150,'dataIndex':'State','header':'State'},{'width':150,'align':'center','pdfwidth':150,'dataIndex':'City','header':'City'}],'msg':'','success':true,'metaData':{'totalProperty':'totalCount','root':'coldata','fields':[{'name':'State'},{'name':'City'}]}},'success':true";
        //jobj = new JSONObject(aa);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * @desc This method give Grid Data for GST Rules
     * @param request
     * @param response
     * @return 
     * @throws JSONException 
     */
    public ModelAndView getGSTRuleReport(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        try {
            JSONObject requestParams = new JSONObject();
            requestParams = StringUtil.convertRequestToJsonObject(request);
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            jobj = accEntityGstService.getGSTRuleReport(requestParams);
            isSuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put(Constants.RES_success, isSuccess);
            jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getFieldComboDataForModule(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accEntityGstService.getFieldComboDataForModule(paramJobj);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * ERP-32829
     *
     * @param request
     * @param response
     * @Desc : save GST rule setup
     * @return
     */
    public ModelAndView saveGSTRuleSetup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            jobj = accEntityGstService.saveGSTRuleSetup(params);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * ERP-34607
     * @param request
     * @param response
     * @Desc : Delete GST Rule Report Item.
     * @return 
     */
    
    public ModelAndView deleteGSTRuleReportItem(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put("locale", RequestContextUtils.getLocale(request));
            msg = accEntityGstService.deleteGSTRuleReportItem(params);
            issuccess = true;
        }catch(Exception ex){
            msg = " " + ex.getMessage();
            if( ex.getMessage() == null){
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            }catch(JSONException ex){
                Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * ERP-32829
     *
     * @param request
     * @param response
     * @Desc : Import Function for GST rules
     * @return
     */
    public ModelAndView importInputGSTRuleSetup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            params.put("companyid", companyid);
            params.put("user", sessionHandlerImpl.getUserFullName(request));
            params.put("userid", sessionHandlerImpl.getUserid(request));
            String doAction = params.getString("do");
            params.optString("extraParams");
            if (doAction.compareToIgnoreCase("import") == 0) {
                params.put("isInput", false);
                /**
                 * Create JSon using File
                 */
                jobj = accEntityGstService.createJsonObjectToSaveGSTRules(params);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                String eParams = params.getString("extraParams");
                params.put("servletContext", this.getServletContext());
                JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);

                HashMap<String, Object> requestParams = importHandler.getImportRequestParams(params);
                requestParams.put("extraParams", extraParams);
                requestParams.put("extraObj", null);
                requestParams.put("servletContext", params.get("servletContext"));
                requestParams.put("locale", RequestContextUtils.getLocale(request));
                /**
                 * Added required params for validateFileData
                 */
                boolean salesOrPurchase = params.optBoolean("isSales");
                requestParams.put("salesOrPurchase", salesOrPurchase ? 1 : 0);
                requestParams.put("countryid", sessionHandlerImpl.getCountryId(request));
                
                jobj = importHandler.validateFileData(requestParams);
                jobj.put(Constants.RES_success, true);
            }
            issuccess = true;

        } catch (Exception ex) {

            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", jobj.get("msg"));
            } catch (JSONException ex) {
                Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView importOutputGSTRuleSetup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            params.put("user", sessionHandlerImpl.getUserFullName(request));
            params.put("userid", sessionHandlerImpl.getUserid(request));
            params.put("companyid", companyid);
            String doAction = params.getString("do");
            params.optString("extraParams");
            if (doAction.compareToIgnoreCase("import") == 0) {
                params.put("isInput", true);
                /**
                 * Create JSON using file
                 */
                jobj = accEntityGstService.createJsonObjectToSaveGSTRules(params);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                String eParams = params.getString("extraParams");
                params.put("servletContext", this.getServletContext());
                JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);

                HashMap<String, Object> requestParams = importHandler.getImportRequestParams(params);
                requestParams.put("extraParams", extraParams);
                requestParams.put("extraObj", null);
                requestParams.put("servletContext", params.get("servletContext"));
                requestParams.put("locale", RequestContextUtils.getLocale(request));
                /**
                 * Added required params for validateFileData
                 */
                boolean salesOrPurchase = params.optBoolean("isSales");
                requestParams.put("salesOrPurchase", salesOrPurchase ? 1 : 0);
                requestParams.put("countryid", sessionHandlerImpl.getCountryId(request));

                jobj = importHandler.validateFileData(requestParams);
                jobj.put(Constants.RES_success, true);
            }
            issuccess = true;

        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * ERP-33784
     * @param request
     * @param response
     * @Desc Get GST Sales tax Liability report Details
     * @return 
     */
    public ModelAndView getGSTSalesTaxLiabilityReportDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject data = new JSONObject();
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            paramJobj.put(Constants.df, df);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            /**
             * Function to get GST Sales tax report data from services
             */
            
            data = accEntityGstService.getGSTSalesTaxLiabilityReportDetails(paramJobj);
            
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return new ModelAndView(Constants.jsonView, "model", data.toString());
    }
    /**
     * ERP-33784
     * @param request
     * @param response
     * @Desc Function to get GST Sales tax report data for Export 
     * @return 
     */
    public ModelAndView exportGSTSalesTaxLiabilityReportDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            boolean export = true;
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            /**
             * Function to get GST Sales tax report data for Export 
             */
            DateFormat df = authHandler.getDateOnlyFormat();
            paramJobj.put(Constants.df, df);
            jobj = accEntityGstService.getGSTSalesTaxLiabilityReportDetails(paramJobj);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    /**
     * ERP-32829
     *
     * @param request
     * @param response
     * @Desc : Function to get GST for particular product
     * @return
     */
    public ModelAndView getGSTForProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        JSONObject data = new JSONObject();
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            /**
             * Function to get GST data from services
             */
            data = accEntityGstService.getGSTForProduct(paramJobj, requestParams);
            if (jobj.has(Constants.RES_success)) {
                issuccess = jobj.optBoolean(Constants.RES_success, false);
            }
            if (jobj.has(Constants.RES_msg)) {
                msg = jobj.optString(Constants.RES_msg, "");
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("data", data);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getGSTForAdvance(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        JSONObject data = new JSONObject();
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            /**
             * Function to get GST data from services
             */
            data = accEntityGstService.getGSTForAdvance(paramJobj, requestParams);
            if (jobj.has(Constants.RES_success)) {
                issuccess = jobj.optBoolean(Constants.RES_success, false);
            }
            if (jobj.has(Constants.RES_msg)) {
                msg = jobj.optString(Constants.RES_msg, "");
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("data", data);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getGSTR1Summary(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            jobj = accEntityGstService.getGSTR1Summary(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
     public ModelAndView getGSTR1SummaryDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            jobj = accEntityGstService.getGSTR1SummaryDetails(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
     /**
      * Function for GSTR2
      * @param request
      * @param response
      * @return 
      */
    public ModelAndView getGSTR2Summary(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            jobj = accEntityGstService.getGSTR2Summary(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView getGSTR2SummaryDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            if (params.optBoolean("isGSTR2AMatchAndReconcile", false)) {
                int month = params.optInt("month", 0);
                int year = params.optInt("year", 0);
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.DATE, 1);
                params.put("startdate", authHandler.getGlobalDateFormat().format(cal.getTime()));
                cal.set(Calendar.DATE, cal.getMaximum(Calendar.DATE));
                params.put("enddate", authHandler.getGlobalDateFormat().format(cal.getTime()));
            }
            jobj = accEntityGstService.getGSTR2SummaryDetails(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
     public ModelAndView getGSTRMatchAndReconcile(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            jobj = accEntityGstService.getGSTRMatchAndReconcile(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    
    public ModelAndView ExportGSTR1Summary(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            String fileType = request.getParameter("filetype");
            params.put("isExport", true);
            HSSFWorkbook wb = null;
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            wb = accEntityGstService.exportGSTR1Summary(params);
            exportDaoObj.writeXLSDataToFile("GSTR1", fileType, wb, response);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
       public ModelAndView ExportGSTR2Summary(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            String fileType = request.getParameter("filetype");
            params.put("isExport", true);
            HSSFWorkbook wb = null;
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            wb = accEntityGstService.exportGSTR2Summary(params);
            exportDaoObj.writeXLSDataToFile("GSTR2", fileType, wb, response);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
}
     public ModelAndView ExportGSTR1Efiling(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            String fileType = request.getParameter("filetype");
            params.put("isExport", true);
            HSSFWorkbook wb = null;
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            wb = accEntityGstService.exportGSTR1Efiling(params);
            exportDaoObj.writeXLSDataToFile("GSTR1-e-Filing", fileType, wb, response);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    public ModelAndView getGSTRComputationReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            if (!StringUtil.isNullOrEmpty(params.optString("startdate", "")) && !StringUtil.isNullOrEmpty(params.optString("enddate", ""))) {
                jobj = accEntityGstService.getGSTComputationSummaryReport(params);
            }
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    /**
     * Get GST Computation section name for Detail view 
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getGSTRComputationReportSectionNames(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            jobj = accEntityGstService.getGSTComputationReportSectionCombo(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    /**
     * Get GSTR3B section name for Detail view 
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getGSTR3BSummaryReportSectionNames(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            jobj = accEntityGstService.getGSTR3BReportSectionCombo(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    /**
     * Function to fetch GST computation report
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getGSTComputationDetailReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            if (!StringUtil.isNullOrEmpty(params.optString("startdate", "")) && !StringUtil.isNullOrEmpty(params.optString("enddate", ""))) {
                jobj = accEntityGstService.getGSTComputationDetailReport(params);
            }
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView getGSTR3BSummaryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            if (!StringUtil.isNullOrEmpty(params.optString("startdate", "")) && !StringUtil.isNullOrEmpty(params.optString("enddate", ""))) {
                params.put("reportid", Constants.GSTR3B_Summary_Report);
                jobj = accEntityGstService.getGSTR3BSummaryReport(params);
            }
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView getGSTR3BSummaryDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            if (!StringUtil.isNullOrEmpty(params.optString("startdate", "")) && !StringUtil.isNullOrEmpty(params.optString("enddate", ""))) {
                jobj = accEntityGstService.getGSTR3BSummaryDetails(params);
            }
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView exportGSTR3BSummaryDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            params.remove("start");
            params.remove("limit");
            if (!StringUtil.isNullOrEmpty(params.optString("startdate", "")) && !StringUtil.isNullOrEmpty(params.optString("enddate", ""))) {
                if (!params.optBoolean("isFromSummaryView", false)) {
                    jobj = accEntityGstService.getGSTR3BSummaryDetails(params);
                    jobj.put(Constants.entity, params.optString(Constants.entity, ""));
                    exportDaoObj.processRequest(request, response, jobj);
                } else {
                    params.put("transactionType", GSTRConstants.GSTR3B_TRANSACTION_TYPE_ALL);
                    params.put(GSTR3BConstants.DETAILED_VIEW_REPORT, true);
                    HSSFWorkbook wb = accEntityGstService.exportGSTR3BDetails(params);
                    exportDaoObj.writeXLSDataToFile("GSTR3B-Detailed Report", "xls", wb, response);
                }
            }
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView exportGSTR3BReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            params.remove("start");
            params.remove("limit");
            if (!StringUtil.isNullOrEmpty(params.optString("startdate", "")) && !StringUtil.isNullOrEmpty(params.optString("enddate", ""))) {
                params.put("reportid", Constants.GSTR3B_Summary_Report);
                jobj = accEntityGstService.getGSTR3BSummaryReport(params);
                jobj.put(Constants.entity, params.optString(Constants.entity, ""));
                exportDaoObj.processRequest(request, response, jobj);
            }
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView ExportGSTRComputationReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            String fileType = request.getParameter("filetype");
            params.put("isExport", true);
            HSSFWorkbook wb = null;
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            String filename=params.optString("filename","GST Computation Report");
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            /**
             * Below if condition to export GST computation individual Section report data.
             */
            if (params.optBoolean("isDetailSectionExport", false)) {
                if (!StringUtil.isNullOrEmpty(params.optString("startdate", "")) && !StringUtil.isNullOrEmpty(params.optString("enddate", ""))) {
                    jobj = accEntityGstService.getGSTComputationDetailReport(params);
                    jobj.put(Constants.entity, params.optString(Constants.entity, ""));
                    exportDaoObj.processRequest(request, response, jobj);
                }
            }else{//Export All detail section data or Summary data 
                if (!StringUtil.isNullOrEmpty(params.optString("startdate", "")) && !StringUtil.isNullOrEmpty(params.optString("enddate", ""))) {

                    if (params.has("isdetailreport") && params.optBoolean("isdetailreport")) {
                        wb = accEntityGstService.exportGSTComputationDetails(params);
                    } else {
                        wb = accEntityGstService.exportGSTRComputationReport(params);
                    }
                }
                exportDaoObj.writeXLSDataToFile(filename, fileType, wb, response);
            }
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Function for GST MisMatch report summary
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getGSTRMisMatchSummary(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            jobj = accEntityGstService.getGSTRMisMatchSummary(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    /**
     * Function for GST MisMatch report summary details
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getGSTRMisMatchSummaryDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            jobj = accEntityGstService.getGSTRMisMatchSummaryDetails(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    /**
     * Function for GST MisMatch report summary export
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView ExportGSTRMisMatchSummary(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            String fileType = request.getParameter("filetype");
            params.put("isExport", true);
            HSSFWorkbook wb = null;
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("userdf", authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            wb = accEntityGstService.exportGSTRMisMatchSummary(params);
            exportDaoObj.writeXLSDataToFile("GSTR MisMatch", fileType, wb, response);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView updateGSTR2JSON(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            DiskFileUpload fu = new DiskFileUpload();
            java.util.List fileItems = null;
            FileItem fi = null;
            String fileName = "";
            try {
                fileItems = fu.parseRequest(request);
            } catch (FileUploadException e) {
                throw ServiceException.FAILURE("ProfileHandler.updateProfile", e);
            }
            Map arrParam = new java.util.HashMap();
            for (java.util.Iterator k = fileItems.iterator(); k.hasNext();) {
                fi = (FileItem) k.next();
                arrParam.put(fi.getFieldName(), fi.getString());
                if (!fi.isFormField()) {
                    if (fi.getSize() != 0) {
                        fileName = new String(fi.getName().getBytes());
                    } else {
                        throw new AccountingException("File not uploaded! File should not be empty.");    //When file is empty
                    }
                }
            }
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put("fileItems", fileItems);
            params.put("fileName", fileName);
            params.put("arrParam", arrParam);
            params.put("diskFileUpload", fu);
            accEntityGstService.uploadGSTR2JSONData(params, fileItems);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getGSTR2AImportData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            params.put("isCallFromGSTR2A", true);
            jobj = accEntityGstService.getGSTR2AImportData(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView getGSTR2AComparisonData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            params.put("isGSTR2AComparisonWindow", true);
            int month = params.optInt("month", 0);
            int year = params.optInt("year", 0);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DATE, 1);
            params.put("startdate", authHandler.getGlobalDateFormat().format(cal.getTime()));
            cal.set(Calendar.DATE, cal.getMaximum(Calendar.DATE));
            params.put("enddate", authHandler.getGlobalDateFormat().format(cal.getTime()));
            jobj = accEntityGstService.getGSTR2AComparisonData(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView exportGSTR2AComparisonData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat(request));
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            params.put("isGSTR2AComparisonWindow", true);
            int month = params.optInt("month", 0);
            int year = params.optInt("year", 0);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DATE, 1);
            params.put("startdate", authHandler.getGlobalDateFormat().format(cal.getTime()));
            cal.set(Calendar.DATE, cal.getMaximum(Calendar.DATE));
            params.put("enddate", authHandler.getGlobalDateFormat().format(cal.getTime()));
            params.remove("start");
            params.remove("limit");
            jobj = accEntityGstService.getGSTR2AComparisonData(params);
            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * 
     * @param request (data: JSONArray containing GST Rule Information)
     * @param response
     * @return 
     * @desc Adds and Edits GST Rule.
     */
    public ModelAndView addGSTRule(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.locale, RequestContextUtils.getLocale(request));
            params.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            params.put(Constants.data, new JSONArray(params.optString(Constants.RES_data)));
            /**
             * isAddOrEdit flag used to differ import case from add edit case to
             * enter detailed Audit Trail Entry.
             */
            params.put(Constants.isAddOrEdit, true);
            jobj = accEntityGstService.saveGSTRuleSetup(params);
            /**
             * Overwriting msg and success variable required for managing 
             * exception case as well as normal case.
             */
            msg = jobj.optString(Constants.RES_msg);
            success = jobj.optBoolean(Constants.RES_success);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_success, success);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * 
     * @param request (State : StateID from FieldComboData, Entity : EntityID
     * from FieldComboData)
     * @param response
     * @return (success1 : true (for CGST,SGST), false for IGST.)
     * @desc get State mapped with entity to check for user to show either :
     * CGST and SGST Input box Or : IGST Input box.
     */
    public ModelAndView isStateMappedwithEntity(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.locale, RequestContextUtils.getLocale(request));
            params.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            jobj = accEntityGstService.isStateMappedwithEntity(params);
            success = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_success, success);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * ERP-33630
     * @desc : delete LineLevelTerm
     * @param request (termId: LineLevelTerm's ID, termName : String (Name of Term))
     * @param response 
     * @return msg (for Delete Operation)
     */    
    public ModelAndView deleteLineLevelTerm(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        try {
            JSONObject requestParams = new JSONObject();
            requestParams = StringUtil.convertRequestToJsonObject(request);
            requestParams.put(Constants.locale, RequestContextUtils.getLocale(request));
            msg = accEntityGstService.deleteLineLevelTerm(requestParams);
            if (msg.equals(messageSource.getMessage("acc.gstterm.failedDelete", null, RequestContextUtils.getLocale(request)))) {
                isSuccess = false;
            } else {
                isSuccess = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateGSTR2TransactionFlag(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            jobj = accEntityGstService.updateGSTR2TransactionFlag(params);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView exportGSTR2Json(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            String fileType = "json";
            params.put(Constants.df, authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            JSONObject nObject = accEntityGstService.exportJsonForGSTR2(params);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            os.write(nObject.toString().getBytes());
            exportDaoObj.writeDataToFile("GSTR2 Offline file", fileType, os, response);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Get INDIA GST CESS Calculation Type 
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getCESSCalculationType(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.df, authHandler.getDateOnlyFormat());
            params.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            jobj = accEntityGstService.getCESSCalculationType(params);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Function to check whether history present for particular customer/vendor
     * after date.
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getGSTFieldsChangedStatus(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            jobj = accEntityGstService.getGSTFieldsChangedStatus(params);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Import E-way fields controller 
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView importEwayFieldsData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            params.put("companyid", companyid);
            params.put("user", sessionHandlerImpl.getUserFullName(request));
            params.put("userid", sessionHandlerImpl.getUserid(request));
            params.put(Constants.locale, RequestContextUtils.getLocale(request));
            String doAction = params.getString("do");
            if (doAction.compareToIgnoreCase("import") == 0) {
                jobj = accEntityGstService.importEwayFieldsData(params);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                params.put("servletContext", this.getServletContext());
                jobj = accEntityGstService.validateEwayFieldsData(params);
                jobj.put(Constants.RES_success, true);
            }//
            msg = jobj.optString("msg", "");
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            issuccess =false;
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
 
