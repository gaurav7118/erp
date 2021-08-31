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
package com.krawler.spring.common;

import com.krawler.common.admin.*;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import com.krawler.utils.json.base.JSONObject;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author Karthik
 */
public class kwlCommonTablesController extends MultiActionController {

    private String successView;
    private kwlCommonTablesService kwlCommonTablesService;

    public void setKwlCommonTablesService(kwlCommonTablesService kwlCommonTablesService) {
        this.kwlCommonTablesService = kwlCommonTablesService;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    
    public ModelAndView getAllTimeZones(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = kwlCommonTablesService.getAllTimeZones(paramJobj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    
    public ModelAndView getAllCurrencies(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = kwlCommonTablesService.getAllCurrencies(paramJobj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAllCountries(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = kwlCommonTablesService.getAllCountries(paramJobj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAllStates(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = kwlCommonTablesService.getAllStates(paramJobj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }


    public ModelAndView getSubdomainListFromCountry(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj =  kwlCommonTablesService.getSubdomainListFromCountry(paramJobj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAllDateFormats(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String[] moduleidarray = request.getParameterValues("formatids");
            paramJobj.put("moduleidarray", moduleidarray);
            jobj = kwlCommonTablesService.getAllDateFormats(paramJobj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAllInventoryStores(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String inventoryURL = this.getServletContext().getInitParameter(Constants.inventoryURL);
            paramJobj.put(Constants.inventoryURL, inventoryURL);
            jobj = kwlCommonTablesService.getAllInventoryStores(paramJobj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Logger.getLogger(kwlCommonTablesController.class.getName()).log(Level.SEVERE, null, e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getLandingCostCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = kwlCommonTablesService.getLandingCostCategory(paramJobj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getSourceDocumentTermsInLinkingDocument(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = kwlCommonTablesService.getSourceDocumentTermsInLinkingDocument(paramJobj);
            issuccess = true;
        } catch (Exception e) {
            msg = " "+e.getMessage();
            Logger.getLogger(kwlCommonTablesController.class.getName()).log(Level.SEVERE, null, e);
        }finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (Exception ex) {
                Logger.getLogger(kwlCommonTablesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

}
