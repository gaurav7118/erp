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
package com.krawler.esp.handlers;

import com.krawler.spring.crm.views.JsonView;
//import com.krawler.spring.sessionHandler.CompanySessionClass;
//import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import javax.servlet.http.HttpSession;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author krawler
 */
public class sessionAdvisor implements MethodInterceptor {

    public Object invoke(MethodInvocation mi) throws Throwable {
        Object valueReturn = null;
        ServletRequestAttributes ss = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        if (sessionHandlerImpl.getInitialized(ss.getRequest()) == null || sessionHandlerImpl.getInitialized(ss.getRequest()) == "false") {
        HttpSession userSession = ss.getRequest().getSession();
        if (userSession.getAttribute("initialized") == null) {
            JsonView jview = new JsonView();
            System.out.print("" + mi.getClass().getCanonicalName());
            jview.setValidSession(false);
            valueReturn = new ModelAndView(jview, "model", "{}");
        } else {
            valueReturn = mi.proceed();
        }



        return valueReturn;
    }
}
