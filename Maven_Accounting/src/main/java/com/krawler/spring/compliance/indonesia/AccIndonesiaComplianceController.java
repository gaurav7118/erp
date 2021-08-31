/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 */
package com.krawler.spring.compliance.indonesia;

import com.krawler.common.util.Constants;
import com.krawler.common.util.IndonesiaConstants;
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
 * @author Rahul A. Bhawar - Indonesia Compliance
 */
public class AccIndonesiaComplianceController extends MultiActionController implements MessageSourceAware {

    private AccIndonesiaComplianceService accIndonesiaComplianceService;
    private exportMPXDAOImpl exportDaoObj;
    ;
    private MessageSource messageSource;

    public void setAccIndonesiaComplianceService(AccIndonesiaComplianceService accIndonesiaComplianceService) {
        this.accIndonesiaComplianceService = accIndonesiaComplianceService;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    /**
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getVATOutReportData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
            jobj = accIndonesiaComplianceService.getVATOutReportData(paramJobj);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccIndonesiaComplianceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Export VAT Out Report data in Excel file
     * @param request
     * @param response
     * @return
     */
    public ModelAndView exportVATOutReportData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
            paramJobj.put(IndonesiaConstants.isExportData, true);
            jobj = accIndonesiaComplianceService.getVATOutReportData(paramJobj);
            jobj.put(IndonesiaConstants.isCreateHeaderAndFilterRow, true);
            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccIndonesiaComplianceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
