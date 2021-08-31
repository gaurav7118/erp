/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.companypreferenceservice.CompanyReportConfigurationService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.QueryException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class CommonExportController extends MultiActionController {

    private CommonExportService commonExportService;
    private CompanyReportConfigurationService companyReportConfigurationService;
    
    public void setCompanyReportConfigurationService(CompanyReportConfigurationService companyReportConfigurationService) {
        this.companyReportConfigurationService = companyReportConfigurationService;
    }

    public void setCommonExportService(CommonExportService commonExportService) {
        this.commonExportService = commonExportService;
    }

    public ModelAndView exportFile(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            Map reqMap = new HashMap();
            reqMap.put("request", request);
            reqMap = extraProcesses(reqMap);
            request = (HttpServletRequest) reqMap.get("request");
            JSONObject jsonObject = StringUtil.convertRequestToJsonObject(request);
            if (jsonObject.optString("filetype").equalsIgnoreCase("csv") || jsonObject.optString("filetype").equalsIgnoreCase("detailedCSV")) {
                jsonObject.put("userDateFormatId", sessionHandlerImpl.getDateFormatID(request));
                jsonObject.put("timeZoneDifferenceId", sessionHandlerImpl.getTimeZoneDifference(request));
                jsonObject.put("currencyIDForProduct", sessionHandlerImpl.getCurrencyID(request));
            }
            String exportid = commonExportService.addOrRemoveExportLog(jsonObject);
            jsonObject.put("exportid", exportid);
            commonExportService.exportFileService(jsonObject);
        } catch (JSONException | SessionExpiredException | ServiceException ex) {
            Logger.getLogger(CommonExportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public Map extraProcesses(Map params) throws JSONException, ServiceException, SessionExpiredException {
        HttpServletRequest request = (HttpServletRequest) params.get("request");
        String module = "";
        module = request.getParameter("module");
        switch (module) {
            case "GeneralLedger":
                Map<String, String> propertiesMap = companyReportConfigurationService.getPropertiesForExport(sessionHandlerImpl.getCompanyid(request), Constants.COMPANY_REPORT_CONFIG_GL, Constants.globalFields, true, Constants.isexportledgerflag, Constants.FROM_EXPANDER);
                request.setAttribute("title", propertiesMap.get("title"));
                request.setAttribute("header", propertiesMap.get("header"));
                request.setAttribute("align", propertiesMap.get("align"));
                break;
        }
        params.clear();
        params.put("request", request);
        return params;

    }

    public ModelAndView getExportLog(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            Map requestParams = new HashMap();
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String user = sessionHandlerImpl.getUserid(request);
            DateFormat udf = authHandler.getUserDateFormatterWithoutTimeZone(request);
            Integer statusFilter = request.getParameter("statusFilter") != null ? Integer.parseInt(request.getParameter("statusFilter")) : 0;
            DateFormat dff = authHandler.getGlobalDateFormat();
            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate"))) {
                Date startdate = dff.parse(request.getParameter("startdate"));
                requestParams.put("startdate", startdate);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                Date enddate = dff.parse(request.getParameter("enddate"));
                requestParams.put("enddate", enddate);
            }
            requestParams.put("companyId", companyId);
            requestParams.put("user", user);
            requestParams.put("statusFilter", statusFilter);
            requestParams.put("udf", udf);
            JSONObject resultJSON = commonExportService.getExportLog(requestParams);
            jobj.put("data", resultJSON);
            jobj.put("valid", true);
            jobj.put("success", true);
        } catch (ParseException | ServiceException | SessionExpiredException | QueryException | JSONException ex) {
            try {
                jobj.put("success", false);
                return new ModelAndView("jsonView_ex", "model", jobj.toString());
            } catch (Exception ex1) {
                Logger.getLogger(CommonExportController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    
    public void downloadExportedFileData(HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = request.getParameter("filename");
            String storagename = request.getParameter("storagename");
            String filetype = request.getParameter("type");
            String exportid = request.getParameter("recordId");
            String destinationDirectory = storageHandlerImpl.GetDocStorePath();
//            destinationDirectory += filetype.equalsIgnoreCase("csv")?"importplans":"xlsfiles";
            File intgfile = new File(destinationDirectory + StorageHandler.GetFileSeparator() + storagename);
            byte[] buff = new byte[(int) intgfile.length()];

            try {
                FileInputStream fis = new FileInputStream(intgfile);
                int read = fis.read(buff);
                Map params = new HashMap();
                params.put("exportid", exportid);
//                commonExportService.updateRequestStatus(4, params);
            } catch (IOException ex) {
                filename = "file_not_found.txt";
            }

            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentType("application/octet-stream");
            response.setContentLength(buff.length);
            response.getOutputStream().write(buff);
            response.getOutputStream().flush();
        } catch (IOException ex) {
            KrawlerLog.op.warn("Unable To Download File :" + ex.toString());
        } catch (Exception ex) {
            KrawlerLog.op.warn("Unable To Download File :" + ex.toString());
        }

    }

}
