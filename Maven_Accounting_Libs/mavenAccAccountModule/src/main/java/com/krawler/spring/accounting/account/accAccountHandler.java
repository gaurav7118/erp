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
package com.krawler.spring.accounting.account;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.Group;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public class accAccountHandler {

    public static HashMap<String, Object> getRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        String[] groups = request.getParameterValues("group");
        String[] groupsAfterAdding = groups;
//        if (groups != null) {
//            List<String> groupsList = new ArrayList<String>(Arrays.asList(groups));
//            Set groupsSet = new HashSet(Arrays.asList(groups));
//            if (groupsSet.contains(Group.ACCOUNTS_PAYABLE)&&!groupsSet.contains(Group.BILLS_PAYABLE)) {
//                groupsList.add(Group.BILLS_PAYABLE);
//            } else if (groupsSet.contains(Group.CURRENT_ASSETS)&&!groupsSet.contains(Group.CASH)) {
//                groupsList.add(Group.CASH);
//            }
//            groupsAfterAdding=groupsList.toArray(new String[groupsList.size()]);
//        }
        requestParams.put("group", groupsAfterAdding);
        requestParams.put("ignore", request.getParameter("ignore"));
        requestParams.put("ignorecustomers", request.getParameter("ignorecustomers"));
        requestParams.put("ignorevendors", request.getParameter("ignorevendors"));
        requestParams.put("ignoreGLAccounts", request.getParameter("ignoreGLAccounts"));
        requestParams.put("ignoreCashAccounts", StringUtil.isNullOrEmpty(request.getParameter("ignoreCashAccounts"))?null:request.getParameter("ignoreCashAccounts"));
        requestParams.put("ignoreBankAccounts", StringUtil.isNullOrEmpty(request.getParameter("ignoreBankAccounts"))?null:request.getParameter("ignoreBankAccounts"));
        requestParams.put("ignoreGSTAccounts", request.getParameter("ignoreGSTAccounts"));
        requestParams.put("ignoreAssets", request.getParameter("ignoreAssets"));
        requestParams.put("acctypes", request.getParameter("acctypes"));
        if (request.getParameter("onlyBalancesheet") != null) {
            requestParams.put("onlyBalancesheet", request.getParameter("onlyBalancesheet"));
        }

        if (request.getParameter("accountid") != null && !StringUtil.isNullOrEmpty(request.getParameter("accountid"))) {
            requestParams.put("accountid", request.getParameter("accountid"));
        }
        if (request.getParameter("ids") != null && !StringUtil.isNullOrEmpty(request.getParameter("ids"))) {
            requestParams.put("ids", request.getParameter("ids"));
        }
        requestParams.put("includeaccountid", request.getParameter("includeaccountid"));
        requestParams.put("includeparentid", request.getParameter("includeparentid"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put("headerAdded", request.getParameter("headerAdded"));
        requestParams.put("isFixedAsset", request.getParameter("isFixedAsset"));
        requestParams.put("isCustomer", request.getParameter("isCustomer"));
        requestParams.put("isVendor", request.getParameter("isVendor"));
        requestParams.put("nature", request.getParameterValues("nature"));
        requestParams.put("costCenterId", request.getParameter("costCenterId"));
        requestParams.put("COA", request.getParameter("COA"));
        requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
        if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            requestParams.put("ss", request.getParameter("ss"));
        } else if (request.getParameter("query") != null && !StringUtil.isNullOrEmpty(request.getParameter("query"))) {
            requestParams.put("ss", request.getParameter("query"));
        } 
        if (request.getParameter("ignoreTaggedAccounts") != null && !StringUtil.isNullOrEmpty(request.getParameter("ignoreTaggedAccounts"))) {
            requestParams.put("ignoreTaggedAccounts", request.getParameter("ignoreTaggedAccounts"));
        }

        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            if (request.getParameter("start") != null) {
                requestParams.put("start", request.getParameter("start"));
            }
            if (request.getParameter("limit") != null) {
                requestParams.put("limit", request.getParameter("limit"));
            }
        }

        if (request.getParameter("dir") != null && !StringUtil.isNullOrEmpty(request.getParameter("dir"))
                && request.getParameter("sort") != null && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
            requestParams.put("dir", request.getParameter("dir"));
            requestParams.put("sort", request.getParameter("sort"));
        }
        requestParams.put("intercompanyflag", request.getParameter("intercompanyflag"));
        requestParams.put("intercompanytypeid", request.getParameter("intercompanytypeid"));
        if (request.getParameter("controlAccounts") != null) {
            requestParams.put("controlAccounts", StringUtil.getBoolean(request.getParameter("controlAccounts")));
        }

        if (!StringUtil.isNullOrEmpty(request.getParameter("isIBGTypeOnly"))) {
            requestParams.put("isIBGTypeOnly", Boolean.parseBoolean(request.getParameter("isIBGTypeOnly")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("showGSTAndExpenseGLAccounts"))) {
            requestParams.put("showGSTAndExpenseGLAccounts", Boolean.parseBoolean(request.getParameter("showGSTAndExpenseGLAccounts")));
        }
        if(request.getParameter("searchstartwith") !=null)
        {
            requestParams.put("searchstartwith",request.getParameter("searchstartwith"));
        }
        //requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put("bankBook", request.getParameter("bankBook"));
        requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
        requestParams.put("accgroupids", request.getParameter("accgroupids"));
        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.requestModuleId))) {
            requestParams.put(Constants.requestModuleId, request.getParameter(Constants.requestModuleId));
        }

        return requestParams;
    }

    public static HashMap<String, Object> getJsonMap(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        String[] groups = null;
        if(paramJobj.has("group")){
            JSONArray groupArr = paramJobj.getJSONArray("group");
            List<String> groupList = new ArrayList<String>();
            for (int i = 0; i < groupArr.length(); i++) {
                groupList.add(groupArr.getJSONObject(i).getString("name"));
            }
            groups = Arrays.copyOf(groupList.toArray(), groupList.toArray().length, String[].class);
            
        }
        requestParams.put("group", groups);
        requestParams.put("ignore", paramJobj.optString("ignore",null));
        requestParams.put("ignorecustomers", paramJobj.optString("ignorecustomers",null));
        requestParams.put("ignorevendors", paramJobj.optString("ignorevendors",null));
        requestParams.put("ignoreGLAccounts", paramJobj.optString("ignoreGLAccounts",null));
        requestParams.put("ignoreCashAccounts", StringUtil.isNullOrEmpty(paramJobj.optString("ignoreCashAccounts"))?null:paramJobj.getString("ignoreCashAccounts"));
        requestParams.put("ignoreBankAccounts", StringUtil.isNullOrEmpty(paramJobj.optString("ignoreBankAccounts"))?null:paramJobj.getString("ignoreBankAccounts"));
        requestParams.put("ignoreGSTAccounts", paramJobj.optString("ignoreGSTAccounts",null));
        requestParams.put("ignoreAssets", paramJobj.optString("ignoreAssets",null));
        requestParams.put("acctypes", paramJobj.optString("acctypes",null));
        if (paramJobj.optString("onlyBalancesheet",null) != null) {
            requestParams.put("onlyBalancesheet", paramJobj.optString("onlyBalancesheet",null));
        }

        if (!StringUtil.isNullOrEmpty(paramJobj.optString("accountid",null))) {
            requestParams.put("accountid", paramJobj.getString("accountid"));
        }

        requestParams.put("includeaccountid", paramJobj.optString("includeaccountid",null));
        requestParams.put("includeparentid", paramJobj.optString("includeparentid",null));
        requestParams.put("deleted", paramJobj.optString("deleted",null));
        requestParams.put("nondeleted", paramJobj.optString("nondeleted",null));
        requestParams.put("headerAdded", paramJobj.optString("headerAdded",null));
        requestParams.put("isFixedAsset", paramJobj.optString("isFixedAsset",null));
        requestParams.put("isCustomer", paramJobj.optString("isCustomer",null));
        requestParams.put("isVendor", paramJobj.optString("isVendor",null));
        String[] nature = null;
        if(paramJobj.has("nature")){
            JSONArray groupArr = paramJobj.getJSONArray("nature");
            List<String> natureList = new ArrayList<String>();
            for (int i = 0; i < groupArr.length(); i++) {
                natureList.add(groupArr.getJSONObject(i).getString("name"));
            }
            nature = Arrays.copyOf(natureList.toArray(), natureList.toArray().length, String[].class);
            
        }
        requestParams.put("nature", nature);
        requestParams.put("costCenterId", paramJobj.optString("costCenterId",null));
        requestParams.put("COA", paramJobj.optString("COA",null));
        requestParams.put("currencyid", paramJobj.optString("currencyid",null));
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("ss",null))) {
            requestParams.put("ss", paramJobj.getString("ss"));
        } else if (!StringUtil.isNullOrEmpty(paramJobj.optString("query",null))) {
            requestParams.put("ss", paramJobj.getString("query"));
        } 
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("ignoreTaggedAccounts",null))) {
            requestParams.put("ignoreTaggedAccounts", paramJobj.getString("ignoreTaggedAccounts"));
        }

        if (StringUtil.isNullOrEmpty(paramJobj.optString("filetype",null))) {
            if (paramJobj.optString("start",null) != null) {
                requestParams.put("start", paramJobj.getString("start"));
            }
            if (paramJobj.optString("limit",null) != null) {
                requestParams.put("limit", paramJobj.getString("limit"));
            }
        }

        if (!StringUtil.isNullOrEmpty(paramJobj.optString("dir",null))
                && !StringUtil.isNullOrEmpty(paramJobj.optString("sort",null))) {
            requestParams.put("dir", paramJobj.getString("dir"));
            requestParams.put("sort", paramJobj.getString("sort"));
        }
        requestParams.put("intercompanyflag", paramJobj.optString("intercompanyflag",null));
        requestParams.put("intercompanytypeid", paramJobj.optString("intercompanytypeid",null));
        if (paramJobj.optString("controlAccounts",null) != null) {
            requestParams.put("controlAccounts", StringUtil.getBoolean(paramJobj.getString("controlAccounts")));
        }

        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isIBGTypeOnly",null))) {
            requestParams.put("isIBGTypeOnly", Boolean.parseBoolean(paramJobj.getString("isIBGTypeOnly")));
        }

        //requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
        requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json,null));
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(Constants.Filter_Criteria,null));
        requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));
        requestParams.put("bankBook", paramJobj.optString("bankBook",null));
        requestParams.put(Constants.REQ_startdate, paramJobj.optString(Constants.REQ_startdate,null));
        requestParams.put(Constants.REQ_enddate, paramJobj.optString(Constants.REQ_enddate,null));
        requestParams.put("accgroupids", paramJobj.optString("accgroupids",null));

        return requestParams;
    }
    
    public static JSONObject getAccountJson(HttpServletRequest request, List list, accCurrencyDAO accCurrencyDAOobj, boolean noactivity) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            KwlReturnObject bAmt, presentBaseAmount;
            String currencyid = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double openbalanceInbase = 0, presentbalanceInBase = 0, openbalanceSummary = 0, presentbalanceSummary = 0;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            while (itr.hasNext()) {
                openbalanceInbase = 0;
                presentbalanceInBase = 0;
                Object[] row = (Object[]) itr.next();
                Account account = (Account) row[0];
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("accdesc", StringUtil.isNullOrEmpty(account.getDescription())?"":account.getDescription());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", account.getOpeningBalance());
                obj.put("acctaxcode", (!StringUtil.isNullOrEmpty(account.getTaxid())) ? account.getTaxid() : "");//"c340667e2896c0d80128a569f065017a");
                KWLCurrency currency = (KWLCurrency) row[5];
                currencyid = account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID();

                if (!noactivity && !account.isDeleted()) {
                    List childlist = new ArrayList(account.getChildren());
                    if (childlist.isEmpty()) {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, account.getOpeningBalance(), currencyid, account.getCreationDate(), 0);
                        openbalanceInbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, account.getOpeningBalance(), currencyid, account.getCreationDate(), 0);
                        openbalanceInbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        openbalanceInbase = getTotalOpeningBalance(account, openbalanceInbase, currency.getCurrencyID(), accCurrencyDAOobj, request);
                    }
                    if (account.getParent() == null) {
                        openbalanceSummary += openbalanceInbase;
                    }
                    obj.put("openbalanceinbase", openbalanceInbase);
                } else {
                    obj.put("openbalanceinbase", openbalanceInbase);
                }
                presentBaseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, account.getPresentValue(), currencyid, account.getCreationDate(), 0);
                presentbalanceInBase = authHandler.round((Double) presentBaseAmount.getEntityList().get(0), companyid);
                obj.put("presentbalanceInBase", presentbalanceInBase);
              //  obj.put("depreciationaccount", account.getDepreciationAccont() == null ? "" : account.getDepreciationAccont().getID());
                Account parentAccount = (Account) row[6];
                if (parentAccount != null) {
                    obj.put("parentid", parentAccount.getID());
                    obj.put("parentname", parentAccount.getName());
                }
                obj.put("currencyid", currencyid);
                obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                obj.put("level", row[3]);
                obj.put("leaf", row[4]);
                obj.put("presentbalance", account.getPresentValue());
                obj.put("custminbudget", account.getCustMinBudget());
                obj.put("life", account.getLife());
                obj.put("salvage", account.getSalvage());
                obj.put("budget", account.getBudget());
                obj.put("taxid", account.getTaxid());

                if (!StringUtil.isNullOrEmpty(account.getAcccode())) {
                    obj.put("acccode", account.getAcccode());
                } else {
                    obj.put("acccode", "");
                }
                boolean accountCodeNotAdded = Boolean.parseBoolean((String) request.getParameter("accountCodeNotAdded"));
                obj.put("accnamecode", (accountCodeNotAdded) ? account.getName() : ((!StringUtil.isNullOrEmpty(account.getAcccode())) ? ("[" + account.getAcccode() + "] " + account.getName()) : account.getName()));
                obj.put("deleted", account.isDeleted());
                obj.put("posted", row[7]);
                obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(account.getCreationDate()));
                obj.put("categoryid", account.getCategory() == null ? "" : account.getCategory().getID());
                obj.put("departmentid", account.getDepartment() == null ? "" : account.getDepartment().getID());
                obj.put("locationid", account.getLocation() == null ? "" : account.getLocation().getID());
                obj.put("installation", account.getInstallation() == null ? "" : account.getInstallation());
                obj.put("userid", account.getUser() == null ? "" : account.getUser().getUserID());
                obj.put("isdepreciable", account.isDepreciable());
                obj.put("costcenterid", account.getCostcenter() == null ? "" : account.getCostcenter().getID());
                obj.put("costcenterName", account.getCostcenter() == null ? "" : account.getCostcenter().getName());
                obj.put("aliascode", account.getAliascode() == null ? "" : account.getAliascode());
                obj.put("accounttype", account.getAccounttype());
                obj.put("mastertypevalue", account.getMastertypevalue());
                obj.put("ifsccode", account.getIfsccode() == null ? "" : account.getIfsccode());
                if (!account.isDeleted()) {
                    /*
                     * openbalanceSummary += presentbalanceInBase;
                     */ //openbalanceSummary += openbalanceInbase;
                    presentbalanceSummary += presentbalanceInBase;
                }
                if (account.isHeaderaccountflag()) {
                    obj.put("isHeaderAccount", true);
                } else {
                    obj.put("isHeaderAccount", false);
                }
                obj.put("eliminateflag", account.isEliminateflag());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("openbalanceSummary", openbalanceSummary);
            jobj.put("presentbalanceSummary", presentbalanceSummary);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public static double getTotalOpeningBalance(Account account, double totalOpeningBalance, String defaultCurrencyid, accCurrencyDAO accCurrencyDAOobj, HttpServletRequest request) throws ServiceException {
        try {
            List list = new ArrayList(account.getChildren());
            Iterator itr = list.iterator();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            while (itr.hasNext()) {
                Account subAccount = (Account) itr.next();
                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                double balance = 0;
                String currencyid = "";
                if (!subAccount.isDeleted()) {
                    currencyid = subAccount.getCurrency() == null ? defaultCurrencyid : subAccount.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, subAccount.getOpeningBalance(), currencyid, subAccount.getCreationDate(), 0);
                    balance = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                }
                totalOpeningBalance = totalOpeningBalance + balance;
                if (subAccount.getChildren().isEmpty()) {
                    continue;
                }
                //Recursive function to get child accounts
                totalOpeningBalance = getTotalOpeningBalance(subAccount, totalOpeningBalance, defaultCurrencyid, accCurrencyDAOobj, request);
            }
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return totalOpeningBalance;
    }
}
