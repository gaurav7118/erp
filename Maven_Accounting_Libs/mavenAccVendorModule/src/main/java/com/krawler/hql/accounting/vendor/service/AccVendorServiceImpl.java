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
package com.krawler.hql.accounting.vendor.service;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public class AccVendorServiceImpl implements AccVendorService {

    private accVendorDAO accVendorDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public JSONObject getVendorsForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getVendorRequestMap(request);
            KwlReturnObject result = accVendorDAOobj.getVendorsForCombo(requestParams);
//            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams);
//            ArrayList resultlist = new ArrayList();
//            boolean ignoreCustomers=requestParams.get("ignorecustomers")!=null;
//            boolean ignoreVendors=requestParams.get("ignorevendors")!=null;
//            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
//            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");

            String currencyid = (String) requestParams.get("currencyid");
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.KWLCurrency", currencyid);

            boolean receivableAccFlag = request.getParameter("receivableAccFlag") != null ? Boolean.parseBoolean(request.getParameter("receivableAccFlag")) : false;
            List list = result.getEntityList();
//            int level=0;
            for(Object vendor : list) {
                Object[] row = (Object[]) vendor;
                
                if (excludeaccountid != null && row[0] != null && row[0].equals(excludeaccountid)) {
                    continue;
                }
                if ((includeparentid != null && row[0] != null && (!row[0].equals(includeparentid) || (row[1] != null && !row[1].equals(includeparentid))))) {
                    continue;
                } else if ((includeaccountid != null && row[0] != null && !row[0].equals(includeaccountid))) {
                    continue;
                }

                JSONObject obj = new JSONObject();
                obj.put("acccode", StringUtil.isNullObject(row[2]) ? "" : row[2]);
                obj.put("accid", StringUtil.isNullObject(row[0]) ? "" : row[0]);
                obj.put("accname", StringUtil.isNullObject(row[15]) ? "" : row[15]);
                obj.put("accountid", StringUtil.isNullObject(row[18]) ? "" : row[18]);
                obj.put("groupname", StringUtil.isNullObject(row[46]) ? "" : row[46]);
                obj.put("currencyid", StringUtil.isNullObject(row[19]) ? "" : row[18]);
                obj.put("currencysymbol", StringUtil.isNullObject(row[42]) ? "" : row[42]);
                obj.put("currencyname", StringUtil.isNullObject(row[43]) ? "" : row[43]);
                obj.put("taxId", StringUtil.isNullObject(row[20]) ? "" : row[20]);
//                obj.put("level", row[3]);
                if (!receivableAccFlag) {
                    obj.put("billto", StringUtil.isNullObject(row[36]) ? "" : row[36]);
                    obj.put("email", StringUtil.isNullObject(row[37]) ? "" : row[37]);
                    obj.put("termdays", StringUtil.isNullObject(row[44]) ? "" : row[44]);
                    obj.put("deleted", StringUtil.isNullObject(row[47]) ? "" : row[47]);
                }

                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(AccVendorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accVendorController.getVendorsForCombo : " + ex.getMessage();
            Logger.getLogger(AccVendorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccVendorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public HashMap<String, Object> getVendorRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        String[] groups = request.getParameterValues("group");
        String[] groupsAfterAdding = groups;
        //To do - Need to check this.
//        if (groups != null) {
//            List<String> groupsList = new ArrayList<String>(Arrays.asList(groups));
//            Set groupsSet = new HashSet(Arrays.asList(groups));
//            if (groupsSet.contains(Group.ACCOUNTS_PAYABLE)&&!groupsSet.contains(Group.BILLS_PAYABLE)) {
//                groupsList.add(Group.BILLS_PAYABLE);
//            }
//            groupsAfterAdding=groupsList.toArray(new String[groupsList.size()]);
//        }
        requestParams.put("group", groupsAfterAdding);
        requestParams.put("ignore", request.getParameter("ignore"));
        requestParams.put("ignorecustomers", request.getParameter("ignorecustomers"));
        requestParams.put("ignorevendors", request.getParameter("ignorevendors"));
        if (request.getParameter("accountid") != null && !StringUtil.isNullOrEmpty(request.getParameter("accountid"))) {
            requestParams.put("accountid", request.getParameter("accountid"));
        }
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("getSundryVendor", request.getParameter("getSundryVendor"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        if (request.getParameter("query") != null && !StringUtil.isNullOrEmpty(request.getParameter("query"))) {
            requestParams.put("ss", request.getParameter("query"));
        } else if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            requestParams.put("ss", request.getParameter("ss"));
        }
        if (request.getParameter("start") != null) {
            requestParams.put("start", request.getParameter("start"));
        }
        if (request.getParameter("limit") != null) {
            requestParams.put("limit", request.getParameter("limit"));
        }
        if (request.getParameter("comboCurrencyid") != null) {
            requestParams.put("comboCurrencyid", request.getParameter("comboCurrencyid"));
        }
        if (request.getParameter("receivableAccFlag") != null && !StringUtil.isNullOrEmpty(request.getParameter("receivableAccFlag"))) {
            requestParams.put("receivableAccFlag", request.getParameter("receivableAccFlag"));
        }
        requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        return requestParams;
    }
}
