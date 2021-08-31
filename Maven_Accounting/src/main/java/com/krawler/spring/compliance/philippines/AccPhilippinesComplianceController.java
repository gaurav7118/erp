/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.compliance.philippines;

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class AccPhilippinesComplianceController extends MultiActionController implements MessageSourceAware {

    private AccPhilippinesComplianceService accPhilippinesComplianceService;
    private MessageSource messageSource;
    private exportMPXDAOImpl exportDaoObj;

    public void setAccPhilippinesComplianceService(AccPhilippinesComplianceService accPhilippinesComplianceService) {
        this.accPhilippinesComplianceService = accPhilippinesComplianceService;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    /**
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getVATSummaryReportData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
            jobj = accPhilippinesComplianceService.getVATSummaryReportData(paramJobj);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccPhilippinesComplianceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
      /**
     * 
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getVATDetailReportData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
            jobj = accPhilippinesComplianceService.getVATDetailReportData(paramJobj);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccPhilippinesComplianceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * 
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getVATReportSectionData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
            jobj = accPhilippinesComplianceService.getVATReportSectionData(paramJobj);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccPhilippinesComplianceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     *
     * @param request
     * @param response
     * @return
     * @throws JSONException
     */
    public ModelAndView getPurchaseReliefReport(HttpServletRequest request, HttpServletResponse response){
        JSONObject returnObj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        try {
            JSONObject requestParams = StringUtil.convertRequestToJsonObject(request);
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            returnObj = accPhilippinesComplianceService.getPurchaseReliefReportSummary(requestParams);
            isSuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                returnObj.put(Constants.RES_success, isSuccess);
                returnObj.put(Constants.RES_msg, msg != null ? msg : "null");
            } catch (Exception ex) {
                Logger.getLogger(AccPhilippinesComplianceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", returnObj.toString());
    }
    /**
     * Export of purchases Relief Report
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView exportPurchaseReliefReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            boolean exportData = true;
            JSONObject requestParams = StringUtil.convertRequestToJsonObject(request);
            requestParams.put("exportData", exportData);
            returnObj = accPhilippinesComplianceService.getPurchaseReliefReportSummary(requestParams);
            exportDaoObj.processRequest(request, response, returnObj);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                returnObj.put(Constants.RES_msg, msg);
                returnObj.put(Constants.RES_success, success);
            } catch (Exception ex) {
                Logger.getLogger(AccPhilippinesComplianceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", request);
    }
           
    public ModelAndView getSalesReliefReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject returnObj = new JSONObject();
        String msg="";
        boolean isSuccess = false;
        try {
            JSONObject requestParams = StringUtil.convertRequestToJsonObject(request);
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
            returnObj = accPhilippinesComplianceService.getSalesReliefSummaryReport(requestParams);
            isSuccess = true;
        } catch (Exception ex) {
            ex.getMessage();
        }finally {
            try {
                returnObj.put(Constants.RES_success, isSuccess);
                returnObj.put(Constants.RES_msg, msg != null ? msg : "null");
            } catch (Exception ex) {
                Logger.getLogger(AccPhilippinesComplianceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", returnObj.toString());
    }
}
