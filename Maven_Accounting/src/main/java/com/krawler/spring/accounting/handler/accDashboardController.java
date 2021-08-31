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
package com.krawler.spring.accounting.handler;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accDashboardController extends MultiActionController implements MessageSourceAware{
    
    private MessageSource messageSource;
    private AccDashboardService accDashboardService;


    public AccDashboardService getAccDashboardService() {
        return accDashboardService;
    }

    public void setAccDashboardService(AccDashboardService accDashboardService) {
        this.accDashboardService = accDashboardService;
    }
    
    @Override
	public void setMessageSource(MessageSource msg) {
		this.messageSource = msg;
                accDashboardService.setMessageSource(msg);
	}
    
    public ModelAndView getDashboardData(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        try {
            JSONObject jbj = new JSONObject();
            msg = accDashboardService.getDashboardData(request);
            boolean refresh = true;
            //  msg += "<link rel='alternate' type='application/rss+xml' title='RSS - Global RSS Feed' href=\""+com.krawler.common.util.URLUtil.getPageURL(request,"")+"feed.rss?m=global&u="+AuthHandler.getUserName(request)+"\">";
            /*Request param must be sent from atleast one case*/
            if (StringUtil.isNullOrEmpty(request.getParameter("refresh"))) {
                refresh = true;
            } else {
                refresh = Boolean.parseBoolean(request.getParameter("refresh"));
            }
            if (refresh) {
                jbj.put("valid", true);
                jbj.put("data", msg);
                msg = jbj.toString();
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }


    public ModelAndView getDashboardLinks(HttpServletRequest request, HttpServletResponse response) {
        String platformURL = this.getServletContext().getInitParameter("platformURL");
        request.setAttribute("platformURL", platformURL);
        return accDashboardService.getDashboardLinks(request, response);
    }
    
    
    public ModelAndView getMaintainanceDetails(HttpServletRequest request, HttpServletResponse response) {
        String platformURL = this.getServletContext().getInitParameter("platformURL");
        request.setAttribute("platformURL", platformURL);
        return accDashboardService.getMaintainanceDetails(request, response);
    }
    public ModelAndView getDashboardUpdates(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = ""; 
        try {
           
            String outPanelDetails=accDashboardService.getDashboardUpdates(request, response);
            jobj.put("data", outPanelDetails);
            jobj.put("Timezone", accDashboardService.checkCompanyAndUserTimezone(request, response));
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString()); 
    }

    public ModelAndView getPendingApprovalsForAllModules(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            int reportId = 0;
            DateFormat df = authHandler.getDateFormatter(request);
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            String userid = sessionHandlerImpl.getUserid(request);
            String userFullName = sessionHandlerImpl.getUserFullName(request);
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.userdf, userdf);
            requestParams.put("companyid", companyId);
            requestParams.put("userid", userid);
            requestParams.put("userFullName", userFullName);
            String start = "";
            String limit = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.start)) && !StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) {
                start = request.getParameter(Constants.start);
                limit = request.getParameter(Constants.limit);
            }
            requestParams.put(Constants.start, start);
            requestParams.put(Constants.limit, limit);

            if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                requestParams.put("searchJson", request.getParameter("searchJson"));
                requestParams.put("moduleid", request.getParameter("moduleid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
                reportId = Integer.parseInt(request.getParameter("reportId"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                requestParams.put("ss", request.getParameter("ss"));
            }
            requestParams.put("locale", RequestContextUtils.getLocale(request));
            jobj = accDashboardService.getPendingApprovalsForAllModulesJson(requestParams);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView saveUserPreferencesOptions(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            JSONObject paramObj = StringUtil.convertRequestToJsonObject(request);
            issuccess = accDashboardService.saveUserPreferencesOptions(paramObj);
        } catch (Exception ex) {
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
