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
package com.krawler.spring.accounting.customer.service;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Customer;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public class AccCustomerServiceImpl implements AccCustomerService {

    private accCustomerDAO accCustomerDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public JSONObject getCustomersForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getCustomerRequestMap(request);
            KwlReturnObject result = accCustomerDAOobj.getCustomersForCombo(requestParams);
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");

            boolean receivableAccFlag = request.getParameter("receivableAccFlag") != null ? Boolean.parseBoolean(request.getParameter("receivableAccFlag")) : false;

            List<Object[]> list = result.getEntityList();

            for (Object[] row : list) {                                
                if (excludeaccountid != null && row[0] != null && row[0].equals(excludeaccountid)) {
                    continue;
                }
                if ((includeparentid != null && row[0] != null && (!row[0].equals(includeparentid) || (row[1] != null && !row[1].equals(includeparentid))))) {
                    continue;
                } else if ((includeaccountid != null && row[0] != null && !row[0].equals(includeaccountid))) {
                    continue;
                }

                JSONObject obj = new JSONObject();
                obj.put("accid", row[0] != null ? row[0] : "");
                obj.put("acccode", row[2] != null ? row[2] : "");
                obj.put("accountid", row[4] != null ? row[4] : "");
                obj.put("accname", row[5] != null ? row[5] : "");
                obj.put("currencyid", row[33] != null ? row[33] : "");
                obj.put("currencysymbol", row[34] != null ? row[34] : "");
                obj.put("currencyname", row[35] != null ? row[35] : "");
                obj.put("taxId", row[7] != null ? row[7] : "");
                if (!receivableAccFlag) {
                    obj.put("masterSalesPerson", row[19] != null ? row[19] : "");
                    obj.put("billto", row[20] != null ? row[20] : "");
                    obj.put("email", row[21] != null ? row[21] : "");
                    obj.put("groupname", row[36] != null ? row[36] : "");
                    obj.put("termdays", row[37] != null ? row[37] : "");
                    obj.put("deleted", row[40] != null ? row[40] : "");
                    }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(AccCustomerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomersForCombo : " + ex.getMessage();
            Logger.getLogger(AccCustomerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public static HashMap<String, Object> getCustomerRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        String[] groups = request.getParameterValues("group");
        String[] groupsAfterAdding = groups;
        //To do - No depedndecy on accounts in customer and vendor.
//        if (groups != null) {
//            List<String> groupsList = new ArrayList<String>(Arrays.asList(groups));
//            Set groupsSet = new HashSet(Arrays.asList(groups));
//             if (groupsSet.contains(Group.ACCOUNTS_PAYABLE)&&!groupsSet.contains(Group.BILLS_PAYABLE)) {
//                groupsList.add(Group.BILLS_PAYABLE);
//            } else if (groupsSet.contains(Group.CURRENT_ASSETS)&&!groupsSet.contains(Group.CASH)) {
//                groupsList.add(Group.CASH);
//            }
//            groupsAfterAdding = groupsList.toArray(new String[groupsList.size()]);
//        }
        requestParams.put("group", groupsAfterAdding);
//        requestParams.put("getSundryCustomer", request.getParameter("getSundryCustomer"));        
//        requestParams.put("query", request.getParameter("query"));
        requestParams.put("ignore", request.getParameter("ignore"));
        requestParams.put("ignorecustomers", request.getParameter("ignorecustomers"));
        requestParams.put("ignorevendors", request.getParameter("ignorevendors"));
        if (request.getParameter("accountid") != null && !StringUtil.isNullOrEmpty(request.getParameter("accountid"))) {
            requestParams.put("accountid", request.getParameter("accountid"));
        }

        requestParams.put("deleted", request.getParameter("deleted"));
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
        if (request.getParameter("selectedCustomerIds") != null && !StringUtil.isNullOrEmpty(request.getParameter("selectedCustomerIds"))) {
            requestParams.put("selectedCustomerIds", request.getParameter("selectedCustomerIds"));
        }
        requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
        requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        return requestParams;
    }
}
