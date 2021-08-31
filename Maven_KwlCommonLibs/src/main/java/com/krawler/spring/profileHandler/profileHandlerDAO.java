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
package com.krawler.spring.profileHandler;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Karthik
 */
public interface profileHandlerDAO {

    public String getUserFullName(String userid) throws ServiceException;

    public KwlReturnObject getUserDetails(HashMap<String, Object> requestParams, ArrayList filter_names, ArrayList filter_params) throws ServiceException;

    public KwlReturnObject getAllManagers(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveUser(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject addUser(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject addCompany(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateCompany(HashMap<String, Object> requestParams) throws ServiceException;
    
    public void deleteCompanySetUpData(Map<String, Object> requestParams) throws ServiceException;

    public void deleteUser(String id) throws ServiceException;

    public void saveUserLogin(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject changePassword(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject changeUserPassword(String platformURL, HashMap<String, Object> requestParams) throws ServiceException;

    public String getUser_hash(String userid) throws ServiceException;

    public void saveUserLastLogin(HashMap<String, Object> requestParams) throws ServiceException;

    public boolean checkDuplicateEmailID(String emailid, String companyid, String userid) throws ServiceException;
    
    public boolean isUserExist(String username, String companyid) throws ServiceException;
    
    public List getUserExistWithUserID(String userid) throws ServiceException;
    
    public KwlReturnObject getUserWithUserName(String userid, boolean isUserName) throws ServiceException;
    
    public boolean isTransactionCreated(String companyid) throws ServiceException;
    
    public KwlReturnObject saveRoleUserMapping(HashMap<String ,Object> requestParams) throws ServiceException;
    
    public KwlReturnObject resetUserPassword(String userid,String password) throws ServiceException;
     
    public KwlReturnObject setUserDepartment(HashMap<String ,Object> requestParams) throws ServiceException;
    
    public boolean checkIsUserApprover(String userId)throws ServiceException;
    
    public String getUserIdFromUserName(String userName, String companyId) throws ServiceException;
    
    public String getUserGroupForUser(Map<String,String> map) throws ServiceException;
    
    public String getSysEmailIdByCompanyID(String companyid);
    
    public String getStateidByStateName(String companyid);
    
    public void saveOrUpdatePasswordPolicy(JSONObject jobj) throws ServiceException;
    
    public KwlReturnObject getPasswordPolicy(String companyid) throws ServiceException;
    
    public int deletePasswordPolicy(JSONObject jobj) throws ServiceException;
}
