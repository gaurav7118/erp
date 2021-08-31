/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.common.filters;

import com.krawler.common.util.CompanyContextHolder;
import com.krawler.common.util.Constants;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

public class RequestListenerForDB implements ServletRequestListener {

    private static Logger _logger = Logger.getLogger(RequestListenerForDB.class.getName());
    Pattern pattern = Pattern.compile("/[ab]/([^\\/]*)/(.*)");

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        CompanyContextHolder.clearCompanySubdomain();
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        String path = request.getRequestURL().toString();
        String subdomain = null;
        if ((subdomain = extractSubdomain(path, pattern)) != null) {
            CompanyContextHolder.setCompanySubdomain(subdomain);
        } else if ((subdomain = request.getParameter(Constants.COMPANY_PARAM)) != null) {
            CompanyContextHolder.setCompanySubdomain(subdomain.trim());
        } else if ((subdomain = request.getParameter(Constants.COMPANY_SUBDOMAIN)) != null) {
            CompanyContextHolder.setCompanySubdomain(subdomain.trim());
        } else if (request.getParameter(Constants.companyKey) != null) {
            String companyid = request.getParameter(Constants.companyKey);
            companyid = companyid.replaceAll("\n", "");
            CompanyContextHolder.setCompanyID(companyid);
        } else if (request.getParameter(Constants.useridKey) != null) {
            String userid = request.getParameter(Constants.useridKey);
            userid = userid.replaceAll("\n", "");
            CompanyContextHolder.setUserID(userid);
        } else if ((path.contains("deskeraCRMMOB_V1.jsp") || path.contains("caseAuthlogin.jsp")) && (((subdomain = request.getParameter("d")) != null) || ((subdomain = request.getParameter("cdomain")) != null))) {// handle case for iphone user authentication
            CompanyContextHolder.setCompanySubdomain(subdomain.toLowerCase().trim());
        } else if (request.getParameter(Constants.RES_data) != null) {
            try {
                JSONObject requestObj = new JSONObject(request.getParameter(Constants.RES_data));
                if (requestObj.has(Constants.RES_CDOMAIN)) {
                    CompanyContextHolder.setCompanySubdomain(requestObj.getString(Constants.RES_CDOMAIN));
                } else if (requestObj.has(Constants.COMPANY_SUBDOMAIN)) {
                    CompanyContextHolder.setCompanySubdomain(requestObj.getString(Constants.COMPANY_SUBDOMAIN));
                } else if (requestObj.has(Constants.companyKey)) {
                    CompanyContextHolder.setCompanyID(requestObj.getString(Constants.companyKey));
                } else if (requestObj.has(Constants.useridKey)) {
                    CompanyContextHolder.setUserID(requestObj.getString(Constants.useridKey));
                    
                }
            } catch (JSONException ex) {
                Logger.getLogger(RequestListenerForDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            clearCompanySubdomain();
        }
        _logger.fine("Current subdomain : " + subdomain);
    }

    private void clearCompanySubdomain() {
        CompanyContextHolder.clearCompanySubdomain();
    }

    private String extractSubdomain(String path, Pattern p) {
        Matcher m = p.matcher(path);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
