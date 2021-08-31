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
package com.krawler.spring.permissionHandler;

import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;

/**
 *
 * @author Karthik
 */
public interface permissionHandlerDAO {

    public KwlReturnObject getFeatureList() throws ServiceException;

    public KwlReturnObject getActivityList() throws ServiceException;

    public KwlReturnObject getRoleList(JSONObject requestJobj) throws ServiceException;
    
    public KwlReturnObject getReportUserList(String userid, String companyid) throws ServiceException;

    public KwlReturnObject saveFeatureList(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveRoleList(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveActivityList(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteFeature(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteRole(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteActivity(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getActivityFeature() throws ServiceException;

    public KwlReturnObject getUserPermission(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject setPermissions(HashMap<String, Object> requestParams, String[] features, String[] permissions) throws ServiceException;
    
    public boolean isSuperAdmin(String userid, String companyid) throws ServiceException;

    public KwlReturnObject getRoleofUser(String userid) throws ServiceException;

    public void deleteUserPermissions(String roleid) throws ServiceException;

    public User getCreator(String companyid);
    
    public KwlReturnObject getRolePermission(HashMap<String, Object> requestParams) throws ServiceException;
   
    public void assignRoles(String userid, String roleid) throws ServiceException;
}
