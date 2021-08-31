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
package com.krawler.spring.accounting.chart;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class accChartControllerCMN extends MultiActionController {
    private accChartDAO accChartDAOObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setAccChartDAO(accChartDAO accChartDAOOjb) {
        this.accChartDAOObj = accChartDAOOjb;
    }

    public ModelAndView getTopCustomerChart(HttpServletRequest request, HttpServletResponse response) {
        JSONArray jarr = new JSONArray();
        String result = "";
        try {
            jarr = getTopCustomerChartData(request);
            double amount = 0;
            String personname = "";
            result = "<chart><series>";
            for (int j = 0; j < jarr.length(); j++) {
                personname = jarr.getJSONObject(j).getString("personname");
                result += "<value xid=\"" + j + "\" >" + personname + "</value>";
            }
            result += "</series><graphs><graph gid=\"0\">";
            for (int k = 0; k < jarr.length(); k++) {
                amount = jarr.getJSONObject(k).getDouble("amount");
                result += "<value xid=\"" + k + "\" >" + amount + "</value>";
            }
            result += "</graph>";
            result += "</graphs></chart>";
        } catch (JSONException ex) {
            Logger.getLogger(accChartControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accChartControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accChartControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", result);
    }

    public JSONArray getTopCustomerChartData(HttpServletRequest request) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        HashMap<String, Object> requestParam = new HashMap<String, Object>();
        JSONArray jArr = new JSONArray();
        int i = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        requestParam.put("companyid", companyid);
        requestParam.put("withinventory", Boolean.parseBoolean(request.getParameter("withinventory")));
        KwlReturnObject charData = accChartDAOObj.getTopCustomerCharData(requestParam);
        List list = charData.getEntityList();
        Iterator itr = list.iterator();

        while (itr.hasNext()) {

            Object[] row = (Object[]) itr.next();
            JSONObject obj = new JSONObject();
            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String cashAccount = pref.getCashAccount().getID();
            if (cashAccount.equals(row[2])) {
                i--;
                continue;
            }
            obj.put("personname", row[1]);
            obj.put("amount", row[0]);
            jArr.put(obj);
            i++;
            if (i >= 10) {

                break;
            }
        }
        return jArr;
    }

   public ModelAndView getTopVendorsChart(HttpServletRequest request, HttpServletResponse response) {
        JSONArray jarr = new JSONArray();
        String result = "";
        try {
            jarr = getTopVendorsChartData(request);
            double amount=0;
               String personname="";
             result = "<chart><series>";
             for (int j = 0; j < jarr.length(); j++) {
                  personname= jarr.getJSONObject(j).getString("personname");
                 result += "<value xid=\"" + j + "\" >" + personname  + "</value>";
             }
             result += "</series><graphs><graph gid=\"0\">";
             for (int k = 0; k < jarr.length(); k++) {
                  amount= jarr.getJSONObject(k).getDouble("amount");
                 result += "<value xid=\"" + k + "\" >" + amount + "</value>";
             }
             result += "</graph>";
              result += "</graphs></chart>";
        } catch (JSONException ex) {
            Logger.getLogger(accChartControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accChartControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accChartControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", result);
    }

    public JSONArray getTopVendorsChartData(HttpServletRequest request) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        HashMap<String, Object> requestParam = new HashMap<String, Object>();
        JSONArray jArr = new JSONArray();
        int i = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        requestParam.put("companyid", companyid);
        KwlReturnObject charData = accChartDAOObj.getTopVendorsChartData(requestParam);
        List list = charData.getEntityList();
        Iterator itr = list.iterator();
            while(itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JSONObject obj = new JSONObject();
                KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String cashAccount = pref.getCashAccount().getID();
            if(cashAccount.equals(row[2])){
                i--;
                continue;
            }

            obj.put("personname", row[1]);
            obj.put("amount",row[0]);
                jArr.put(obj);
                i++;
                if(i>=10)
                    break;
            }
        return jArr;
    }
}
