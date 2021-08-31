/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.exportFunctionality;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProductExportDetail;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import org.hibernate.QueryException;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author krawler
 */
public class CommonExportDAOImpl extends BaseDAO implements CommonExportDAO {
    
    private SessionFactory sessionFactory;
    private JdbcTemplate jdbcTemplate;
    
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    

    @Override
    public KwlReturnObject addOrRemoveExportLog(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ExportLog exportLog = new ExportLog();
            if (dataMap.containsKey("id") && dataMap.get("id") != null) {
                exportLog = (ExportLog) get(ExportLog.class, (String) dataMap.get("id"));
            }

            if (dataMap.containsKey("companyId") && dataMap.get("companyId") != null) {
                Company company = (Company) get(Company.class, (String) dataMap.get("companyId"));
                exportLog.setCompany(company);
            }

            if (dataMap.containsKey("fileName") && dataMap.get("fileName") != null) {
                exportLog.setFileName((String) dataMap.get("fileName"));
            }

            if (dataMap.containsKey("fileType") && dataMap.get("fileType") != null) {
                exportLog.setFileType((String) dataMap.get("fileType"));
            }

            if (dataMap.containsKey("requestJSON") && dataMap.get("requestJSON") != null) {
                exportLog.setRequestJSON((String) dataMap.get("requestJSON").toString());
            }

            if (dataMap.containsKey("module") && dataMap.get("module") != null) {
                exportLog.setModule((String) dataMap.get("module"));
            }

            if (dataMap.containsKey("requestTime") && dataMap.get("requestTime") != null) {
                exportLog.setRequestTime((Date) dataMap.get("requestTime"));
            }

            if (dataMap.containsKey("status") && dataMap.get("status") != null) {
                exportLog.setStatus((Integer) dataMap.get("status"));
            }
            
            if (dataMap.containsKey("reportDescription") && dataMap.get("reportDescription") != null) {
                exportLog.setReportDescription((String) dataMap.get("reportDescription"));
            }
            
            if (dataMap.containsKey("user") && dataMap.get("user") != null) {
                User user = (User) get(User.class, (String) dataMap.get("user"));
                exportLog.setUser(user);
            }

            saveOrUpdate(exportLog);
            list.add(exportLog);
        } catch (Exception e) {
            throw ServiceException.FAILURE("save ExportLog Table : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Export Details has been added successfully", null, list, list.size());
    }

    
    @Override
    public List<ExportLog> getPendingExports() throws ServiceException{
        List<ExportLog> pendingExport = new ArrayList();
        ArrayList params = new ArrayList();
//        params.add(1);
        String query = "From ExportLog where status IN (1,2)";
        pendingExport = executeQuery(query, params.toArray());
        pendingExport.size();
        return pendingExport;
    }

    @Override
    public KwlReturnObject getExportLog(Map requestParams) throws ServiceException, QueryException {
        List returnList = new ArrayList();

        String condition = "";
        ArrayList params = new ArrayList();

        String companyid = (String) requestParams.get("companyId");
        params.add(companyid);

        if (requestParams.containsKey("startdate") && requestParams.containsKey("enddate")) {
            Date startdate = (Date) requestParams.get("startdate");
            Date enddate = (Date) requestParams.get("enddate");
            params.add(startdate);
            params.add(enddate);

            condition += " and (requestTime>? and requestTime<=?) ";
        }

        if (requestParams.containsKey("statusFilter")) {
            int status = (int) requestParams.get("statusFilter");
            if (status > 0) {
                condition += " and status=?";
                params.add(status);
            }
        }

        if (requestParams.containsKey("user") && requestParams.get("user") != null) {
            String user = (String) requestParams.get("user");
            condition += " and user.userID=?";
            params.add(user);
        }

        String query = "from ExportLog where company.companyID = ? " + condition + " order by requestTime desc";
        returnList = executeQuery(query, params.toArray());

        return new KwlReturnObject(true, "Export Details has been added successfully", null, returnList, returnList.size());
    }

    @Override
    public boolean updateRequestStatus(int status, Map params) throws ServiceException {
        ArrayList paramsList = new ArrayList();
        if (params.containsKey("exportid") && params.get("exportid") != null) {
            paramsList.add(status);
            String id = (String) params.get("exportid");
            paramsList.add(id);
            String query = "update ExportLog as el set el.status=? where el.id=?";
            int change = executeUpdate(query, paramsList.toArray());
            return true;
        }
            return false;
    }


}
