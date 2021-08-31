/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.asset;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author swapnil.khandre
 */
public class AccAssetController extends MultiActionController {

    private AccAssetService accAssetService;

    private exportMPXDAOImpl exportDaoObj;

    public void setAccAssetService(AccAssetService accAssetService) {
        this.accAssetService = accAssetService;
    }

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public ModelAndView getAssetDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = accAssetService.getAssetDetails(request, false);//Normal Case for getassetdetails
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccAssetController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccAssetController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccAssetController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView exportAssetDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        boolean isexport = true;
        try {
            jobj = accAssetService.getAssetDetails(request, isexport);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                exportDaoObj.processRequest(request, response, jobj);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccAssetController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccAssetController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    /**
     * Description: This method is used to get Asset Summary Details
     *
     * @param request
     * @param response
     * @return JSONObject
     */
    public ModelAndView getAssetSummeryReportDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjTemp = new JSONObject();
        JSONObject returnObject = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);

            /*Get common request parameters*/
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            paramJobj.put("df", df);

            /*Get Grid configuration Meta Data and Column/Field Information */
            jobj = accAssetService.getAssetSummeryReportGridInfo(paramJobj);

            boolean isFirstTimeLoad = request.getParameter("isFirstTimeLoad") != null ? Boolean.parseBoolean(request.getParameter("isFirstTimeLoad")) : false;
            /*Get Asset Summery Details*/
            paramJobj.put("isFixedAsset", true);
            paramJobj.put("isFirstTimeLoad", isFirstTimeLoad);
            jobjTemp = accAssetService.getAssetSummeryReportDetails(paramJobj);
            // Add UR method 

            JSONObject dataObj = jobjTemp.getJSONObject("data");
            dataObj.put("columns", jobj.getJSONArray("columns"));
            dataObj.put("success", true);
            dataObj.put("metaData", jobj.getJSONObject("metadata"));
            returnObject.put("data", dataObj);
            returnObject.put("valid", true);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccAssetController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                returnObject.put("success", issuccess);
                returnObject.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccAssetController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, returnObject.toString());
    }

    public ModelAndView exportAssetSummary(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String view = "jsonView_ex";
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            /*Get common request parameters*/
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            paramJobj.put("request", request);
            paramJobj.put("response", response);
            paramJobj.put("isFixedAsset", true);
            paramJobj.put("df", df);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            /*Get Asset Summary Details*/
            accAssetService.exportAssetSummary(paramJobj);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccAssetController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, Constants.model, jobj.toString());
    }
}
