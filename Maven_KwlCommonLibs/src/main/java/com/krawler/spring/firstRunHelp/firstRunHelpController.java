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
package com.krawler.spring.firstRunHelp;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.common.admin.EditHelp;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Karthik
 */
public class firstRunHelpController extends MultiActionController implements MessageSourceAware {

    private firstRunHelpDAO firstRunHelpDAOObj;
    private String successView;
    private HibernateTransactionManager txnManager;
    private MessageSource messageSource;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setfirstRunHelpDAO(firstRunHelpDAO firstRunHelpDAOObj1) {
        this.firstRunHelpDAOObj = firstRunHelpDAOObj1;
    }

    public JSONObject getModuleJson(List ll) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                Object row[] = (Object[]) ite.next();
                String obj = (String) row[0];
                String obj2 = (String) row[1];

                JSONObject tmpObj = new JSONObject();
                tmpObj.put("id", obj2);
                tmpObj.put("name", obj);
                jarr.put(tmpObj);
            }
            jobj.put("success", true);
            jobj.put("data", jarr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("firstRunHelpController.getModuleJson", e);
        }
        return jobj;
    }

    public ModelAndView getModule(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {

            kmsg = firstRunHelpDAOObj.getModule();
            jobj = getModuleJson(kmsg.getEntityList());
            jobj.put("totalCount", kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getComponentsJson(HttpServletRequest request, List ll) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                EditHelp obj = (EditHelp) ite.next();
                JSONObject tmpObj = new JSONObject();
                tmpObj.put("id", obj.getId());
                tmpObj.put("name", obj.getCompname());
                tmpObj.put("title", messageSource.getMessage("acc.het." + obj.getId(), null, RequestContextUtils.getLocale(request)));
                tmpObj.put("desc", messageSource.getMessage("acc.he." + obj.getId(), null, RequestContextUtils.getLocale(request)));  //obj.getDescp());
                tmpObj.put("modeid", obj.getModeid());
                tmpObj.put("compid", obj.getCompid());
                jarr.put(tmpObj);
            }
            jobj.put("success", true);
            jobj.put("data", jarr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("firstRunHelpController.getComponentsJson", e);
        }
        return jobj;
    }

    public ModelAndView getComponents(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            String modName = StringUtil.checkForNull(request.getParameter("mod"));
            kmsg = firstRunHelpDAOObj.getComponents(modName);
            jobj = getComponentsJson(request, kmsg.getEntityList());
            jobj.put("totalCount", kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getCompDetailsJson(List ll) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                EditHelp obj = (EditHelp) ite.next();
                JSONObject tmpObj = new JSONObject();
                tmpObj.put("id", obj.getId());
                tmpObj.put("title", obj.getTitle());
                tmpObj.put("desc", obj.getDescp());
                jarr.put(tmpObj);
            }
            jobj.put("success", true);
            jobj.put("data", jarr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("firstRunHelpController.getCompDetailsJson", e);
        }
        return jobj;
    }

    public ModelAndView getCompDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            String modName = StringUtil.checkForNull(request.getParameter("name"));
            kmsg = firstRunHelpDAOObj.getCompDetails(modName);
            jobj = getCompDetailsJson(kmsg.getEntityList());
            jobj.put("totalCount", kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveCompDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String newTitle = StringUtil.checkForNull(request.getParameter("title"));
            String newDesc = StringUtil.checkForNull(request.getParameter("desc"));
            String ID = StringUtil.checkForNull(request.getParameter("id"));

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("title", newTitle);
            requestParams.put("desc", newDesc);
            requestParams.put("id", ID);
            kmsg = firstRunHelpDAOObj.saveCompDetails(requestParams);

            jobj.put("success", kmsg.isSuccessFlag());
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
