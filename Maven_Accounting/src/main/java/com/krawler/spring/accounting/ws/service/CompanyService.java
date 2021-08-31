/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.ws.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.SQLException;
import java.text.ParseException;
import javax.servlet.http.HttpServletRequest;
/**
 *
 * @author krawler
 */
public interface CompanyService {
    
    public JSONObject getSequenceFormat(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject getRoles(JSONObject jobj) throws ServiceException;

    public JSONObject updateCompany(JSONObject jobj) throws JSONException, ServiceException;
    public JSONObject saveCompany(JSONObject jobj) throws JSONException, ServiceException;

    public JSONObject deactivateCompany(JSONObject jobj) throws ServiceException, JSONException;

    public boolean isCompanyActivated(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject deleteCompany(JSONObject jobj) throws ServiceException, SQLException, JSONException;

    public JSONObject getUserList(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject getUpdates(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject getYearLock(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject getAccountList(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject editUser(JSONObject jobj) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
    public JSONObject getUserPermissions(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject isCompanyExists(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject isUserExists(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject deleteUser(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject verifyLogin(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public JSONObject getNextAutonumber(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public JSONObject getDefaultFieldsforMobileSetup(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public JSONObject createUser(JSONObject paramJobj) throws SQLException, ServiceException, JSONException ;
    
//    public JSONObject activateDeactivateUser(JSONObject paramJobj) throws SQLException, ServiceException, JSONException  ;
    /**
     * Method used to parepare JSON required for audit trail rest method.
     * @param paramJobj
     * @return
     * @throws JSONException
     * @throws ServiceException
     * @throws SessionExpiredException
     * @throws ParseException 
     */
    public JSONObject getAuditTrails(JSONObject paramJobj) throws JSONException, ServiceException,SessionExpiredException, ParseException;
    
    public JSONObject assignRole(JSONObject paramJobj) throws SQLException, ServiceException, JSONException  ;
    
    public JSONObject activateUser(JSONObject paramJobj) throws SQLException, ServiceException, JSONException;
    
    public JSONObject deactivateUser(JSONObject paramJobj) throws SQLException, ServiceException, JSONException;
    
    public JSONObject createCompany(JSONObject paramObj) throws SQLException, ServiceException, JSONException;
    
    public JSONObject getAllDateFormats(JSONObject paramObj) throws SQLException, ServiceException, JSONException;
    
    public JSONObject getAllTimeZones(JSONObject paramObj) throws SQLException, ServiceException, JSONException;
    
    public JSONObject getAllUserDetails(JSONObject paramObj) throws SQLException, ServiceException, JSONException;
    
   public JSONObject saveUsers(JSONObject paramJobj) throws ServiceException, JSONException ;
   
   public JSONObject getUrls(JSONObject paramJobj) throws ServiceException, JSONException ;
   
   public JSONObject getGSTConfiguration(JSONObject paramJobj) throws ServiceException, JSONException ;
 
   public JSONObject saveOrUpdatePasswordPolicy(JSONObject paramJobj) throws JSONException,ServiceException;
 
   public JSONObject saveDeskeraProxyDetails(JSONObject paramJobj) throws ServiceException, JSONException ;
 
}

