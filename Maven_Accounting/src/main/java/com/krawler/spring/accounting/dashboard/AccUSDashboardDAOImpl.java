/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.dashboard;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Dashboard;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.hibernate.SessionFactory;

public class AccUSDashboardDAOImpl extends BaseDAO implements AccUSDashboardDAO{
    
    private SessionFactory sessionFactory;
    private JdbcTemplate jdbcTemplate;
    private authHandlerDAO authHandlerDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    
    @Override
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    
    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    
    @Override
    public KwlReturnObject saveDashboard(JSONObject dataObj) throws ServiceException,SessionExpiredException {
        List list = new ArrayList();
        try {
            Dashboard dashboard = new Dashboard();
            String dashboardId = dataObj.has("id") ? dataObj.optString("id", "") : "";

            if (StringUtil.isNullOrEmpty(dashboardId)) {
                if (dataObj.has(Constants.useridKey) && dataObj.get(Constants.useridKey) != null) {
                    User user = (User) get(User.class, dataObj.getString(Constants.useridKey));
                    dashboard.setCreatedby(user);
                }

                if (dataObj.has("createdon")) {
                    dashboard.setCreatedon(dataObj.getLong("createdon"));
                }
                if (dataObj.has("updatedon")) {
                    dashboard.setUpdatedon(dataObj.getLong("updatedon"));
                }
            } else {
                dashboard = dataObj.get("id") == null ? null : (Dashboard) get(Dashboard.class, dataObj.getString("id"));
                if (dataObj.has("updatedon")) {
                    dashboard.setUpdatedon(dataObj.getLong("updatedon"));
                }
            }

            dashboard.setDeleted(false);


            if (dataObj.has("name")) {
                dashboard.setName(dataObj.getString("name"));
            }
            if (dataObj.has("description")) {
                dashboard.setDescription(dataObj.getString("description"));
            }
            if (dataObj.has("json")) {
                dashboard.setJson(dataObj.getString("json"));
            }
            if (dataObj.has(Constants.companyKey) && dataObj.get(Constants.companyKey) != null) {
                Company company = (Company) get(Company.class, dataObj.getString(Constants.companyKey));
                if (company != null) {
                    dashboard.setCompany(company);
                }
            }
            saveOrUpdate(dashboard);
            list.add(dashboard);
        } catch (JSONException | ServiceException e) {
            throw ServiceException.FAILURE("saveDashboard : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Dashboard Saved successfully", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getDashboard(JSONObject dataObj) throws ServiceException, SessionExpiredException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        int totalCount = 0;

        String ss = dataObj.optString(Constants.ss, null);
        String start = dataObj.optString(Constants.start, null);
        String limit = dataObj.optString(Constants.limit, null);

        String selectQuery = " from Dashboard dashboard  ";

        String conditionQuery = " where dashboard.company.companyID = ?";
        params.add(dataObj.optString(Constants.companyKey, ""));
        
        if(dataObj.has("isActive")){
            conditionQuery += " and dashboard.active = ? "; 
            params.add(dataObj.optBoolean("isActive", true));
        }
        if(dataObj.has("isProductView")){
            conditionQuery += " and dashboard.productView = ? "; 
            params.add(dataObj.optBoolean("isProductView", true));
        }

        String orderBy = " order by dashboard.name asc ";

        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"dashboard.name", "dashboard.description"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                conditionQuery += searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(AccUSDashboardDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        String sqlQuery = selectQuery + conditionQuery + orderBy;

        list = executeQuery(sqlQuery, params.toArray());
        totalCount = list.size();

        if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
            list = executeQueryPaging(sqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
        }

        return new KwlReturnObject(true, null, null, list, totalCount);
    }
    
    @Override
    public KwlReturnObject setActiveDashboard(JSONObject dataObj) throws ServiceException, SessionExpiredException {
        List list = new ArrayList();
        int totalCount = 0;
        KwlReturnObject result = null;
        String id = dataObj.optString("id", "");
        Boolean applyDefault = dataObj.optBoolean("applyDefault", false);
        Dashboard dashboard = new Dashboard();

        try {
//            if (!StringUtil.isNullOrEmpty(id)) {
            dataObj.put("isActive", true);
            result = getDashboard(dataObj);
            List<Dashboard> dashboardList = result.getEntityList();
            for (Dashboard dashboard1 : dashboardList) {
                dashboard1.setActive(false);
            }

            if (!applyDefault) {
                dashboard = dataObj.get("id") == null ? null : (Dashboard) get(Dashboard.class, dataObj.getString("id"));
                dashboard.setActive(true);
                saveOrUpdate(dashboard);
            }
            list.add(dashboard);
//            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("saveDashboard : " + e.getMessage(), e);
        }

        return new KwlReturnObject(true, null, null, list, totalCount);
    }
}