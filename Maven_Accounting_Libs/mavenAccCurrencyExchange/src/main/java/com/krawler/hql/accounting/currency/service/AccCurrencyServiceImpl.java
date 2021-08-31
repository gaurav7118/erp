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
package com.krawler.hql.accounting.currency.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.ExchangeRate;
import com.krawler.hql.accounting.ExchangeRateDetails;
import com.krawler.spring.accounting.currency.CurrencyContants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.JSONException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public class AccCurrencyServiceImpl implements AccCurrencyService, CurrencyContants {

    private accCurrencyDAO accCurrencyDAOobj;

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public JSONObject getCurrencyExchange(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(TRANSACTIONDATE, request.getAttribute("transactiondate") != null ? request.getAttribute("transactiondate") : request.getParameter(TRANSACTIONDATE));
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(FROMCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            String toCurrencyid = request.getParameter(TOCURRENCYID);
            if (!StringUtil.isNullOrEmpty(toCurrencyid)) {
                requestParams.put(TOCURRENCYID, request.getParameter(TOCURRENCYID));
            }
            KwlReturnObject result = accCurrencyDAOobj.getCurrencyExchange(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getCurrencyExchangeJson(request, list);
            jobj.put(DATA, jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccCurrencyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccCurrencyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                try {
                    jobj.put(SUCCESS, issuccess);
                    jobj.put(MSG, msg);
                } catch (com.krawler.utils.json.base.JSONException ex) {
                    Logger.getLogger(AccCurrencyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (JSONException ex) {
                Logger.getLogger(AccCurrencyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public JSONArray getCurrencyExchangeJson(HttpServletRequest request, List<ExchangeRate> list) throws SessionExpiredException, ServiceException, java.text.ParseException, com.krawler.utils.json.base.JSONException {
        JSONArray jArr = new JSONArray();
        try {
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            Date transactiondate = null;
            String date = request.getAttribute("transactiondate") != null ? authHandler.getDateFormatter(request).format(request.getAttribute("transactiondate")) : request.getParameter(TRANSACTIONDATE);
            String isAddAll = request.getParameter("isAll");
            if (!StringUtil.isNullOrEmpty(date)) {
                transactiondate = authHandler.getDateOnlyFormat(request).parse(date);
            }
            DateFormat df = authHandler.getDateOnlyFormat(request);
//            Iterator itr = list.iterator();
//            while (itr.hasNext()) {
//                ExchangeRate ER = (ExchangeRate) itr.next();
            JSONObject obj = new JSONObject();
//            if (!StringUtil.isNullOrEmpty(isAddAll)) {
//                obj.put(TOCURRENCYID, "1234");
//                obj.put(TOCURRENCY, "All");
//                jArr.put(obj);
//            }
            if (list != null && !list.isEmpty()) {
                for (ExchangeRate ER : list) {
                    String erID = ER.getID();
                    //                ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,null,transactiondate,erID);
                    KwlReturnObject erdresult = accCurrencyDAOobj.getExcDetailID(requestParams, null, transactiondate, erID);
                    ExchangeRateDetails erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                    obj = new JSONObject();
                    if (erd != null) {
                        obj.put(ID, erd.getExchangeratelink().getID());
                        obj.put(APPLYDATE, df.format(erd.getApplyDate()));
                        obj.put(EXCHANGERATE, erd.getExchangeRate());
                        obj.put(NEWEXCHANGERATE, erd.getExchangeRate());
                        obj.put(FROMCURRENCY, erd.getExchangeratelink().getFromCurrency().getName());
                        obj.put(SYMBOL, erd.getExchangeratelink().getToCurrency().getSymbol());
                        obj.put(HTMLCODE, erd.getExchangeratelink().getToCurrency().getHtmlcode());
                        obj.put(CURRENCYCODE, erd.getExchangeratelink().getToCurrency().getCurrencyCode());
                        obj.put(TOCURRENCY, erd.getExchangeratelink().getToCurrency().getName());
                        obj.put(TOCURRENCYID, erd.getExchangeratelink().getToCurrency().getCurrencyID());
                        obj.put(FROMCURRENCYID, erd.getExchangeratelink().getFromCurrency().getCurrencyID());
                        obj.put(COMPANYID, erd.getCompany().getCompanyID());
                        jArr.put(obj);
                    }
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCurrencyExchangeJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
}
