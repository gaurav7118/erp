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
package com.krawler.spring.accounting.discount;

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accDiscountController extends MultiActionController {

    accDiscountService accDiscountServiceObj;
    private MessageSource messageSource;

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public accDiscountService getAccDiscountServiceObj() {
        return accDiscountServiceObj;
    }

    public void setAccDiscountServiceObj(accDiscountService accDiscountServiceObj) {
        this.accDiscountServiceObj = accDiscountServiceObj;
    }

    public ModelAndView getDiscountsAndTerms(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put(Constants.companyKey, companyid);
            jObj = accDiscountServiceObj.getDiscountsAndTerms(requestParams);
            msg = "Success";
            isSuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accDiscountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put(Constants.RES_success, isSuccess);
                jObj.put(Constants.RES_msg, msg);
            } catch (Exception ex) {
                Logger.getLogger(accDiscountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());

    }
    public ModelAndView getDiscountMaster(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            String companyid = request.getParameter("companyid");

            requestParams.put(Constants.companyKey, companyid);
            requestParams.put(Constants.start, start);
            requestParams.put(Constants.limit, limit);

            jObj = accDiscountServiceObj.getDiscountMaster(requestParams);

            msg = "Success";
            isSuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accDiscountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put(Constants.RES_success, isSuccess);
                jObj.put(Constants.RES_msg, msg);
            } catch (Exception ex) {
                Logger.getLogger(accDiscountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        System.out.println("Hello " + jObj.toString());
        return new ModelAndView("jsonView", "model", jObj.toString());
    }

    public ModelAndView saveDiscountMaster(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONObject result = null;
        Map<String, Object> requestParam = new HashMap();
        Locale locale = RequestContextUtils.getLocale(request);
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userName = sessionHandlerImpl.getUserFullName(request);
            String userID = sessionHandlerImpl.getUserid(request);
            String ipaddr="";
            if (StringUtil.isNullOrEmpty(request.getHeader("x-real-ip"))) {
                ipaddr = request.getRemoteAddr();
            } else {
                ipaddr = request.getHeader("x-real-ip");
            }
            requestParam.put(Constants.remoteIPAddress,ipaddr);
            requestParam.put("deleteddata", jDelArr);
            requestParam.put("data", jArr);
            requestParam.put(Constants.companyid, companyid);
            requestParam.put(Constants.username, userName);
            requestParam.put("userid", userID);
            requestParam.put("locale", locale);
            result = accDiscountServiceObj.saveDiscountMaster(requestParam);
            issuccess = result.optBoolean("success");
            /*
             * a variable to store the names of Discount Master which are being used in other transactions 
             */
            String linkedDiscountMaster = result.optString("isUsedDiscountMaster");
            /*
             * a variable to store the name of duplicate Discount center
             */
            String duplicateDiscountMaster = result.optString("isDuplicateDiscountMaster");
            /*
             * if the Discount Master is not used in other transactions and not duplicate
             */
            if (StringUtil.isNullOrEmpty(linkedDiscountMaster) && StringUtil.isNullOrEmpty(duplicateDiscountMaster)) {
                msg = messageSource.getMessage("acc.dm.add", null, RequestContextUtils.getLocale(request));
            } /*
             * if the Discount Master is used in other transactions and duplicate
             */ else if (!StringUtil.isNullOrEmpty(linkedDiscountMaster) && !StringUtil.isNullOrEmpty(duplicateDiscountMaster)) {
                msg = messageSource.getMessage("acc.dm.except", null, RequestContextUtils.getLocale(request)) + "  <b>" + linkedDiscountMaster + " </b>" + " and " + " <b>" + duplicateDiscountMaster + " </b>.<br><b>" + linkedDiscountMaster + "</b> " + messageSource.getMessage("acc.dm.used1", null, RequestContextUtils.getLocale(request)) + "<br>" + messageSource.getMessage("acc.dm.cantAdd", null, RequestContextUtils.getLocale(request)) + "  " + "<b>" + duplicateDiscountMaster + " " + "</b>" + messageSource.getMessage("acc.dm.exists1", null, RequestContextUtils.getLocale(request)) + ".";
            } /*
             * if the Discount Master is duplicate
             */ else if (!StringUtil.isNullOrEmpty(duplicateDiscountMaster)) {
                msg = messageSource.getMessage("acc.dm.except", null, RequestContextUtils.getLocale(request)) + " " + "<b>" + duplicateDiscountMaster + "</b>.<br>" + messageSource.getMessage("acc.dm.exists2", null, RequestContextUtils.getLocale(request)) + ".";
            } /*
             * if the Discount Master is used in other transactions
             */ else if (!StringUtil.isNullOrEmpty(linkedDiscountMaster)) {
                msg = messageSource.getMessage("acc.dm.except", null, RequestContextUtils.getLocale(request)) + " " + "<b>" + " " + linkedDiscountMaster + "</b>.<br>" + messageSource.getMessage("acc.dm.used2", null, RequestContextUtils.getLocale(request));
            }

        } catch (AccountingException aex) {
            msg = aex.getMessage();
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(accDiscountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put(Constants.RES_success, issuccess);
                jObj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDiscountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }
}
