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

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 *
 * @author krawler
 */
public class errorMessageController extends AbstractController {

    private sessionHandlerImpl sessionHandlerImplObj;
    private authHandlerDAO authHandlerDAOObj;
    private companyDetailsDAO companyDetailsDAOObj;
    
    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    
    }
    
    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }


    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String pageTitle = "Access Denied";
        String errReason = request.getParameter("e");
        String newDomain = request.getParameter("n");
        String errMsg = "Sorry, the page you requested doesn't exist.";
        String errImgPath = "pagenotfound";
        String subdomainFromSession = "";
        if (!StringUtil.isNullOrEmpty(errReason)) {
            if ("noaccess".equalsIgnoreCase(errReason)) {
                errImgPath = errReason;
                errMsg = "Sorry, you don't have access to this application";
            } else if ("alreadyloggedin".equalsIgnoreCase(errReason) && !StringUtil.isNullOrEmpty(newDomain)) {
                if (sessionHandlerImpl.isValidSession(request, response)) {

                    String companyid = sessionHandlerImplObj.getCompanyid(request);
                    subdomainFromSession = companyDetailsDAOObj.getSubDomain(companyid);
                    pageTitle = "Already logged in";
                    errMsg = "You are already logged in with another Deskera account.";
                }

            }

        }
        HashMap model = new HashMap();
        model.put("errImgPath", errImgPath);
        model.put("errMsg", errMsg);
        model.put("errReason", errReason);
        model.put("pageTitle", pageTitle);
        model.put("newDomain", newDomain);
        model.put("subdomainFromSession", subdomainFromSession);
        return new ModelAndView("error", "model", model);
    }
}